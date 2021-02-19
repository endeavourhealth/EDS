package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.endeavourhealth.transform.common.ExchangePayloadFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Pattern;

/**
 * common functions used by lots of one-off routines
 */
public abstract class AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRoutine.class);

    private static final int BULK_STARTED = 0;
    private static final int BULK_DONE = 1;

    public static boolean shouldSkipService(Service service, String odsCodeRegex) {
        if (Strings.isNullOrEmpty(odsCodeRegex)) {
            return false;
        }

        String odsCode = service.getLocalId();
        if (!Strings.isNullOrEmpty(odsCode)
                && Pattern.matches(odsCodeRegex, odsCode)) {
            return false;
        }

        String ccgCode = service.getCcgCode();
        if (!Strings.isNullOrEmpty(ccgCode)
                && Pattern.matches(odsCodeRegex, ccgCode)) {
            return false;
        }

        LOG.debug("Skipping " + service + " due to regex");
        return true;
    }

    public static boolean isServiceStartedOrDoneBulkOperation(Service service, String bulkOperationName, boolean includeStartedButNotFinishedServices) throws Exception {
        if (includeStartedButNotFinishedServices) {
            //if including started ones, we only want to exclude DONE ones
            boolean ret = isServiceDoneBulkOperation(service, bulkOperationName);
            if (ret) {
                LOG.debug("Skipping " + service + " as already done");
            }
            return ret;

        } else {
            //if not including started ones, exclude any DONE or STARTED
            boolean ret = isServiceStartedOrDoneBulkOperation(service, bulkOperationName);
            if (ret) {
                LOG.debug("Skipping " + service + " as already started or done");
            }
            return ret;
        }
    }

    /**
     * checks if the given service has already done the given bulk operation and audits the start if not
     */
    public static boolean isServiceDoneBulkOperation(Service service, String bulkOperationName) throws Exception {

        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "SELECT 1"
                    + " FROM bulk_operation_audit"
                    + " WHERE service_id = ?"
                    + " AND operation_name = ?"
                    + " AND status = ?";
            ps = connection.prepareStatement(sql);
            int col = 1;
            ps.setString(col++, service.getId().toString());
            ps.setString(col++, bulkOperationName);
            ps.setInt(col++, BULK_DONE);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }

        startBulkOperation(service, bulkOperationName);
        return false;
    }

    public static boolean isServiceStartedOrDoneBulkOperation(Service service, String bulkOperationName) throws Exception {

        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "SELECT 1"
                    + " FROM bulk_operation_audit"
                    + " WHERE service_id = ?"
                    + " AND operation_name = ?"
                    + " AND (status = ? OR status = ?)";
            ps = connection.prepareStatement(sql);
            int col = 1;
            ps.setString(col++, service.getId().toString());
            ps.setString(col++, bulkOperationName);
            ps.setInt(col++, BULK_STARTED);
            ps.setInt(col++, BULK_DONE);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }

        startBulkOperation(service, bulkOperationName);
        return false;
    }

    private static void startBulkOperation(Service service, String bulkOperationName) throws Exception {

        //if not done, audit that we're doing it
        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "INSERT INTO bulk_operation_audit (service_id, operation_name, status, started) "
                    + " VALUES (?, ?, ?, ?)";
            ps = connection.prepareStatement(sql);
            int col = 1;
            ps.setString(col++, service.getId().toString());
            ps.setString(col++, bulkOperationName);
            ps.setInt(col++, BULK_STARTED); //0 = started
            ps.setTimestamp(col++, new java.sql.Timestamp(new Date().getTime()));
            ps.executeUpdate();
            connection.commit();

        } catch (Exception ex) {
            connection.rollback();
            throw ex;

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    /**
     * updates the bulk operation audit table to say the given bulk is done
     */
    public static void setServiceDoneBulkOperation(Service service, String bulkOperationName) throws Exception {

        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "UPDATE bulk_operation_audit "
                    + " SET status = ?, finished = ? "
                    + "WHERE service_id = ? "
                    + "AND operation_name = ? "
                    + "AND status = ?";
            ps = connection.prepareStatement(sql);
            int col = 1;
            ps.setInt(col++, BULK_DONE);
            ps.setTimestamp(col++, new java.sql.Timestamp(new Date().getTime()));
            ps.setString(col++, service.getId().toString());
            ps.setString(col++, bulkOperationName);
            ps.setInt(col++, BULK_STARTED);
            ps.executeUpdate();
            connection.commit();

        } catch (Exception ex) {
            connection.rollback();
            throw ex;

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    /**
     * handy fn to stop a routine for manual inspection before continuing (or quitting)
     */
    public static void continueOrQuit() throws Exception {
        LOG.info("Enter y to continue, anything else to quit");

        byte[] bytes = new byte[10];
        java.lang.System.in.read(bytes);
        char c = (char) bytes[0];
        if (c != 'y' && c != 'Y') {
            java.lang.System.out.println("Read " + c);
            java.lang.System.exit(1);
        }
    }

    public static void copyFile(File src, File dst) throws Exception {
        FileInputStream fis = new FileInputStream(src);
        BufferedInputStream bis = new BufferedInputStream(fis);
        Files.copy(bis, dst.toPath());
        bis.close();
    }

    public static String findFilePathInExchange(Exchange exchange, String fileType) {

        String exchangeBody = exchange.getBody();
        List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);

        for (ExchangePayloadFile file : files) {
            if (file.getType().equals(fileType)) {
                return file.getPath();
            }
        }

        return null;
    }


    public static Exchange createNewExchange(Service service, UUID systemId, String messageFormat, String eventDesc) throws Exception {
        return createNewExchange(service, systemId, messageFormat, eventDesc, UUID.randomUUID());
    }

    public static Exchange createNewExchange(Service service, UUID systemId, String messageFormat, String eventDesc, UUID exchangeId) throws Exception {

        String bodyJson = JsonSerializer.serialize(new ArrayList<ExchangePayloadFile>());
        String odsCode = service.getLocalId();

        Exchange ret = new Exchange();
        ret.setId(exchangeId);
        ret.setBody(bodyJson);
        ret.setTimestamp(new Date());
        ret.setHeaders(new HashMap<>());
        ret.setHeaderAsUuid(HeaderKeys.SenderServiceUuid, service.getId());
        ret.setHeader(HeaderKeys.ProtocolIds, ""); //just set to non-null value, so postToExchange(..) can safely recalculate
        ret.setHeader(HeaderKeys.SenderLocalIdentifier, odsCode);
        ret.setHeaderAsUuid(HeaderKeys.SenderSystemUuid, systemId);
        ret.setHeader(HeaderKeys.SourceSystem, messageFormat);
        ret.setServiceId(service.getId());
        ret.setSystemId(systemId);

        AuditWriter.writeExchange(ret);
        AuditWriter.writeExchangeEvent(ret, eventDesc);

        return ret;
    }
}

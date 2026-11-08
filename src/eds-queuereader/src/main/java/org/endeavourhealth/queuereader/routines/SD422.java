package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.PublishedFileDalI;
import org.endeavourhealth.core.database.dal.audit.models.PublishedFileRecord;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SD422 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD478.class);

    private static final String BULK_OPERATION_NAME = "Fixing TPP local codes (SD-478)";

    /**
     * fixes data SD-422
     *
     * problems is the bulk routine to populate the audit.published_file_record table didn't do all records, most likely
     * due to a threading issue
     */
    public static void fixPublishedFileRecords() {
        LOG.debug("Fixing Missing Published File Records");
        try {
            List<Integer> ids = findIdsToFix();
            LOG.debug("Found " + ids.size() + " to fix");

            int done = 0;

            for (Integer id: ids) {
                fixPublishedFile(id);
                markAsDone(id);

                done ++;
                if (done % 1000 == 0) {
                    LOG.debug("Done " + done);
                }
            }

            LOG.debug("Finished " + done);
            LOG.debug("Finished Fixing Missing Published File Records");

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixPublishedFile(Integer id) throws Exception {

        //find the records that we DO have
        Set<Integer> existingRecords = findExistingRecords(id);

        String filePath = retrieveFilePath(id);
        LOG.debug("Doing file " + id + " with " + existingRecords.size() + " records:  " + filePath);

        //if our set of records DOES NOT contain a 1, then the first record contains headers
        boolean firstRecordContainsHeaders = !existingRecords.contains(new Integer(1));

        //if we're missing a 1 and 2 then the above logic is wrong
        if (!existingRecords.contains(new Integer(1))
                && !existingRecords.contains(new Integer(2))) {
            throw new Exception("Missing record 1 and 2 for file " + id);
        }

        PublishedFileDalI dal = DalProvider.factoryPublishedFileDal();

        int added = 0;

        //go through the file
        InputStream inputStream = FileHelper.readFileFromSharedStorage(filePath);
        try {
            long currentRecordStart = 0;
            long currentBytePos = 0;
            int recordNumber = 1; //these start at one
            byte[] buffer = new byte[500 * 1024]; //use a 500KB buffer

            while (true) {

                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1){
                    //if we've hit the end of the file, we're done
                    break;
                }

                for (int i=0; i<bytesRead; i++) {
                    byte b = buffer[i];
                    currentBytePos ++;

                    //when we hit a newline character, then audit that record
                    if (b == '\n') {
                        //LOG.debug("Found newline at " + currentBytePos + " on record " + recordNumber);

                        //don't audit record 0 if we know it contains headers
                        if (recordNumber > 1 || !firstRecordContainsHeaders) {

                            PublishedFileRecord fileRecord = new PublishedFileRecord();
                            fileRecord.setPublishedFileId(id.intValue());
                            fileRecord.setRecordNumber(recordNumber);
                            fileRecord.setByteStart(currentRecordStart);
                            fileRecord.setByteLength((int) (currentBytePos - currentRecordStart));

                            if (!existingRecords.contains(new Integer(recordNumber))) {
                                List<PublishedFileRecord> l = new ArrayList<>();
                                l.add(fileRecord);
                                dal.auditFileRows(l);
                                added ++;
                            }
                        }

                        //LOG.debug("Record " + recordNumber + " starts at " + currentRecordStart + " and ends at " + currentBytePos + " so len = " + (currentBytePos - currentRecordStart));
                        recordNumber ++;
                        currentRecordStart = currentBytePos;
                    }
                }

                //if we have a trailing record, audit that
                if (currentRecordStart != currentBytePos) {
                    PublishedFileRecord fileRecord = new PublishedFileRecord();
                    fileRecord.setPublishedFileId(id.intValue());
                    fileRecord.setRecordNumber(recordNumber);
                    fileRecord.setByteStart(currentRecordStart);
                    fileRecord.setByteLength((int) (currentBytePos - currentRecordStart));

                    if (!existingRecords.contains(new Integer(recordNumber))) {
                        List<PublishedFileRecord> l = new ArrayList<>();
                        l.add(fileRecord);
                        dal.auditFileRows(l);
                        added ++;
                    }
                }
            }

        } finally {
            inputStream.close();
        }

        LOG.debug("    Added " + added + " records");
    }

    private static String retrieveFilePath(Integer id) throws Exception {

        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "SELECT file_path FROM published_file WHERE id = ?";
            ps = connection.prepareStatement(sql);

            ps.setInt(1, id.intValue());

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("Failed to find file_path for " + id);
            }
            return rs.getString(1);

        } finally {
            ps.close();
            connection.close();
        }

    }

    private static Set<Integer> findExistingRecords(Integer id) throws Exception {

        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "SELECT record_number FROM published_file_record WHERE published_file_id = ?";
            ps = connection.prepareStatement(sql);

            ps.setInt(1, id.intValue());

            Set<Integer> ret = new HashSet<>();

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int recordNum = rs.getInt(1);
                ret.add(new Integer(recordNum));
            }

            return ret;

        } finally {
            ps.close();
            connection.close();
        }
    }


    private static void markAsDone(Integer id) throws Exception {

        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "UPDATE tmp.SD422_files_to_fix SET done = 1 WHERE published_file_id = ?";
            ps = connection.prepareStatement(sql);

            ps.setInt(1, id.intValue());

            ps.executeUpdate();
            connection.commit();

        } finally {
            ps.close();
            connection.close();
        }
    }

    private static List<Integer> findIdsToFix() throws Exception {

        Connection connection = ConnectionManager.getAuditConnection();
        Statement statement = connection.createStatement();
        try {
            String sql = "SELECT published_file_id FROM tmp.SD422_files_to_fix WHERE done = 0";
            ResultSet rs = statement.executeQuery(sql);

            List<Integer> ret = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt(1);
                ret.add(new Integer(id));
            }

            return ret;

        } finally {
            statement.close();
            connection.close();
        }
    }

}
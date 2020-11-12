package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SD186 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD186.class);

    /**
     * Finds all distinct codes in Vision data and populates a table defined by:

     create table tmp.vision_codes (
        code varchar(255) CHARACTER SET latin1 COLLATE latin1_bin,
        cnt int,
        CONSTRAINT pk PRIMARY KEY (code)
     );

     */
    public static void findVisionCodes() {
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

            UUID systemId = UUID.fromString("4809b277-6b8d-4e5c-be9c-d1f1d62975c6");
            String operationName = "Find distinct codes (SD-186)";

            List<Service> services = serviceDal.getAll();
            for (Service service: services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("Vision")) {
                    continue;
                }

                if (isServiceDoneBulkOperation(service, operationName)) {
                    continue;
                }

                LOG.debug("Doing service " + service);

                Map<String, AtomicInteger> hmCounts = new HashMap<>();

                List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
                for (Exchange exchange: exchanges) {

                    if (!ExchangeHelper.isAllowRequeueing(exchange)) {
                        continue;
                    }

                    String path = findFilePathInExchange(exchange, "journal_data_extract");
                    if (Strings.isNullOrEmpty(path)) {
                        continue;
                    }
                    LOG.debug("    Doing exchange " + exchange.getId() + " -> " + path);
                    int done = 0;

                    InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);
                    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader("PID","ID","DATE","RECORDED_DATE","CODE","SNOMED_CODE","BNF_CODE","HCP","HCP_TYPE","GMS","EPISODE","TEXT","RUBRIC","DRUG_FORM","DRUG_STRENGTH","DRUG_PACKSIZE","DMD_CODE","IMMS_STATUS","IMMS_COMPOUND","IMMS_SOURCE","IMMS_BATCH","IMMS_REASON","IMMS_METHOD","IMMS_SITE","ENTITY","VALUE1_NAME","VALUE1","VALUE1_UNITS","VALUE2_NAME","VALUE2","VALUE2_UNITS","END_DATE","TIME","CONTEXT","CERTAINTY","SEVERITY","LINKS","LINKS_EXT","SERVICE_ID","ACTION","SUBSET","DOCUMENT_ID");
                    CSVParser parser = new CSVParser(isr, csvFormat);

                    Iterator<CSVRecord> iterator = parser.iterator();
                    while (iterator.hasNext()) {
                        CSVRecord record = iterator.next();
                        String code = record.get("CODE");
                        if (Strings.isNullOrEmpty(code)) {
                            continue;
                        }

                        AtomicInteger count = hmCounts.get(code);
                        if (count == null) {
                            count = new AtomicInteger(0);
                            hmCounts.put(code, count);
                        }
                        count.incrementAndGet();

                        done ++;
                        if (done % 5000 == 0) {
                            LOG.debug("    Done " + done + " lines");
                        }
                    }

                    isr.close();
                    LOG.debug("    Finished, reading " + done + " lines");
                }

                LOG.debug("   Done, finding " + hmCounts.size() + " codes");

                String sql = "INSERT INTO tmp.vision_codes (code, cnt)"
                            + " VALUES (?, ?)"
                            + " ON DUPLICATE KEY UPDATE"
                            + " cnt = cnt + VALUES(cnt)";
                Connection connection = ConnectionManager.getAdminConnection();
                PreparedStatement ps = connection.prepareStatement(sql);

                for (String code: hmCounts.keySet()) {
                    AtomicInteger count = hmCounts.get(code);

                    ps.setString(1, code);
                    ps.setInt(2, count.get());
                    ps.executeUpdate();
                    connection.commit();
                }

                ps.close();
                connection.close();
                LOG.debug("   Updated code table");

                setServiceDoneBulkOperation(service, operationName);
            }

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

}

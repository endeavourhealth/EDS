package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SD307 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD307.class);

    /**
     * finds affected TPP services for SD307
     */
    public static void findTppServicesMissingDeltas(boolean verbose, String odsCodeRegex) {
        LOG.debug("Finding TPP Services Missing Deltas " + odsCodeRegex);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                LOG.debug("Doing " + service);
                findTppServicesMissingDeltasForService(verbose, service);
            }

            LOG.debug("Finished Finding TPP Services Missing Deltas " + odsCodeRegex);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void findTppServicesMissingDeltasForService(boolean verbose, Service service) throws Exception {

        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.TPP_CSV);
        if (endpoint == null) {
            LOG.warn("No emis endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
        LOG.debug("Found " + exchanges.size() + " exchanges");

        Map<String, List<DateRange>> hmFiles = new HashMap<>();

        //exchange list is most-recent-first, so go backwards
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            if (!ExchangeHelper.isAllowRequeueing(exchange)) {
                continue;
            }

            String filePath = findFilePathInExchange(exchange, "Manifest");
            if (Strings.isNullOrEmpty(filePath)) {
                LOG.warn("Missing manifest file in exchange " + exchange.getId());
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(filePath);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            //FileName,IsDelta,IsReference,DateExtractFrom,DateExtractTo

            //20200705_1707
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String fileName = record.get("FileName");
                String isDeltaYN = record.get("IsDelta");
                String startStr = record.get("DateExtractFrom");
                String endStr = record.get("DateExtractTo");

                List<DateRange> list = hmFiles.get(fileName);
                if (list == null) {
                    list = new ArrayList<>();
                    hmFiles.put(fileName, list);
                }

                //if it's a bulk, clear out what was before
                boolean isBulk = isDeltaYN.equalsIgnoreCase("N");
                if (isBulk) {
                    list.clear();
                }

                DateRange r = new DateRange();
                r.setFilePath(filePath);
                r.setBulk(isBulk);
                r.setExchangeId(exchange.getId());
                r.setFromStr(startStr);
                r.setToStr(endStr);
                if (!Strings.isNullOrEmpty(startStr)) {
                    r.setFrom(dateFormat.parse(startStr));
                }
                if (!Strings.isNullOrEmpty(endStr)) {
                    r.setTo(dateFormat.parse(endStr));
                }
                list.add(r);
            }

            parser.close();
        }

        LOG.debug("Cached file metadata, checking...");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        List<String> fileNames = new ArrayList<>(hmFiles.keySet());
        fileNames.sort((a, b) -> a.compareToIgnoreCase(b));

        for (String fileName: fileNames) {
            List<DateRange> list = hmFiles.get(fileName);

            LOG.debug("Checking " + fileName + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            /*if (verbose) {
                LOG.debug("Checking " + fileName + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }*/

            DateRange lastDateRange = list.get(0);
            for (int i=1; i<list.size(); i++) {
                DateRange dateRange = list.get(i);

                Date previousStart = lastDateRange.getFrom();
                Date previousEnd = lastDateRange.getTo();

                Date currentStart = dateRange.getFrom();
                Date currentEnd = dateRange.getTo();

                if (verbose) {
                    LOG.debug("    " + dateRange);
                }

                //if the start and end don't match up, then something is off
                if (currentStart == null) {
                    LOG.warn("    NULL START DATE: exchange " + dateRange.getExchangeId());

                } else if (currentStart.equals(previousEnd)) {
                    //OK

                } else if (previousStart != null && currentStart.equals(previousStart)
                        && currentEnd.equals(previousEnd)) {
                    LOG.warn("    DUPLICATE FOUND: exchange " + dateRange.getExchangeId() + " has range " + dateFormat.format(currentStart) + " - " + dateFormat.format(currentEnd) + " which is the same as previous");

                } else if (currentStart.after(previousEnd)) {
                    LOG.error("    GAP FOUND: exchange " + dateRange.getExchangeId() + " expecting start " + dateFormat.format(previousEnd) + " but got " + dateFormat.format(currentStart));

                } else {
                    LOG.error("    GONE BACK: exchange " + dateRange.getExchangeId() + " expecting start " + dateFormat.format(previousEnd) + " but got " + dateFormat.format(currentStart));
                }

                lastDateRange = dateRange;
            }
        }
    }

    private static class DateRange {
        private boolean isBulk;
        private String fromStr;
        private String toStr;
        private Date from;
        private Date to;
        private UUID exchangeId;
        private String filePath;

        public boolean isBulk() {
            return isBulk;
        }

        public void setBulk(boolean bulk) {
            isBulk = bulk;
        }

        public String getFromStr() {
            return fromStr;
        }

        public void setFromStr(String fromStr) {
            this.fromStr = fromStr;
        }

        public String getToStr() {
            return toStr;
        }

        public void setToStr(String toStr) {
            this.toStr = toStr;
        }

        public Date getFrom() {
            return from;
        }

        public void setFrom(Date from) {
            this.from = from;
        }

        public Date getTo() {
            return to;
        }

        public void setTo(Date to) {
            this.to = to;
        }

        public UUID getExchangeId() {
            return exchangeId;
        }

        public void setExchangeId(UUID exchangeId) {
            this.exchangeId = exchangeId;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            if (fromStr == null) {
                sb.append("NULL");
            } else {
                sb.append(fromStr);
            }
            sb.append(" -> ");
            if (toStr == null) {
                sb.append("NULL");
            } else {
                sb.append(toStr);
            }
            sb.append(" (exchange " + exchangeId + ")");
            if (isBulk) {
                sb.append(" BULK");
            }
            sb.append(" " + filePath);
            return sb.toString();
        }
    }
}
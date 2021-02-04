package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.FileInfo;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.endeavourhealth.transform.common.ExchangePayloadFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
            List<Service> servicesWithGaps = new ArrayList<>();

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
                boolean gapFound = findTppServicesMissingDeltasForService(verbose, service);

                if (gapFound) {
                    servicesWithGaps.add(service);
                }
            }

            LOG.debug("Finished Finding TPP Services Missing Deltas " + odsCodeRegex);

            LOG.debug("Found " + servicesWithGaps.size() + " services with gaps");
            for (Service service: servicesWithGaps) {
                LOG.debug("" + service);
            }

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static boolean findTppServicesMissingDeltasForService(boolean verbose, Service service) throws Exception {

        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.TPP_CSV);
        if (endpoint == null) {
            LOG.warn("No TPP endpoint found for " + service);
            return false;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();
        String odsCode = service.getLocalId();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
        LOG.debug("Found " + exchanges.size() + " exchanges");

        Map<String, Map<String, List<DateRange>>> hmExtractConfiguration = new HashMap<>();

        //exchange list is most-recent-first, so go forwards to find the most recent bulk
        int bulkIndex = -1;
        for (int i=0; i<exchanges.size()-1; i++) {
            Exchange exchange = exchanges.get(i);
            Boolean isBulk = exchange.getHeaderAsBoolean(HeaderKeys.IsBulk);
            if (isBulk != null && isBulk.booleanValue()) {
                bulkIndex = i;
                break;
            }
        }
        if (bulkIndex == -1) {
            throw new Exception("Failed to find bulk for " + service);
        }

        //exchange list is most-recent-first, so go backwards FROM the last bulk TO the latest data
        for (int i=bulkIndex; i>=0; i--) {
            Exchange exchange = exchanges.get(i);

            //let these be counted
            /*if (!ExchangeHelper.isAllowRequeueing(exchange)) {
                continue;
            }*/

            String manifestFilePath = findFilePathInExchange(exchange, "Manifest");
            if (Strings.isNullOrEmpty(manifestFilePath)) {
                attemptFixExchangeMissingManifest(exchange);
                manifestFilePath = findFilePathInExchange(exchange, "Manifest");
                if (Strings.isNullOrEmpty(manifestFilePath)) {
                    LOG.warn("Missing manifest file in exchange " + exchange.getId());
                    continue;
                }
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(manifestFilePath);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            //FileName,IsDelta,IsReference,DateExtractFrom,DateExtractTo

            //20200705_1707
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
            String firstEndStr = null;

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String fileName = record.get("FileName");
                String isDeltaYN = record.get("IsDelta");
                String startStr = record.get("DateExtractFrom");
                String endStr = record.get("DateExtractTo");

                //the main hash map is keyed by the extract configuration part of the path,
                //since we have TPP practices that were in multiple SystmOne extract configurations
                String extractConfiguration = findExtractConfiguration(manifestFilePath);

                Map<String, List<DateRange>> hmFiles = hmExtractConfiguration.get(extractConfiguration);
                if (hmFiles == null) {
                    hmFiles = new HashMap<>();
                    hmExtractConfiguration.put(extractConfiguration, hmFiles);
                }

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
                r.setManifestFilePath(manifestFilePath);
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

                //why would any record have an empty start
                if (Strings.isNullOrEmpty(startStr) && Strings.isNullOrEmpty(endStr)) {

                    //there's something odd about this file and it seems to have null dates in every manifest, so just ignore it
                    if (!fileName.equals("SRAppointmentAttendees")) {
                        LOG.warn("NULL start and end date for " + fileName + " in " + manifestFilePath);
                    }

                } else if (Strings.isNullOrEmpty(startStr)) {

                    //null start is OK if we're a bulk
                    if (!isBulk) {
                        LOG.warn("NULL start date for " + fileName + " in " + manifestFilePath);
                    }

                } else if (Strings.isNullOrEmpty(endStr)) {
                    LOG.warn("NULL end date for " + fileName + " in " + manifestFilePath);
                }


                if (!Strings.isNullOrEmpty(endStr)) {
                    //verify if the end date is always the same for records
                    if (firstEndStr == null) {
                        firstEndStr = endStr;
                    } else if (!firstEndStr.equalsIgnoreCase(endStr)) {
                        LOG.error("Got multiple distinct end dates " + firstEndStr + " vs " + endStr + " in " + manifestFilePath);
                    }
                }
            }

            parser.close();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        boolean gapFound = false;

        if (hmExtractConfiguration.size() > 1) {
            LOG.error("Service is in MULTIPLE extract configurations");
        } else {
            LOG.debug("Service is in ONE extract configurations");
        }

        for (String extractConfiguration: hmExtractConfiguration.keySet()) {
            LOG.debug("Extract configuration " + extractConfiguration + " ===================================================================================================================================");

            Map<String, List<DateRange>> hmFiles = hmExtractConfiguration.get(extractConfiguration);
            //LOG.debug("Cached " + hmFiles.size() + " file metadata, checking...");

            List<String> fileNames = new ArrayList<>(hmFiles.keySet());
            fileNames.sort((a, b) -> a.compareToIgnoreCase(b));

            for (String fileName : fileNames) {
                List<DateRange> list = hmFiles.get(fileName);

                //don't care about files we don't process
                if (!getTransformedFileNames().contains(fileName)) {
                    continue;
                }

                LOG.debug("Checking " + fileName + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                /*if (verbose) {
                    LOG.debug("Checking " + fileName + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                }*/

                DateRange lastDateRange = list.get(0);
                if (verbose) {
                    LOG.debug("    " + lastDateRange);
                }

                for (int i = 1; i < list.size(); i++) {
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
                        LOG.warn("    NULL START DATE: " + odsCode + " " + extractConfiguration + "::" + fileName + " exchange " + dateRange.getExchangeId());

                    } else if (currentStart.equals(previousEnd)) {
                        //OK

                    } else if (previousStart != null && currentStart.equals(previousStart)
                            && currentEnd.equals(previousEnd)) {
                        LOG.warn("    DUPLICATE FOUND: " + odsCode + " " + extractConfiguration + "::" + fileName + " exchange " + dateRange.getExchangeId() + " has range " + dateFormat.format(currentStart) + " - " + dateFormat.format(currentEnd) + " which is the same as previous");

                    } else if (currentStart.after(previousEnd)) {
                        LOG.error("    GAP FOUND: " + odsCode + " " + extractConfiguration + "::" + fileName + " exchange " + dateRange.getExchangeId() + " expecting start " + dateFormat.format(previousEnd) + " but got " + dateFormat.format(currentStart));
                        gapFound = true;

                    } else {
                        LOG.warn("    GONE BACK: " + odsCode + " " + extractConfiguration + "::" + fileName + " exchange " + dateRange.getExchangeId() + " expecting start " + dateFormat.format(previousEnd) + " but got " + dateFormat.format(currentStart));
                    }

                    lastDateRange = dateRange;
                }
            }
        }

        return gapFound;
    }

    private static Set<String> getTransformedFileNames() {
        Set<String> ret = new HashSet<>();

        ret.add("SRCcg");
        ret.add("SROrganisationBranch");
        ret.add("SROrganisation");
        ret.add("SRTrust");
        ret.add("SRAppointmentFlags");
        ret.add("SRAppointment");
        ret.add("SRRota");
        ret.add("SRVisit");
        ret.add("SRChildAtRisk");
        ret.add("SRCode");
        ret.add("SRDrugSensitivity");
        ret.add("SREvent");
        ret.add("SRImmunisation");
        ret.add("SRPersonAtRisk");
        ret.add("SRProblem");
        ret.add("SRRecall");
        ret.add("SRReferralOut");
        ret.add("SRRepeatTemplate");
        ret.add("SRSpecialNotes");
        ret.add("SRConfiguredListOption");
        ret.add("SRCtv3");
        ret.add("SRCtv3ToSnomed");
        ret.add("SRImmunisationContent");
        ret.add("SRMapping");
        ret.add("SRMedicationReadCodeDetails");

        return ret;
    }

    /**
     * extracts the element of the path that indicates the extract configuration it came from
     * e.g.
     * from
     *  S3/discoverysftplanding/endeavour/sftpReader/TPP/YDDH3_08Y/2021-01-01T04.03.00/Split/E87711/SRManifest.csv
     * find
     *  YDDH3_08Y
     */
    private static String findExtractConfiguration(String filePath) {
        File f = new File(filePath);
        f = f.getParentFile(); //S3/discoverysftplanding/endeavour/sftpReader/TPP/YDDH3_08Y/2021-01-01T04.03.00/Split/E87711
        f = f.getParentFile(); //S3/discoverysftplanding/endeavour/sftpReader/TPP/YDDH3_08Y/2021-01-01T04.03.00/Split
        f = f.getParentFile(); //S3/discoverysftplanding/endeavour/sftpReader/TPP/YDDH3_08Y/2021-01-01T04.03.00/
        f = f.getParentFile(); //S3/discoverysftplanding/endeavour/sftpReader/TPP/YDDH3_08Y/
        return f.getName();
    }

    /**
     * on older exchanges, we didn't used to copy the SRManifest.csv file into the service-specific "Split" directory.
     * The SRManfiest files were copied over a while back, but the Exchange bodies weren't updated accordingly
     */
    private static void attemptFixExchangeMissingManifest(Exchange exchange) throws Exception {

        String exchangeBody = exchange.getBody();
        List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);
        if (files.isEmpty()) {
            return;
        }

        LOG.debug("Attempting to fix Exchange " + exchange.getId() + " without SRManifest in body");

        ExchangePayloadFile first = files.get(0);
        String firstPath = first.getPath();
        String dir = FilenameUtils.getFullPath(firstPath);
        LOG.debug("Getting listing of " + dir);

        List<FileInfo> listing = FileHelper.listFilesInSharedStorageWithInfo(dir);
        for (FileInfo info: listing) {
            String path = info.getFilePath();
            if (path.endsWith("SRManifest.csv")) {
                LOG.debug("Found manifest file at " + path);

                //get the file list again, but this time without the storage prefix, so we can use to populate the exchange body from it
                List<ExchangePayloadFile> filesNoPrefix = ExchangeHelper.parseExchangeBody(exchangeBody, false);
                first = filesNoPrefix.get(0);
                firstPath = first.getPath();
                dir = FilenameUtils.getFullPath(firstPath);
                String newPath = FilenameUtils.concat(dir, "SRManifest.csv");
                LOG.debug("Adding path to body " + newPath);

                ExchangePayloadFile f = new ExchangePayloadFile();
                f.setPath(newPath);
                f.setSize(new Long(info.getSize()));
                f.setType("Manifest");
                filesNoPrefix.add(f);

                String json = JsonSerializer.serialize(filesNoPrefix);
                //LOG.debug("New body: " + json);
                exchange.setBody(json);

                //fix on the DB
                AuditWriter.writeExchange(exchange);

                return;
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
        private String manifestFilePath;

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

        public String getManifestFilePath() {
            return manifestFilePath;
        }

        public void setManifestFilePath(String manifestFilePath) {
            this.manifestFilePath = manifestFilePath;
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
            sb.append(" " + manifestFilePath);
            return sb.toString();
        }
    }
}
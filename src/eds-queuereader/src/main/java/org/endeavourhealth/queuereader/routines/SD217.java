package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.endeavourhealth.transform.common.Read2Cache;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SD217 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD217.class);

    private static DateFormat visionDateFormat = new SimpleDateFormat("yyyyMMdd");
    private static DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void findAffectedVisionPatients(String odsCodeRegex) {
        LOG.debug("Finding Vision Patients Affected By Imms Bug for " + odsCodeRegex);
        try {

            UUID visionSystemId = UUID.fromString("4809b277-6b8d-4e5c-be9c-d1f1d62975c6");
            Set<String> hsDataSetCodes = getDataSetCodes();

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("EMIS")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
                List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), visionSystemId, Integer.MAX_VALUE);
                if (exchanges.isEmpty()) {
                    continue;
                }

                LOG.debug("Doing " + service);
                LOG.debug("Found " + exchanges.size() + " exchanges");

                //blast through the patient files to get the NHS number for each patient ID
                Map<String, String> hmNhsNumbers = new HashMap<>();
                Map<String, Date> hmDobs = new HashMap<>();
                findNhsNumbers(exchanges, hmNhsNumbers, hmDobs);
                LOG.debug("Found " + hmNhsNumbers.size() + " NHS numbers");

                int done = 0;
                Set<String> hsIdsDone = new HashSet<>();

                String outputFile = "SD217_bad_imms_" + service.getLocalId() + ".csv";
                PrintWriter fw = new PrintWriter(outputFile);
                BufferedWriter bw = new BufferedWriter(fw);
                CSVFormat format = EmisCsvToFhirTransformer.CSV_FORMAT
                        .withHeader("Name", "ODS Code", "PatientGuid", "PreviousStart", "ChangedStart", "Direction", "PreviousRegType", "ChangedRegType", "RegTypeChanged", "PreviousFile", "ChangedFile"
                        );
                CSVPrinter printer = new CSVPrinter(bw, format);

                //the exchanges are most-recent-first so go through them in order so we're going backwards
                for (Exchange exchange: exchanges) {

                    if (!ExchangeHelper.isAllowRequeueing(exchange)) {
                        continue;
                    }

                    String path = findFilePathInExchange(exchange, "journal_data_extract");
                    if (Strings.isNullOrEmpty(path)) {
                        continue;
                    }

                    String[] headers = new String[]{"nhs_number", "date_of_birth", "patient_id", "journal_id", "imm_date", "imm_code", "imm_term", "imm_status", "included_in_extract"};
                    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headers);

                    InputStreamReader reader = FileHelper.readFileReaderFromSharedStorage(path);
                    CSVParser parser = new CSVParser(reader, csvFormat);
                    Iterator<CSVRecord> iterator = parser.iterator();
                    while (iterator.hasNext()) {
                        CSVRecord record = iterator.next();

                        String id = record.get("ID");
                        String patientId = record.get("PID");
                        String date = record.get("DATE");
                        String code = record.get("CODE");
                        String rubric = record.get("RUBRIC");
                        String status = record.get("IMMS_STATUS");
                        String subset = record.get("SUBSET");
                        String action = record.get("ACTION");

                        //if not a coded entry then not relevant
                        if (Strings.isNullOrEmpty(code)) {
                            continue;
                        }

                        //this is entirely about the imms status column, so skip anything without one
                        if (Strings.isNullOrEmpty(status)) {
                            continue;
                        }

                        //ensure it's an immunisation
                        if (!subset.equalsIgnoreCase("I")) {
                            throw new Exception("ID " + id + " in " + path + " has an imms_status but isn't subset I");
                        }

                        //only do each ID once
                        if (hsIdsDone.contains(id)) {
                            continue;
                        }
                        hsIdsDone.add(id);

                        //if it had been deleted, it's OK
                        if (action.equalsIgnoreCase("D")) {
                            continue;
                        }

                        //if the status says it was given, then it's OK
                        if (status.equalsIgnoreCase("Given")) {
                            continue;
                        }

                        //ensure the status matches the other known values
                        if (!status.equalsIgnoreCase("Advised")
                                && !status.equalsIgnoreCase("Refusal to start or complete course")) {
                            throw new Exception("ID " + id + " in " + path + " has unexpected imms_status [" + status + "]");
                        }

                        //work out if the Read code is part of the childhood imms data set
                        String formattedCode = formatReadCode(code);

                        String nhsNumber = hmNhsNumbers.get(patientId);
                        if (nhsNumber == null) {
                            nhsNumber = "";
                        }

                        String dobStr = "";
                        Date dob = hmDobs.get(patientId);
                        if (dob != null) {
                            dobStr = sqlDateFormat.format(dob);
                        }

                        String immDateStr = "";
                        if (!Strings.isNullOrEmpty(date)) {
                            Date d = visionDateFormat.parse(date);
                            immDateStr = sqlDateFormat.format(d);
                        }

                        boolean includedInExtract = hsDataSetCodes.contains(formattedCode);

                        //"nhs_number", "date_of_birth", "patient_id", "journal_id", "imm_date", "imm_code", "imm_term", "imm_status", "included_in_extract"
                        printer.printRecord(nhsNumber, dobStr, patientId, id, immDateStr, formattedCode, rubric, status, new Boolean(includedInExtract));
                    }

                    parser.close();

                    done ++;
                    if (done % 100 == 0) {
                        LOG.debug("Done " + done);
                    }
                }

                printer.close();

                LOG.debug("Finished " + service + " -> " + outputFile);

            }

            LOG.debug("Finished Finding Vision Patients Affected By Imms Bug for " + odsCodeRegex);

        } catch (Throwable t) {
            LOG.error("", t);
        }

    }


    /**
     * code got off data_extracts DB using:

     select concat('\"', read2_concept_id, '\",')
     from data_extracts.code_set_codes
     where code_set_id = 212
     and ctv3_concept_id = 'read2';

     */
    private static Set<String> getDataSetCodes() {
        String[] codes = new String[]{
                "6572.",
                "65720",
                "653..",
                "654..",
                "6541.",
                "6542.",
                "6543.",
                "6544.",
                "6545.",
                "6551.",
                "6552.",
                "6553.",
                "6554.",
                "6561.",
                "6562.",
                "6563.",
                "6564.",
                "6571.",
                "6581.",
                "6582.",
                "6583.",
                "6584.",
                "65710",
                "65711",
                "65712",
                "65715",
                "65716",
                "65717",
                "65E20",
                "65E21",
                "65E22",
                "654Z.",
                "6571A",
                "657A.",
                "657B.",
                "657C.",
                "657D.",
                "657E.",
                "657F.",
                "657G.",
                "657I.",
                "657J.",
                "657J0",
                "657J1",
                "657J2",
                "657J3",
                "657J4",
                "657L.",
                "657M.",
                "657N.",
                "657P.",
                "657S.",
                "657S0",
                "65A..",
                "65A1.",
                "65A2.",
                "65a0.",
                "65a1.",
                "65a2.",
                "65a3.",
                "65b..",
                "65B..",
                "65b0.",
                "65d0.",
                "65d1.",
                "65ED.",
                "65ED0",
                "65ED1",
                "65ED2",
                "65ED3",
                "65F1.",
                "65F10",
                "65F2.",
                "65F20",
                "65F3.",
                "65F30",
                "65F5.",
                "65F6.",
                "65F60",
                "65FS.",
                "65FT.",
                "65FV.",
                "65H1.",
                "65H2.",
                "65H3.",
                "65H4.",
                "65H5.",
                "65H6.",
                "65H7.",
                "65I1.",
                "65I2.",
                "65I3.",
                "65I4.",
                "65I5.",
                "65I6.",
                "65I7.",
                "65I8.",
                "65I9.",
                "65J1.",
                "65J2.",
                "65J3.",
                "65J4.",
                "65J5.",
                "65J6.",
                "65J7.",
                "65J8.",
                "65J9.",
                "65K1.",
                "65K2.",
                "65K3.",
                "65K4.",
                "65K5.",
                "65K6.",
                "65K7.",
                "65K8.",
                "65K9.",
                "65KA.",
                "65L1.",
                "65L2.",
                "65L3.",
                "65L4.",
                "65M1.",
                "65M2.",
                "65M7.",
                "65M8.",
                "65M9.",
                "65MA.",
                "65MB.",
                "65MC.",
                "65MH.",
                "65MI.",
                "65MJ.",
                "65MK.",
                "65MP.",
                "65MQ.",
                "65O..",
                "65O1.",
                "65O2.",
                "65O3.",
                "65O4.",
                "65O5.",
                "65O6.",
                "65O7.",
                "65O8.",
                "65OZ.",
                "65VH.",
                "9N4c.",
                "9OX5.",
                "9OX50",
                "9OX51",
                "9OX52",
                "9OX53",
                "9OX54",
                "9OX55",
                "9OX56",
                "9OX57",
                "ZV064",
                "65E2.",
                "6524.",
                "65ED6",
                "65FA.",
                "65FG.",
                "65ML.",
                "6511.",
                "6512.",
                "6521.",
                "6522.",
                "6523.",
                "6555.",
                "6565.",
                "65719",
                "9ki1.",
                "652..",
                "652Z.",
                "655..",
                "656..",
                "657R.",
                "658..",
                "65a..",
                "65b1.",
                "65C..",
                "65d..",
                "65D..",
                "65D2.",
                "65d2.",
                "65d20",
                "65d21",
                "65D3.",
                "65D5.",
                "65D6.",
                "65DZ.",
                "65E..",
                "657K.",
                "65E10",
                "65E24",
                "65E30",
                "65ED4",
                "65ED5",
                "65ED7",
                "65ED8",
                "65ED9",
                "65F..",
                "65F11",
                "65F4.",
                "65F7.",
                "65F8.",
                "65Fa.",
                "65FB.",
                "65FD.",
                "65FE.",
                "65FF.",
                "65FH.",
                "65FI.",
                "65FL.",
                "65FN.",
                "65FP.",
                "65FQ.",
                "65FR.",
                "65FW.",
                "65FX0",
                "65FX1",
                "65FX2",
                "65FY.",
                "65H..",
                "65I..",
                "65IZ.",
                "65K..",
                "65M10",
                "65M11",
                "65M3.",
                "65MC0",
                "65MD.",
                "65MD0",
                "65ME.",
                "65ME0",
                "65MF.",
                "65MF0",
                "65MG.",
                "65MM.",
                "65MN.",
                "65MZ1",
                "65MZ2",
                "65MZ3",
                "7L197",
                "65714",
                "ZV032",
                "ZV036",
                "ZV048",
                "ZV063",
                "65MZ.",
                "657J5",
                "65560",
                "65FM.",
                "65D4."
        };

        Set<String> ret = new HashSet<>();
        for (String code: codes) {
            ret.add(code);
        }
        return ret;
    }

    private static String formatReadCode(String code) throws Exception {

        if (code.length() <= 4) {
            //all codes under four chars are invalid Read2 codes and do not fit
            //with Vision local codes so just return them as they are
            return code;

        } else if (code.length() == 5) {
            //if length five, it may be a real Read2 or maybe not, but don't do anything to it
            return code;

        } else if (code.length() == 6) {
            //there are only a small number of six-character codes, and some a legit Read2 codes with an additional
            //single-digit term code, and others look like garbage
            String prefix = code.substring(0, 5);
            if (Read2Cache.isRealRead2Code(code)) {
                return prefix;

            } else {
                //there are only three six-character non-valid Read2 codes that have been found in the first 30 practices
                //so rather than attempt to come up with some algorithm to process them, just let those specific ones through
                //and throw an exception if it's something else
                if (code.equals("#9V0..")
                        || code.equals("9V0.00")
                        || code.equals("asthma")) {
                    return code;

                } else {
                    throw new Exception("Unexpected six-character Vision code [" + code + "]");
                }
            }

        } else if (code.length() == 7) {
            //the vast majority of codes are seven-characters, with a five-letter Read2 code (or local code) and
            //then a two-digit term code
            String prefix = code.substring(0, 5);
            if (Read2Cache.isRealRead2Code(code)) {
                //if the first five characters are a true Read2 code, then use that as the code
                return prefix;
            } else {
                //if not a true Read2 code then validate that the last two digits are a numeric term code
                String suffix = code.substring(5);
                try {
                    //if the suffix is a numeric term code, then this is just a Vision local code, so return the five-character prefix
                    Integer.parseInt(suffix);
                    return prefix;

                } catch (NumberFormatException nfe) {
                    //if the suffix isn't a numeric term code, then the format of the cell is off, and it's not something we
                    //should silently process
                    throw new Exception("Unexpected seven-character Vision code [" + code + "]");
                }
            }

        } else {
            //if any code is longer than seven chars, then this is new to us, so throw an exception so we can investigate
            throw new Exception("Unexpected " + code.length() + " character code [" + code.length() + "]");
        }
    }


    private static void findNhsNumbers(List<Exchange> exchanges, Map<String, String> hmNhsNumbers, Map<String, Date> hmDobs) throws Exception {

        for (Exchange exchange: exchanges) {

            if (!ExchangeHelper.isAllowRequeueing(exchange)) {
                continue;
            }

            String path = findFilePathInExchange(exchange, "patient_data_extract");
            if (Strings.isNullOrEmpty(path)) {
                continue;
            }

            String[] headers = new String[]{"PID", "REFERENCE", "DATE_OF_BIRTH", "SEX", "POSTCODE", "MARITAL_STATUS", "GP", "GP_USUAL", "ACTIVE", "REGISTERED_DATE", "REMOVED_DATE", "HA", "PCG", "SURGERY", "MILEAGE", "DISPENSING", "ETHNIC", "DATE_OF_DEATH", "PRACTICE", "SURNAME", "FORENAME", "TITLE", "NHS_NUMBER", "ADDRESS", "ADDRESS_1", "ADDRESS_2", "ADDRESS_3", "ADDRESS_4", "ADDRESS_5", "PHONE_NUMBER", "MOBILE_NUMBER", "EMAIL", "PRACT_NUMBER", "SERVICE_ID", "ACTION"};
            CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headers);

            InputStreamReader reader = FileHelper.readFileReaderFromSharedStorage(path);
            CSVParser parser = new CSVParser(reader, csvFormat);
            Iterator<CSVRecord> iterator = parser.iterator();
            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String id = record.get("PID");
                String dob = record.get("DATE_OF_BIRTH");
                String nhsNumber = record.get("NHS_NUMBER");

                if (!Strings.isNullOrEmpty(nhsNumber)
                        && !hmNhsNumbers.containsKey(id)) {

                    hmNhsNumbers.put(id, nhsNumber);
                }

                if (!Strings.isNullOrEmpty(dob)
                    && !hmDobs.containsKey(id)) {

                    hmDobs.put(id, visionDateFormat.parse(dob));
                }
            }

            parser.close();
        }
    }
}

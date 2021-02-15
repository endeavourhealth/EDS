package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.endeavourhealth.common.fhir.PeriodHelper;
import org.endeavourhealth.common.fhir.schema.RegistrationType;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.eds.models.PatientSearch;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.hl7.fhir.instance.model.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SD367 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD367.class);

    public static void findEthnicityCodes(String odsCodeRegex, String sourceFile, String dstFile) {
        try {
            LOG.debug("Finding Ethnicity Codes from " + sourceFile + " to " + dstFile);

            Set<String> serviceIds = findServiceIds(odsCodeRegex);
            LOG.debug("Found " + serviceIds + " service IDs");

            List<String> nhsNumbers = findNhsNumbers(sourceFile);
            LOG.debug("Found " + nhsNumbers.size() + " NHS numbers");

            findEthnicityCodesForNhsNumbers(serviceIds, nhsNumbers, dstFile);

            LOG.debug("Finished Finding Ethnicity Codes from " + sourceFile + " to " + dstFile);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static Set<String> findServiceIds(String odsCodeRegex) throws Exception {

        Set<String> ret = new HashSet<>();

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();

        List<Service> services = serviceDal.getAll();
        for (Service service : services) {

            if (!shouldSkipService(service, odsCodeRegex)) {
                ret.add(service.getId().toString());
            }

        }

        return ret;
    }

    private static void findEthnicityCodesForNhsNumbers(Set<String> serviceIds, List<String> nhsNumbers, String dstFile) throws Exception {

        FileOutputStream fos = new FileOutputStream(new File(dstFile));
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bufferedWriter = new BufferedWriter(osw);

        CSVFormat format = CSVFormat.DEFAULT
                .withHeader("nhs_number", "ethnicity_code", "ethnicity_term", "language_code", "language_term", "output_logging"
                );
        CSVPrinter printer = new CSVPrinter(bufferedWriter, format);

        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

        for (String nhsNumber: nhsNumbers) {

            //validate NHS number, inluding the length
            Boolean b = IdentifierHelper.isValidNhsNumber(nhsNumber);
            if (!b.booleanValue()) {
                LOG.error("Non-valid NHS number " + nhsNumber);
                continue;
            }

            //look up on patient search table
            List<PatientSearch> patientSearches = patientSearchDal.searchByNhsNumber(serviceIds, nhsNumber);

            //find just ones for regular/GMS registrations
            List<PatientSearch> regularPatientSearches = new ArrayList<>();
            for (PatientSearch ps: patientSearches) {
                String regTypeStr = ps.getRegistrationTypeCode();
                if (!Strings.isNullOrEmpty(regTypeStr)) {
                    RegistrationType rt = RegistrationType.fromCode(regTypeStr);
                    if (rt == RegistrationType.REGULAR_GMS) {
                        regularPatientSearches.add(ps);
                    }
                }
            }

            if (regularPatientSearches.isEmpty()) {
                printOutput(printer, nhsNumber, null, null, null, null, "No GP record found");
                continue;
            }
            
            //sort by end date to get active first, oldest last
            regularPatientSearches.sort((o1, o2) -> {
                Period p1 = new Period();
                p1.setStart(o1.getRegistrationStart());
                p1.setEnd(o1.getRegistrationEnd());

                Period p2 = new Period();
                p2.setStart(o2.getRegistrationStart());
                p2.setEnd(o2.getRegistrationEnd());

                return PeriodHelper.comparePeriods(p1, p2);
            });

            if (regularPatientSearches.size() > 1) {
                LOG.trace("Found " + regularPatientSearches.size() + " GP records for " + nhsNumber);
            }

            //find an active one
            PatientSearch bestPatientSearch = regularPatientSearches.get(0);

            UUID patientId = bestPatientSearch.getPatientId();
            UUID serviceId = bestPatientSearch.getServiceId();

            findEthnicityCodeForPatient(patientId, serviceId, printer);
        }

        printer.close();

    }

    private static void findEthnicityCodeForPatient(UUID patientId, UUID serviceId, CSVPrinter printer) throws Exception {

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(serviceId, patientId);



        //TODO - indicate if TPP
        //TODO - indicate if bad Emis or Vision


    }

    private static void printOutput(CSVPrinter printer, String nhsNumber, String ethnicytCode, String ethnicityTerm, String languageCode, String languageTerm, String comments) throws Exception {
        printer.printRecord(nhsNumber, ethnicytCode, ethnicityTerm, languageCode, languageTerm, comments);
    }

    private static List<String> findNhsNumbers(String sourceFile) throws Exception {

        List<String> ret = new ArrayList<>();

        Reader reader = Files.newBufferedReader(Paths.get(sourceFile));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

        Iterator<CSVRecord> iterator = csvParser.iterator();
        while (iterator.hasNext()) {
            CSVRecord record = iterator.next();
            String nhsNumber = record.get("nhs_number");
            ret.add(nhsNumber);
        }

        csvParser.close();

        return ret;
    }

}
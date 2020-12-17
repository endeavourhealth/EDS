package org.endeavourhealth.queuereader.routines;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.terminology.Read2Code;
import org.endeavourhealth.core.terminology.SnomedCode;
import org.endeavourhealth.core.terminology.TerminologyService;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.enterprise.ObservationCodeHelper;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SD81 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD81.class);

    public static void countVaccinationCodes(String sinceDateStr, String ccgOdsCodes) {
        LOG.debug("Counting VaccinationCodes at " + ccgOdsCodes);
        try {

            Date cutoff = new SimpleDateFormat("yyyy-MM-dd").parse(sinceDateStr);
            LOG.debug("Counting vaccinations since " + sinceDateStr);

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();


            Map<String, AtomicInteger> emisResults = new HashMap<>();
            Map<String, AtomicInteger> tppResults = new HashMap<>();
            Map<String, AtomicInteger> visionResults = new HashMap<>();


            for (Service service: services) {

                if (shouldSkipService(service, ccgOdsCodes)) {
                    continue;
                }

                LOG.debug("Doing " + service);

                Map<String, AtomicInteger> hmResults = null;
                if (service.getTags() == null) {
                    LOG.warn("No tags set");
                    continue;
                } else if (service.getTags().containsKey("TPP")) {
                    hmResults = tppResults;
                } else if (service.getTags().containsKey("EMIS")) {
                    hmResults = emisResults;
                } else if (service.getTags().containsKey("Vision")) {
                    hmResults = visionResults;
                } else {
                    LOG.error("Unknown system type");
                    continue;
                    //throw new Exception();
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.YEAR, -20);
                Date d = cal.getTime();
                //String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(d);

                List<UUID> patientIds = new ArrayList<>();

                String sql = "SELECT patient_id FROM patient_search WHERE dt_deleted IS NULL AND date_of_birth > ? AND service_id = ?";
                Connection connection = ConnectionManager.getEdsConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setTimestamp(1, new java.sql.Timestamp(d.getTime()));
                ps.setString(2, service.getId().toString());

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String s = rs.getString(1);
                    patientIds.add(UUID.fromString(s));
                }

                ps.close();
                connection.close();

                LOG.debug("Found " + patientIds.size() + " patient IDs");

                int done = 0;
                for (UUID patientId: patientIds) {

                    ResourceDalI resourceDal = DalProvider.factoryResourceDal();
                    List<ResourceWrapper> resources = resourceDal.getResourcesByPatient(service.getId(), patientId, ResourceType.Immunization.toString());
                    for (ResourceWrapper resourceWrapper: resources) {
                        Immunization imm = (Immunization)resourceWrapper.getResource();

                        if (!imm.hasDateElement()) {
                            continue;
                        }
                        DateTimeType dtVal = imm.getDateElement();
                        Date dt = dtVal.getValue();
                        if (dt.before(cutoff)) {
                            continue;
                        }

                        if (imm.hasVaccineCode()) {
                            CodeableConcept cc = imm.getVaccineCode();
                            ObservationCodeHelper codes = ObservationCodeHelper.extractCodeFields(cc);

                            Long snomedConceptId = codes.getSnomedConceptId();
                            String originalCode = codes.getOriginalCode();
                            String originalTerm = codes.getOriginalTerm();

                            //there are Vision immunizations with a Snomed code only
                            if (originalCode == null) {
                                originalCode = "NULL";
                            }

                            if (originalTerm == null) {
                                originalTerm = "NULL";
                            }

                            String snomedConceptIdStr;
                            if (snomedConceptId != null) {
                                snomedConceptIdStr = "" + snomedConceptId;
                            } else {
                                snomedConceptIdStr = "NULL";
                            }

                            String cacheKey = originalCode + "|" + originalTerm + "|" + snomedConceptIdStr;
                            AtomicInteger count = hmResults.get(cacheKey);
                            if (count == null) {
                                count = new AtomicInteger(0);
                                hmResults.put(cacheKey, count);
                            }
                            count.incrementAndGet();
                        }
                    }

                    done ++;
                    if (done % 100 == 0) {
                        LOG.debug("Done " + done);
                    }
                }
                LOG.debug("Finished " + done);
            }

            LOG.debug("Writing results");

            List<String> fileNames = new ArrayList<>();
            fileNames.add("Immunisation_Codes_TPP.csv");
            fileNames.add("Immunisation_Codes_Emis.csv");
            fileNames.add("Immunisation_Codes_Vision.csv");

            for (String fileName: fileNames) {

                Map<String, AtomicInteger> hmResults = null;
                String localScheme = null;
                if (fileName.equals("Immunisation_Codes_TPP.csv")) {
                    hmResults = tppResults;
                    localScheme = "TPP local";
                } else if (fileName.equals("Immunisation_Codes_Emis.csv")) {
                    hmResults = emisResults;
                    localScheme = "EMIS local";
                } else if (fileName.equals("Immunisation_Codes_Vision.csv")) {
                    hmResults = visionResults;
                    localScheme = "Vision local";
                } else {
                    throw new Exception("Unknown file name " + fileName);
                }

                //find max count
                Map<Integer, List<String>> hmByCount = new HashMap<>();
                int max = 0;

                for (String key: hmResults.keySet()) {
                    AtomicInteger a = hmResults.get(key);
                    int count = a.get();

                    List<String> l = hmByCount.get(new Integer(count));
                    if (l == null) {
                        l = new ArrayList<>();
                        hmByCount.put(new Integer(count), l);
                    }
                    l.add(key);

                    max = Math.max(max, count);
                }

                File dstFile = new File(fileName);
                FileOutputStream fos = new FileOutputStream(dstFile);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                BufferedWriter bufferedWriter = new BufferedWriter(osw);

                CSVFormat format = EmisCsvToFhirTransformer.CSV_FORMAT
                        .withHeader("Code Scheme", "Code", "Term", "Mapped Snomed Concept", "Mapped Snomed Term", "Count"
                        );
                CSVPrinter printer = new CSVPrinter(bufferedWriter, format);

                for (int i=max; i>=0; i--) {
                    List<String> l = hmByCount.get(new Integer(i));
                    if (l == null) {
                        continue;
                    }

                    for (String key: l) {
                        String[] toks = key.split("|");
                        String originalCode = toks[0];

                        String originalTerm = "NULL";
                        String snomedConceptId = "NULL";
                        String snomedTerm = "NULL";

                        if (toks.length > 1) {
                            originalTerm = toks[1];
                        }

                        if (toks.length > 2) {
                            snomedConceptId = toks[2];

                            SnomedCode snomedCode = TerminologyService.lookupSnomedFromConceptId(snomedConceptId);
                            if (snomedCode != null) {
                                snomedTerm = snomedCode.getTerm();
                            }
                        }

                        String codeScheme = null;

                        if (originalCode.startsWith("CTV3_")) {
                            originalCode = originalCode.substring(5);
                            if (originalCode.startsWith("Y")) {
                                codeScheme = localScheme;
                            } else {
                                codeScheme = "CTV3";
                            }

                        } else {
                            Read2Code dbCode = TerminologyService.lookupRead2Code(originalCode);
                            if (dbCode == null) {
                                codeScheme = localScheme;
                            } else {
                                codeScheme = "Read2";
                            }
                        }

                        printer.printRecord(codeScheme, originalCode, originalTerm, snomedConceptId, snomedTerm, new Integer(i));
                        //String cacheKey = originalCode + "|" + originalTerm + "|" + snomedConceptId;
                    }
                }

                printer.close();
            }

            LOG.debug("Finished Counting VaccinationCodes at " + ccgOdsCodes);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }
}

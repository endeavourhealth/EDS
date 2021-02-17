package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirCodeUri;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.endeavourhealth.common.fhir.PeriodHelper;
import org.endeavourhealth.common.fhir.schema.EthnicCategory;
import org.endeavourhealth.common.fhir.schema.RegistrationType;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.eds.models.PatientSearch;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.publisherCommon.models.EmisClinicalCode;
import org.endeavourhealth.transform.emis.csv.helpers.EmisMappingHelper;
import org.endeavourhealth.transform.tpp.csv.helpers.TppMappingHelper;
import org.endeavourhealth.transform.vision.helpers.VisionMappingHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SD367 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD367.class);

    private static Set<Long> hsSnomedLanguageCodes = null;
    private static Set<String> hsTppLanguageCodes = null;
    private static Set<String> hsEmisLanguageCodes = null;
    private static Set<String> hsVisionLanguageCodes = null;

    public static void findEthnicityCodes(String odsCodeRegex, String sourceFile, String dstFile) {
        try {
            LOG.debug("Finding Ethnicity Codes from " + sourceFile + " to " + dstFile);

            Set<String> serviceIds = findServiceIds(odsCodeRegex);
            LOG.debug("Found " + serviceIds.size() + " service IDs");

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
                .withHeader("nhs_number", "raw_ethnicity_code", "raw_ethnicity_term", "data_dictionary_ethnicity_code", "data_dictionary_ethnicity_term", "raw_language_code", "raw_language_term", "snomed_language_concept_id", "snomed_language_term", "comment"
                );
        CSVPrinter printer = new CSVPrinter(bufferedWriter, format);

        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

        int done = 0;

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
                printer.printRecord(nhsNumber, null, null, null, null, null, null, null, null, "No GP record found in DDS cohort");
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

            String comments = null;
            if (bestPatientSearch.getRegistrationEnd() != null) {
                comments = "Using deducted GP record";

            } else {
                comments = "Using active GP record";
            }

            findEthnicityCodeForPatient(nhsNumber, patientId, serviceId, printer, comments);

            done ++;
            if (done % 100 == 0) {
                LOG.debug("Done " + done + " patients");
            }
        }
        LOG.debug("Finished " + done + " patients");

        printer.close();

    }

    private static void findEthnicityCodeForPatient(String nhsNumber, UUID patientId, UUID serviceId, CSVPrinter printer, String comments) throws Exception {

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(serviceId, patientId);
        
        CodeableConcept latestEthnicity = null;
        Date latestEthnicityDate = null;
        CodeableConcept latestLanguage = null;
        Date latestLanguageDate = null;
        
        for (ResourceWrapper wrapper: wrappers) {
            Resource resource = wrapper.getResource();

            //skip resource types that aren't interesting for this
            if (resource.getResourceType() == ResourceType.Appointment
                    || resource.getResourceType() == ResourceType.Encounter
                    || resource.getResourceType() == ResourceType.Patient
                    || resource.getResourceType() == ResourceType.EpisodeOfCare
                    || resource.getResourceType() == ResourceType.Immunization
                    || resource.getResourceType() == ResourceType.MedicationStatement
                    || resource.getResourceType() == ResourceType.MedicationOrder) {
                continue;
            }

            try {
                CodeableConcept codeableConcept = CodeableConceptHelper.findMainCodeableConcept(resource);
                Date effectiveDate = CodeableConceptHelper.findMainEffectiveDate(resource);

                if (codeableConcept == null) {
                    LOG.warn("No codeable concept found for " + resource.getResourceType() + " " + resource.getId() + " for patient " + patientId);
                    continue;
                }

                Coding coding = CodeableConceptHelper.findOriginalCoding(codeableConcept);
                if (coding == null) {
                    LOG.warn("No original coding found for " + resource.getResourceType() + " " + resource.getId() + " for patient " + patientId);
                    continue;
                }

                if (isEthnicityCode(coding, resource, patientId)) {
                    if (latestEthnicityDate == null
                            || (effectiveDate != null && effectiveDate.after(latestEthnicityDate))) {

                        latestEthnicity = codeableConcept;
                        latestEthnicityDate = effectiveDate;
                        LOG.debug("Patient " + patientId + " found ethnicity in " + resource.getResourceType() + " " + resource.getId() + " with date " + effectiveDate);
                    }
                }
                
                if (isLanguageCode(codeableConcept)) {
                    if (latestLanguageDate == null ||
                            (effectiveDate != null && effectiveDate.after(latestLanguageDate))) {

                        latestLanguage = codeableConcept;
                        latestLanguageDate = effectiveDate;
                        LOG.debug("Patient " + patientId + " found language in " + resource.getResourceType() + " " + resource.getId() + " with date " + effectiveDate);
                    }
                }

            } catch (IllegalArgumentException iae) {
                LOG.debug("Skipping resource type " + resource.getResourceType());
                
            }
        }

        String rawEthnicityCode = null;
        String rawEthnicityTerm = null;
        String ddEthnicityCode = null;
        String ddEthnicityTerm = null;
        String rawLanguageCode = null;
        String rawLanguageTerm = null;
        String snomedLanguageCode = null;
        String snomedLanguageTerm = null;

        if (latestEthnicity != null) {
            Coding coding = CodeableConceptHelper.findOriginalCoding(latestEthnicity);
            rawEthnicityCode = coding.getCode();
            rawEthnicityTerm = coding.getDisplay();

            EthnicCategory ec = findEthnicCategory(coding);
            if (ec == null) {
                ddEthnicityCode = "";
                ddEthnicityTerm = "";
            } else {
                ddEthnicityCode = ec.getCode();
                ddEthnicityTerm = ec.getDescription();
            }
        }

        if (latestLanguage != null) {
            Coding coding = CodeableConceptHelper.findOriginalCoding(latestLanguage);
            rawLanguageCode = coding.getCode();
            rawLanguageTerm = coding.getDisplay();

            snomedLanguageCode = "" + CodeableConceptHelper.findSnomedConceptId(latestLanguage);
            if (Strings.isNullOrEmpty(snomedLanguageCode)) {
                LOG.warn("Failed to find snomed concept from language for patient " + patientId);
                snomedLanguageCode = "";
            }
            snomedLanguageTerm = CodeableConceptHelper.findSnomedConceptText(latestLanguage);
            if (Strings.isNullOrEmpty(snomedLanguageTerm)) {
                snomedLanguageTerm = "";
            }
        }

        if (comments == null) {
            comments = "";
        }

        printer.printRecord(nhsNumber, rawEthnicityCode, rawEthnicityTerm, ddEthnicityCode, ddEthnicityTerm, rawLanguageCode, rawLanguageTerm, snomedLanguageCode, snomedLanguageTerm, comments);
    }

    private static boolean isLanguageCode(CodeableConcept codeableConcept) {

        //check against the Snomed refset first, as that's simpler
        Long snomedConceptId = CodeableConceptHelper.findSnomedConceptId(codeableConcept);
        if (snomedConceptId == null) {
            if (isSnomedLanguageCode(snomedConceptId)) {
                return true;
            }
        }

        Coding originalCoding = CodeableConceptHelper.findOriginalCoding(codeableConcept);
        String system = originalCoding.getSystem();
        String code = originalCoding.getCode();

        if (system.equals(FhirCodeUri.CODE_SYSTEM_READ2)
                || system.equals(FhirCodeUri.CODE_SYSTEM_EMIS_CODE)) {

            if (isEmisLanguageCode(code)) {
                return true;
            }

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_CTV3)
                || system.equals(FhirCodeUri.CODE_SYSTEM_TPP_CTV3)) {

            if (isTppLanguageCode(code)) {
                return true;
            }

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_VISION_CODE)) {

            if (isVisionLanguageCode(code)) {
                return true;
            }

        } else {
            LOG.error("Unexpected code system " + system);
        }

        return false;
    }

    private static boolean isVisionLanguageCode(String code) {

        if (hsVisionLanguageCodes == null) {

            Set<String> hs = new HashSet<>();

            hs.add("13Z6."); //Language spoken
            hs.add("13Z60"); //English as a second language
            hs.add("13Z61"); //Language Bengali
            hs.add("13Z62"); //Language Gujurati
            hs.add("13Z63"); //Language Hindi
            hs.add("13Z64"); //Language Pashtu
            hs.add("13Z65"); //Language Punjabi
            hs.add("13Z66"); //Language Urdu
            hs.add("13Z67"); //Speaks English well
            hs.add("13Z68"); //Speaks English poorly
            hs.add("13Z69"); //First language not English
            hs.add("13Z6Z"); //Language NOS
            hs.add("13ZM."); //Using British sign language
            hs.add("13ZP."); //Using Makaton sign language
            hs.add("13b.."); //World languages
            hs.add("13b0."); //Vietnamese language
            hs.add("13b1."); //Cantonese Chinese dialect
            hs.add("13b3."); //Creole language
            hs.add("13l.."); //Main spoken language
            hs.add("13l0."); //Main spoken language Arabic
            hs.add("13l1."); //Main spoken language Bengali
            hs.add("13l2."); //Main spoken language Cantonese
            hs.add("13l3."); //Main spoken language Czech
            hs.add("13l4."); //Main spoken language English
            hs.add("13l5."); //Main spoken language French
            hs.add("13l6."); //Main spoken language Gujerati
            hs.add("13l7."); //Main spoken language Hausa
            hs.add("13l8."); //Main spoken language Hindi
            hs.add("13l9."); //Main spoken language Iba
            hs.add("13lA."); //Main spoken language Kutchi
            hs.add("13lB."); //Main spoken language Mandarin
            hs.add("13lC."); //Main spoken language Polish
            hs.add("13lD."); //Main spoken language Portuguese
            hs.add("13lE."); //Main spoken language Punjabi
            hs.add("13lF."); //Main spoken language Russian
            hs.add("13lG."); //Main spoken language Somali
            hs.add("13lH."); //Main spoken language Spanish
            hs.add("13lI."); //Main spoken language Swahili
            hs.add("13lJ."); //Main spoken language Sylheti
            hs.add("13lK."); //Main spoken language Tamil
            hs.add("13lL."); //Main spoken language Urdu
            hs.add("13lM."); //Main spoken language Yoruba
            hs.add("13lN."); //Main spoken language Kurdish
            hs.add("13lO."); //Main spoken language Farsi
            hs.add("13lP."); //Main spoken language Shona
            hs.add("13lQ."); //Main spoken language Italian
            hs.add("13lR."); //Main spoken language German
            hs.add("13lS."); //Main spoken language Albanian
            hs.add("13lT."); //Main spoken language Croatian
            hs.add("13lV."); //Main spoken language Greek
            hs.add("13lW."); //Main spoken language Japanese
            hs.add("13lX."); //Main spoken language Korean
            hs.add("13lY."); //Main spoken language Lithuanian
            hs.add("13lZ."); //Main spoken language Turkish
            hs.add("13la."); //Main spoken language Ukrainian
            hs.add("13lb."); //Main spoken language Vietnamese
            hs.add("13lc."); //Main spoken language Akan
            hs.add("13ld."); //Main spoken language Amharic
            hs.add("13lf."); //Main spoken language Dutch
            hs.add("13lg."); //Main spoken language Ethiopian
            hs.add("13lh."); //Main spoken language Flemish
            hs.add("13li."); //Main spoken language French Creole
            hs.add("13lj."); //Main spoken language Gaelic
            hs.add("13lk."); //Main spoken language Hakka
            hs.add("13ll."); //Main spoken language Hebrew
            hs.add("13lm."); //Main spoken language Igbo
            hs.add("13ln."); //Main spoken language Lingala
            hs.add("13lo."); //Main spoken language Luganda
            hs.add("13lp."); //Main spoken language Malayalam
            hs.add("13lq."); //Main spoken language Norwegian
            hs.add("13lr."); //Main spoken language Pashto
            hs.add("13ls."); //Main spoken language Patois
            hs.add("13lt."); //Main spoken language Serbian
            hs.add("13lu."); //Main spoken language Sinhala
            hs.add("13lv."); //Main spoken language Swedish
            hs.add("13lw."); //Main spoken language Tagalog
            hs.add("13lx."); //Main spoken language Thai
            hs.add("13ly."); //Main spoken language Tigrinya
            hs.add("13lz."); //Main spoken language Welsh
            hs.add("13u.."); //Additional main spoken language
            hs.add("13u0."); //Main spoken language Bulgarian
            hs.add("13u1."); //Main spoken language Romanian
            hs.add("13u2."); //Main spoken language Oromo
            hs.add("13u3."); //Main spoken language Abkhazian
            hs.add("13u4."); //Main spoken language Afar
            hs.add("13u5."); //Main spoken language Afrikaans
            hs.add("13u6."); //Main spoken language Armenian
            hs.add("13u9."); //Main spoken language Azerbaijani
            hs.add("13uC."); //Main spoken language Dzongkha
            hs.add("13uE."); //Main spoken language Bislama
            hs.add("13uG."); //Main spoken language Burmese
            hs.add("13uH."); //Main spoken language Belarusian
            hs.add("13uK."); //Main spoken language Catalan
            hs.add("13uL."); //Main spoken language Slovak
            hs.add("13uM."); //Main spoken language Corsican
            hs.add("13uN."); //Main spoken language Danish
            hs.add("13uP."); //Main spoken language Esperanto
            hs.add("13uQ."); //Main spoken language Estonian
            hs.add("13uS."); //Main spoken language Fijian
            hs.add("13uT."); //Main spoken language Finnish
            hs.add("13uV."); //Main spoken language Frisian
            hs.add("13uW."); //Main spoken language Galician
            hs.add("13uX."); //Main spoken language Georgian
            hs.add("13uY."); //Main spoken language Kalaallisut
            hs.add("13uZ."); //Main spoken language Guarani
            hs.add("13ua."); //Main spoken language Hungarian
            hs.add("13ub."); //Main spoken language Icelandic
            hs.add("13uc."); //Main spoken language Indonesian
            hs.add("13uh."); //Main spoken language Irish
            hs.add("13ui."); //Main spoken language Javanese
            hs.add("13uj."); //Main spoken language Kannada
            hs.add("13uk."); //Main spoken language Kashmiri
            hs.add("13ul."); //Main spoken language Kazakh
            hs.add("13um."); //Main spoken language Kinyarwanda
            hs.add("13uo."); //Main spoken language Rundi
            hs.add("13up."); //Main spoken language Lao
            hs.add("13ur."); //Main spoken language Latvian
            hs.add("13us."); //Main spoken language Macedonian
            hs.add("13uu."); //Main spoken language Malay
            hs.add("13uv."); //Main spoken language Maltese
            hs.add("13ux."); //Main spoken language Marathi
            hs.add("13uy."); //Main spoken language Moldavian
            hs.add("13uz."); //Main spoken language Mongolian
            hs.add("13w.."); //Supplemental main language spoken
            hs.add("13w1."); //Main spoken language Nepali
            hs.add("13w2."); //Main spoken language Occitan
            hs.add("13w3."); //Main spoken language Oriya
            hs.add("13w4."); //Main spoken language Filipino
            hs.add("13w5."); //Main spoken language Quechua
            hs.add("13w6."); //Main spoken language Romansh
            hs.add("13w7."); //Main spoken language Samoan
            hs.add("13w8."); //Main spoken language Sango
            hs.add("13w9."); //Main spoken language Hindko
            hs.add("13wA."); //Main spoken language Dari
            hs.add("13wD."); //Main spoken language Sindhi
            hs.add("13wE."); //Main spoken language Ndebele
            hs.add("13wG."); //Main spoken language Slovenian
            hs.add("13wH."); //Main spoken language Sundanese
            hs.add("13wJ."); //Main spoken language Tajik
            hs.add("13wL."); //Main spoken language Telugu
            hs.add("13wM."); //Main spoken language Tibetan
            hs.add("13wN."); //Main spoken language Tongan
            hs.add("13wR."); //Main spoken language Twi
            hs.add("13wS."); //Main spoken language Uighur
            hs.add("13wT."); //Main spoken language Uzbek
            hs.add("13wW."); //Main spoken language Wolof
            hs.add("13wY."); //Main spoken language Yiddish
            hs.add("13wZ."); //Main spoken language Zhuang
            hs.add("13wa."); //Main spoken language Zulu
            hs.add("13wb."); //Main spoken language Konkani
            hs.add("13wd."); //Main spoken language Romany

            hsVisionLanguageCodes = hs;
        }

        return hsVisionLanguageCodes.contains(code);
    }

    private static boolean isEmisLanguageCode(String code) {

        if (hsEmisLanguageCodes == null) {

            Set<String> hs = new HashSet<>();

            hs.add("13Z6."); //Language spoken
            hs.add("13Z6."); //Language
            hs.add("13ZM."); //Using British sign language
            hs.add("13ZP."); //Using Makaton sign language
            hs.add("13b.."); //World languages
            hs.add("13b0."); //Vietnamese language
            hs.add("13b1."); //Cantonese Chinese dialect
            hs.add("13b2."); //Sylhety
            hs.add("13b4."); //Mirpuri language
            hs.add("13l.."); //Main spoken language
            hs.add("13l0."); //Main spoken language Arabic
            hs.add("13l1."); //Main spoken language Bengali
            hs.add("13l2."); //Main spoken language Cantonese
            hs.add("13l3."); //Main spoken language Czech
            hs.add("13l4."); //Main spoken language English
            hs.add("13l5."); //Main spoken language French
            hs.add("13l6."); //Main spoken language Gujarati
            hs.add("13l6."); //Main spoken language Gujerati
            hs.add("13l7."); //Main spoken language Hausa
            hs.add("13l8."); //Main spoken language Hindi
            hs.add("13l9."); //Main spoken language Iba
            hs.add("13l9."); //Main spoken language Iban
            hs.add("13lA."); //Main spoken language Kutchi
            hs.add("13lB."); //Main spoken language Mandarin
            hs.add("13lC."); //Main spoken language Polish
            hs.add("13lD."); //Main spoken language Portuguese
            hs.add("13lE."); //Main spoken language Punjabi
            hs.add("13lE."); //Main spoken language Panjabi
            hs.add("13lF."); //Main spoken language Russian
            hs.add("13lG."); //Main spoken language Somali
            hs.add("13lH."); //Main spoken language Spanish
            hs.add("13lI."); //Main spoken language Swahili
            hs.add("13lJ."); //Main spoken language Sylheti
            hs.add("13lK."); //Main spoken language Tamil
            hs.add("13lL."); //Main spoken language Urdu
            hs.add("13lM."); //Main spoken language Yoruba
            hs.add("13lN."); //Main spoken language Kurdish
            hs.add("13lO."); //Main spoken language Farsi
            hs.add("13lO."); //Main spoken language Persian
            hs.add("13lP."); //Main spoken language Shona
            hs.add("13lQ."); //Main spoken language Italian
            hs.add("13lR."); //Main spoken language German
            hs.add("13lS."); //Main spoken language Albanian
            hs.add("13lT."); //Main spoken language Serbo-Croatian
            hs.add("13lT."); //Main spoken language Croatian
            hs.add("13lV."); //Main spoken language Greek
            hs.add("13lW."); //Main spoken language Japanese
            hs.add("13lX."); //Main spoken language Korean
            hs.add("13lY."); //Main spoken language Lithuanian
            hs.add("13lZ."); //Main spoken language Turkish
            hs.add("13la."); //Main spoken language Ukrainian
            hs.add("13lb."); //Main spoken language Vietnamese
            hs.add("13lc."); //Main spoken language Akan
            hs.add("13ld."); //Main spoken language Amharic
            hs.add("13le."); //Main spoken language Brawa
            hs.add("13lf."); //Main spoken language Dutch
            hs.add("13lg."); //Main spoken language Ethiopian
            hs.add("13lh."); //Main spoken language Flemish
            hs.add("13lj."); //Main spoken language Gaelic
            hs.add("13lk."); //Main spoken language Hakka
            hs.add("13ll."); //Main spoken language Hebrew
            hs.add("13lm."); //Main spoken language Igbo
            hs.add("13ln."); //Main spoken language Lingala
            hs.add("13lo."); //Main spoken language Luganda
            hs.add("13lp."); //Main spoken language Malayalam
            hs.add("13lq."); //Main spoken language Norwegian
            hs.add("13lr."); //Main spoken language Pashto
            hs.add("13ls."); //Main spoken language Patois
            hs.add("13lt."); //Main spoken language Serbian
            hs.add("13lt."); //Main spoken language Serbo-Croatian
            hs.add("13lu."); //Main spoken language Sinhala
            hs.add("13lu."); //Main spoken language Sinhalese
            hs.add("13lv."); //Main spoken language Swedish
            hs.add("13lw."); //Main spoken language Tagalog
            hs.add("13lx."); //Main spoken language Thai
            hs.add("13ly."); //Main spoken language Tigrinya
            hs.add("13lz."); //Main spoken language Welsh
            hs.add("13s.."); //Second language
            hs.add("13u.."); //Additional main spoken language
            hs.add("13u0."); //Main spoken language Bulgarian
            hs.add("13u1."); //Main spoken language Romanian
            hs.add("13u2."); //Main spoken language Oromo
            hs.add("13u4."); //Main spoken language Afar
            hs.add("13u5."); //Main spoken language Afrikaans
            hs.add("13u6."); //Main spoken language Armenian
            hs.add("13u7."); //Main spoken language Assamese
            hs.add("13u8."); //Main spoken language Aymara
            hs.add("13u9."); //Main spoken language Azerbaijani
            hs.add("13uA."); //Main spoken language Bashkir
            hs.add("13uB."); //Main spoken language Basque
            hs.add("13uC."); //Main spoken language Dzongkha
            hs.add("13uD."); //Main spoken language Bihari
            hs.add("13uE."); //Main spoken language Bislama
            hs.add("13uF."); //Main spoken language Breton
            hs.add("13uG."); //Main spoken language Burmese
            hs.add("13uH."); //Main spoken language Belarusian
            hs.add("13uJ."); //Main spoken language Central Khmer
            hs.add("13uK."); //Main spoken language Catalan
            hs.add("13uL."); //Main spoken language Slovak
            hs.add("13uM."); //Main spoken language Corsican
            hs.add("13uN."); //Main spoken language Danish
            hs.add("13uP."); //Main spoken language Esperanto
            hs.add("13uQ."); //Main spoken language Estonian
            hs.add("13uS."); //Main spoken language Fijian
            hs.add("13uT."); //Main spoken language Finnish
            hs.add("13uV."); //Main spoken language Frisian
            hs.add("13uW."); //Main spoken language Galician
            hs.add("13uX."); //Main spoken language Georgian
            hs.add("13uY."); //Main spoken language Greenlandic
            hs.add("13uY."); //Main spoken language Kalaallisut
            hs.add("13uZ."); //Main spoken language Guarani
            hs.add("13ua."); //Main spoken language Hungarian
            hs.add("13ub."); //Main spoken language Icelandic
            hs.add("13uc."); //Main spoken language Indonesian
            hs.add("13ud."); //Main spoken language Interlingua
            hs.add("13ue."); //Main spoken language Interlingue
            hs.add("13uf."); //Main spoken language Inupiaq
            hs.add("13ug."); //Main spoken language Inuktitut
            hs.add("13uh."); //Main spoken language Irish
            hs.add("13ui."); //Main spoken language Javanese
            hs.add("13uj."); //Main spoken language Kannada
            hs.add("13uk."); //Main spoken language Kashmiri
            hs.add("13ul."); //Main spoken language Kazakh
            hs.add("13um."); //Main spoken language Kinyarwanda
            hs.add("13un."); //Main spoken language Kirghiz
            hs.add("13uo."); //Main spoken language Rundi
            hs.add("13up."); //Main spoken language Lao
            hs.add("13uq."); //Main spoken language Bamun
            hs.add("13uq."); //Main spoken language Bamoun
            hs.add("13ur."); //Main spoken language Latvian
            hs.add("13us."); //Main spoken language Macedonian
            hs.add("13ut."); //Main spoken language Malagasy
            hs.add("13uu."); //Main spoken language Malay
            hs.add("13uv."); //Main spoken language Maltese
            hs.add("13uw."); //Main spoken language Maori
            hs.add("13ux."); //Main spoken language Marathi
            hs.add("13uy."); //Main spoken language Moldavian
            hs.add("13uz."); //Main spoken language Mongolian
            hs.add("13w.."); //Supplemental main language spoken
            hs.add("13w0."); //Main spoken language Nauru
            hs.add("13w1."); //Main spoken language Nepali
            hs.add("13w2."); //Main spoken language Occitan
            hs.add("13w3."); //Main spoken language Oriya
            hs.add("13w4."); //Main spoken language Filipino
            hs.add("13w5."); //Main spoken language Quechua
            hs.add("13w6."); //Main spoken language Romansh
            hs.add("13w7."); //Main spoken language Samoan
            hs.add("13w8."); //Main spoken language Sango
            hs.add("13w9."); //Main spoken language Hindko
            hs.add("13wA."); //Main spoken language Dari
            hs.add("13wB."); //Main spoken language Southern Sotho
            hs.add("13wC."); //Main spoken language Tswana
            hs.add("13wD."); //Main spoken language Sindhi
            hs.add("13wE."); //Main spoken language Ndebele
            hs.add("13wF."); //Main spoken language Swati
            hs.add("13wG."); //Main spoken language Slovenian
            hs.add("13wH."); //Main spoken language Sundanese
            hs.add("13wJ."); //Main spoken language Tajik
            hs.add("13wK."); //Main spoken language Tatar
            hs.add("13wL."); //Main spoken language Telugu
            hs.add("13wM."); //Main spoken language Tibetan
            hs.add("13wN."); //Main spoken language Tongan
            hs.add("13wP."); //Main spoken language Tsonga
            hs.add("13wQ."); //Main spoken language Turkmen
            hs.add("13wR."); //Main spoken language Twi
            hs.add("13wS."); //Main spoken language Uighur
            hs.add("13wT."); //Main spoken language Uzbek
            hs.add("13wV."); //Main spoken language Tetum
            hs.add("13wW."); //Main spoken language Wolof
            hs.add("13wX."); //Main spoken language Xhosa
            hs.add("13wY."); //Main spoken language Yiddish
            hs.add("13wZ."); //Main spoken language Zhuang
            hs.add("13wa."); //Main spoken language Zulu
            hs.add("13wb."); //Main spoken language Konkani
            hs.add("13wc."); //Main spoken language Aragonese
            hs.add("13wd."); //Main spoken language Romsky
            hs.add("13wd."); //Main spoken language Romanesa
            hs.add("13wd."); //Main spoken language Romanes
            hs.add("13wd."); //Main spoken language Romany
            hs.add("13wd."); //Main spoken language Romani
            hs.add("EMISNQAK1"); //Akan as a second language
            hs.add("EMISNQAL1"); //Albanian as a second language
            hs.add("EMISNQAM1"); //Amharic as a second language
            hs.add("EMISNQAR3"); //Arabic as a second language
            hs.add("EMISNQBA9"); //Main spoken language Konkani
            hs.add("EMISNQBE2"); //Bengali as a second language
            hs.add("EMISNQBR9"); //Brawa as a second language
            hs.add("EMISNQCA39"); //Cantonese as a second language
            hs.add("EMISNQCR2"); //Croatian as a second language
            hs.add("EMISNQCZ1"); //Czech as a second language
            hs.add("EMISNQDU1"); //Dutch as a second language
            hs.add("EMISNQET1"); //Ethiopian as a second language
            hs.add("EMISNQFL2"); //Flemish as a second language
            hs.add("EMISNQFR3"); //French as a second language
            hs.add("EMISNQFR4"); //French Creole as a second language
            hs.add("EMISNQGA1"); //Gaelic as a second language
            hs.add("EMISNQGE19"); //German as a second language
            hs.add("EMISNQGR3"); //Greek as a second language
            hs.add("EMISNQGU1"); //Gujerati as a second language
            hs.add("EMISNQHA4"); //Hausa as a second language
            hs.add("EMISNQHA5"); //Hakka as a second language
            hs.add("EMISNQHE10"); //Hebrew as a second language
            hs.add("EMISNQHI2"); //Hindi as a second language
            hs.add("EMISNQIB2"); //Iba as a second language
            hs.add("EMISNQIG1"); //Igbo as a second language
            hs.add("EMISNQIT1"); //Italian as a second language
            hs.add("EMISNQJA3"); //Japanese as a second language
            hs.add("EMISNQKO1"); //Korean as a second language
            hs.add("EMISNQKU1"); //Kutchi as a second language
            hs.add("EMISNQKU2"); //Kurdish as a second language
            hs.add("EMISNQLI3"); //Lingala as a second language
            hs.add("EMISNQLI4"); //Lithuanian as a second language
            hs.add("EMISNQLU2"); //Luganda as a second language
            hs.add("EMISNQMA107"); //Main spoken language Sorani Kurdish
            hs.add("EMISNQMA17"); //Main spoken language Romanian
            hs.add("EMISNQMA18"); //Main spoken language Bulgarian
            hs.add("EMISNQMA2"); //Mandarin as a second language
            hs.add("EMISNQMA23"); //Main spoken language Finnish
            hs.add("EMISNQMA26"); //Main spoken language Slovak
            hs.add("EMISNQMA28"); //Main spoken language Aragonese
            hs.add("EMISNQMA29"); //Main spoken language Avaric
            hs.add("EMISNQMA3"); //Malayalam as a second language
            hs.add("EMISNQMA30"); //Main spoken language Avestan
            hs.add("EMISNQMA31"); //Main spoken language Bambara
            hs.add("EMISNQMA32"); //Main spoken language Bosnian
            hs.add("EMISNQMA33"); //Main spoken language Chamorro
            hs.add("EMISNQMA34"); //Main spoken language Chechen
            hs.add("EMISNQMA35"); //Main spoken language Chinese
            hs.add("EMISNQMA36"); //Main spoken language Church Slavic
            hs.add("EMISNQMA37"); //Main spoken language Chuvash
            hs.add("EMISNQMA38"); //Main spoken language Cornish
            hs.add("EMISNQMA39"); //Main spoken language Cree
            hs.add("EMISNQMA40"); //Main spoken language Dhivehi
            hs.add("EMISNQMA41"); //Main spoken language Ewe
            hs.add("EMISNQMA42"); //Main spoken language Faroese
            hs.add("EMISNQMA43"); //Main spoken language Western Frisian
            hs.add("EMISNQMA45"); //Main spoken language Manx
            hs.add("EMISNQMA46"); //Main spoken language Gujarati
            hs.add("EMISNQMA47"); //Main spoken language Haitian
            hs.add("EMISNQMA48"); //Main spoken language Herero
            hs.add("EMISNQMA49"); //Main spoken language Hiri Motu
            hs.add("EMISNQMA50"); //Main spoken language Ido
            hs.add("EMISNQMA52"); //Main spoken language Kanuri
            hs.add("EMISNQMA53"); //Main spoken language Kikuyu
            hs.add("EMISNQMA54"); //Main spoken language Komi
            hs.add("EMISNQMA55"); //Main spoken language Kongo
            hs.add("EMISNQMA56"); //Main spoken language Kuanyama
            hs.add("EMISNQMA57"); //Main spoken language Latin
            hs.add("EMISNQMA58"); //Main spoken language Limburgan
            hs.add("EMISNQMA59"); //Main spoken language Luxembourgish
            hs.add("EMISNQMA60"); //Main spoken language Luba-Katanga
            hs.add("EMISNQMA61"); //Main spoken language Marshallese
            hs.add("EMISNQMA62"); //Main spoken language Navajo
            hs.add("EMISNQMA63"); //Main spoken language South Ndebele
            hs.add("EMISNQMA64"); //Main spoken language Northern Ndebele
            hs.add("EMISNQMA65"); //Main spoken language Ndonga
            hs.add("EMISNQMA66"); //Main spoken language Norwegian Nynorsk
            hs.add("EMISNQMA67"); //Main spoken language Norwegian Bokmal
            hs.add("EMISNQMA68"); //Main spoken language Chichewa
            hs.add("EMISNQMA69"); //Main spoken language Ojibwa
            hs.add("EMISNQMA70"); //Main spoken language Ossetian
            hs.add("EMISNQMA71"); //Main spoken language Pali
            hs.add("EMISNQMA72"); //Main spoken language Pushto
            hs.add("EMISNQMA73"); //Main spoken language Sanskrit
            hs.add("EMISNQMA74"); //Main spoken language Northern Sami
            hs.add("EMISNQMA75"); //Main spoken language Sardinian
            hs.add("EMISNQMA76"); //Main spoken language Tahitian
            hs.add("EMISNQMA77"); //Main spoken language Venda
            hs.add("EMISNQMA79"); //Main spoken language Walloon
            hs.add("EMISNQMA83"); //Main spoken language Bamoun
            hs.add("EMISNQMA84"); //Main spoken language Tetum
            hs.add("EMISNQMA85"); //Main spoken language NOS
            hs.add("EMISNQNO2"); //Norwegian as a second language
            hs.add("EMISNQOT5"); //Other main spoken language
            hs.add("EMISNQPA14"); //Patois as a second language
            hs.add("EMISNQPO2"); //Polish as a second language
            hs.add("EMISNQPO3"); //Portuguese as a second language
            hs.add("EMISNQRU2"); //Russian as a second language
            hs.add("EMISNQSE13"); //Serbian as a second language
            hs.add("EMISNQSH3"); //Shona as a second language
            hs.add("EMISNQSO5"); //Somali as a second language
            hs.add("EMISNQSP4"); //Spanish as a second language
            hs.add("EMISNQSW1"); //Swahili as a second language
            hs.add("EMISNQSW2"); //Swedish as a second language
            hs.add("EMISNQSY2"); //Sylheti as a second language
            hs.add("EMISNQTA1"); //Tamil as a second language
            hs.add("EMISNQTA2"); //Tagalog as a second language
            hs.add("EMISNQTH8"); //Thai as a second language
            hs.add("EMISNQTI2"); //Tigrinya as a second language
            hs.add("EMISNQTU2"); //Turkish as a second language
            hs.add("EMISNQUK2"); //Ukrainian as a second language
            hs.add("EMISNQUR1"); //Urdu as a second language
            hs.add("EMISNQVI6"); //Vietnamese as a second language
            hs.add("EMISNQWE4"); //Welsh as a second language
            hs.add("EMISNQYO1"); //Yoruba as a second language
            hs.add("ESCTMA8"); //Main spoken language Nyanja
            hs.add("TESTPEN1"); //English speaking ability
            hs.add("^ESCTAB583977"); //Abazinian language
            hs.add("^ESCTAB583978"); //Abkhazian language
            hs.add("^ESCTAC584028"); //Acholi language
            hs.add("^ESCTAC584188"); //Achinese language
            hs.add("^ESCTAD583979"); //Adygei language
            hs.add("^ESCTAD584256"); //Adamawa-Eastern language
            hs.add("^ESCTAD584257"); //Adamawa language
            hs.add("^ESCTAD584350"); //Adangme language
            hs.add("^ESCTAD759211"); //Adamorobe sign language
            hs.add("^ESCTAF583858"); //Afro-Asiatic language
            hs.add("^ESCTAF583871"); //Afar language
            hs.add("^ESCTAF584088"); //Afrikaans language
            hs.add("^ESCTAF759197"); //African sign language
            hs.add("^ESCTAG584351"); //Agni language
            hs.add("^ESCTAI584054"); //Ainu language
            hs.add("^ESCTAK730331"); //Akan language
            hs.add("^ESCTAL583901"); //Altaic language
            hs.add("^ESCTAL583919"); //Altai language
            hs.add("^ESCTAL584029"); //Alur language
            hs.add("^ESCTAL584050"); //Aleut language
            hs.add("^ESCTAL584064"); //Albanian language
            hs.add("^ESCTAL584393"); //Algonkian language
            hs.add("^ESCTAL759202"); //Algerian sign language
            hs.add("^ESCTAL759230"); //Albanian sign language
            hs.add("^ESCTAL759271"); //Al-Sayyid Bedouin sign language
            hs.add("^ESCTAM583890"); //Amharic language
            hs.add("^ESCTAM584265"); //Ambo language
            hs.add("^ESCTAM759311"); //American Sign Language
            hs.add("^ESCTAN583983"); //Andean equatorial language
            hs.add("^ESCTAP584409"); //Apache language
            hs.add("^ESCTAR583882"); //Aramaic language
            hs.add("^ESCTAR583897"); //Arabic language
            hs.add("^ESCTAR583951"); //Artificial language
            hs.add("^ESCTAR583960"); //Aranda language
            hs.add("^ESCTAR583984"); //Araucanian language
            hs.add("^ESCTAR583985"); //Arawak language
            hs.add("^ESCTAR584065"); //Armenian language
            hs.add("^ESCTAR584394"); //Arapaho language
            hs.add("^ESCTAR730684"); //Aragonese language
            hs.add("^ESCTAR759231"); //Armenian sign language
            hs.add("^ESCTAR759313"); //Argentine sign language
            hs.add("^ESCTAS583883"); //Assyrian language
            hs.add("^ESCTAS584138"); //Assamese language
            hs.add("^ESCTAS584461"); //Assiniboin language
            hs.add("^ESCTAS759200"); //Asian and Pacific sign language
            hs.add("^ESCTAT584408"); //Athapascan language
            hs.add("^ESCTAU583959"); //Australian language
            hs.add("^ESCTAU759232"); //Austrian sign language
            hs.add("^ESCTAU759283"); //Australian sign language
            hs.add("^ESCTAV583965"); //Avaric language
            hs.add("^ESCTAV762721"); //Avestan language
            hs.add("^ESCTAY583986"); //Aymara language
            hs.add("^ESCTAZ583946"); //Azerbaijani language
            hs.add("^ESCTBA583924"); //Balkar language
            hs.add("^ESCTBA583925"); //Bashkir language
            hs.add("^ESCTBA584017"); //Bari language
            hs.add("^ESCTBA584055"); //Barushaski language
            hs.add("^ESCTBA584058"); //Basque language
            hs.add("^ESCTBA584066"); //Baltic language
            hs.add("^ESCTBA584167"); //Baluchi language
            hs.add("^ESCTBA584189"); //Balinese language
            hs.add("^ESCTBA584190"); //Batak language
            hs.add("^ESCTBA584238"); //Bahnar language
            hs.add("^ESCTBA584260"); //Banda language
            hs.add("^ESCTBA584341"); //Bariba language
            hs.add("^ESCTBA584353"); //Bassa language
            hs.add("^ESCTBA584354"); //Baule language
            hs.add("^ESCTBA584373"); //Bambara language
            hs.add("^ESCTBA584383"); //Balante language
            hs.add("^ESCTBA584511"); //Baining language
            hs.add("^ESCTBA730411"); //Bamun language
            hs.add("^ESCTBA759203"); //Bamako sign language
            hs.add("^ESCTBA759284"); //Bali sign language
            hs.add("^ESCTBA759309"); //Ban Khor sign language
            hs.add("^ESCTBE453883"); //Bengali language
            hs.add("^ESCTBE583860"); //Berber language
            hs.add("^ESCTBE583872"); //Beja language
            hs.add("^ESCTBE584119"); //Belorussian language
            hs.add("^ESCTBE584264"); //Benue-Congo language
            hs.add("^ESCTBE584266"); //Bemba language
            hs.add("^ESCTBE759233"); //Belgian-French sign language
            hs.add("^ESCTBE759285"); //Bengali sign language
            hs.add("^ESCTBH584139"); //Bhili language
            hs.add("^ESCTBI584142"); //Bihari language
            hs.add("^ESCTBI584191"); //Bikol language
            hs.add("^ESCTBI723315"); //Bislama language
            hs.add("^ESCTBL584395"); //Blackfoot language
            hs.add("^ESCTBO584564"); //Bodo language
            hs.add("^ESCTBO729920"); //Norwegian
            hs.add("^ESCTBO730047"); //Bosnian language
            hs.add("^ESCTBO759314"); //Bolivian sign language
            hs.add("^ESCTBR583999"); //Bribri language
            hs.add("^ESCTBR584038"); //Brahui language
            hs.add("^ESCTBR584071"); //Brythonic Celtic language
            hs.add("^ESCTBR584072"); //Breton language
            hs.add("^ESCTBR724083"); //Brawa language
            hs.add("^ESCTBR730928"); //Braille language
            hs.add("^ESCTBR759234"); //British sign language
            hs.add("^ESCTBR759315"); //Brazilian sign language
            hs.add("^ESCTBU583903"); //Buryat language
            hs.add("^ESCTBU584125"); //Bulgarian language
            hs.add("^ESCTBU584176"); //Bushman language
            hs.add("^ESCTBU584192"); //Buginese language
            hs.add("^ESCTBU584267"); //Bubi language
            hs.add("^ESCTBU584268"); //Bulu language
            hs.add("^ESCTBU584565"); //Burmese language
            hs.add("^ESCTBU759204"); //Bura sign language
            hs.add("^ESCTCA583885"); //Canaanitic language
            hs.add("^ESCTCA583963"); //Caucasian language
            hs.add("^ESCTCA583993"); //Carib language
            hs.add("^ESCTCA584000"); //Cabecar language
            hs.add("^ESCTCA584103"); //Catalan language
            hs.add("^ESCTCA584410"); //Carrier language
            hs.add("^ESCTCA584414"); //Caddoan language
            hs.add("^ESCTCA584415"); //Caddo language
            hs.add("^ESCTCA584426"); //Cakchiquel language
            hs.add("^ESCTCA759235"); //Catalan sign language
            hs.add("^ESCTCA795323"); //Cayuga language
            hs.add("^ESCTCE583982"); //Central and South American Indian language
            hs.add("^ESCTCE584008"); //Central Sudanic language
            hs.add("^ESCTCE584070"); //Celtic language
            hs.add("^ESCTCE722136"); //Central Khmer language
            hs.add("^ESCTCH583868"); //Chadic language
            hs.add("^ESCTCH583917"); //Chuvash language
            hs.add("^ESCTCH583972"); //Chechen language
            hs.add("^ESCTCH583994"); //Chiquito language
            hs.add("^ESCTCH584006"); //Chari-Nile language
            hs.add("^ESCTCH584193"); //Cham language
            hs.add("^ESCTCH584194"); //Chamorro language
            hs.add("^ESCTCH584269"); //Chagga language
            hs.add("^ESCTCH584270"); //Chiga language
            hs.add("^ESCTCH584272"); //Chokwe language
            hs.add("^ESCTCH584396"); //Cheyenne language
            hs.add("^ESCTCH584411"); //Chilcotin language
            hs.add("^ESCTCH584412"); //Chipewyan language
            hs.add("^ESCTCH584420"); //Cherokee language
            hs.add("^ESCTCH584427"); //Chol language
            hs.add("^ESCTCH584428"); //Chontal language
            hs.add("^ESCTCH584438"); //Chickasaw language
            hs.add("^ESCTCH584439"); //Choctaw language
            hs.add("^ESCTCH584443"); //Chinantec language
            hs.add("^ESCTCH584500"); //Chukchi language
            hs.add("^ESCTCH584512"); //Chimbu language
            hs.add("^ESCTCH584541"); //Chinese language
            hs.add("^ESCTCH584554"); //Chuang language
            hs.add("^ESCTCH584566"); //Chin language
            hs.add("^ESCTCH759205"); //Chadian sign language
            hs.add("^ESCTCH759286"); //Chinese sign language
            hs.add("^ESCTCH759310"); //Chiangmai sign language
            hs.add("^ESCTCH759317"); //Chilean sign language
            hs.add("^ESCTCH762720"); //Church Slavic language
            hs.add("^ESCTCI583980"); //Circassian language
            hs.add("^ESCTCL583898"); //Classical Arabic language
            hs.add("^ESCTCO583880"); //Coptic language
            hs.add("^ESCTCO584476"); //Comanche language
            hs.add("^ESCTCO722073"); //Corsican language
            hs.add("^ESCTCO730766"); //Cornish language
            hs.add("^ESCTCO759206"); //Congolese sign language
            hs.add("^ESCTCO759318"); //Colombian sign language
            hs.add("^ESCTCO759319"); //Costa Rican sign language
            hs.add("^ESCTCR584397"); //Cree language
            hs.add("^ESCTCR584440"); //Creek language
            hs.add("^ESCTCR584462"); //Crow language
            hs.add("^ESCTCR602986"); //Crï¿½ole language
            hs.add("^ESCTCR730152"); //Croatian language
            hs.add("^ESCTCR759236"); //Croatian sign language
            hs.add("^ESCTCU583870"); //Cushitic language
            hs.add("^ESCTCU584001"); //Cuna language
            hs.add("^ESCTCU759320"); //Cuba sign language
            hs.add("^ESCTCZ584130"); //Czech language
            hs.add("^ESCTCZ759237"); //Czech sign language
            hs.add("^ESCTDA583964"); //Dagestan Caucasian language
            hs.add("^ESCTDA583967"); //Dargin language
            hs.add("^ESCTDA584081"); //Danish language
            hs.add("^ESCTDA584342"); //Dagomba language
            hs.add("^ESCTDA733294"); //Dari language
            hs.add("^ESCTDA759238"); //Danish sign language
            hs.add("^ESCTDE584398"); //Delaware language
            hs.add("^ESCTDI584030"); //Dinka language
            hs.add("^ESCTDJ584036"); //Djerma language
            hs.add("^ESCTDO759321"); //Dominican sign language
            hs.add("^ESCTDR584037"); //Dravidian language
            hs.add("^ESCTDU584089"); //Dutch language
            hs.add("^ESCTDU584273"); //Duala language
            hs.add("^ESCTDU759239"); //Dutch sign language
            hs.add("^ESCTDY584374"); //Dyula language
            hs.add("^ESCTDY584384"); //Dyola language
            hs.add("^ESCTEA583971"); //Eastern Caucasian language
            hs.add("^ESCTEA584013"); //Eastern Sudanic language
            hs.add("^ESCTEA584015"); //Eastern Nilotic language
            hs.add("^ESCTEA584118"); //Eastern Slavic language
            hs.add("^ESCTEA584259"); //Eastern language (Niger-Congo)
            hs.add("^ESCTEC759322"); //Ecuadorian sign language
            hs.add("^ESCTED584355"); //Edo language
            hs.add("^ESCTEF584333"); //Efik language
            hs.add("^ESCTEG583879"); //Egyptian language
            hs.add("^ESCTEG759207"); //Egyptian sign language
            hs.add("^ESCTEN584090"); //English language
            hs.add("^ESCTEN584513"); //Enga language
            hs.add("^ESCTEN584605"); //Enets language
            hs.add("^ESCTES583952"); //Esperanto
            hs.add("^ESCTES584049"); //Eskimo-Aleut language
            hs.add("^ESCTES584051"); //Eskimo language
            hs.add("^ESCTES584589"); //Estonian language
            hs.add("^ESCTES759240"); //Estonian sign language
            hs.add("^ESCTET583888"); //Ethiopic language
            hs.add("^ESCTET759208"); //Ethiopian sign language
            hs.add("^ESCTEU759198"); //European sign language
            hs.add("^ESCTEV583907"); //Evenki language
            hs.add("^ESCTEV583909"); //Even language
            hs.add("^ESCTEW584356"); //Ewe language
            hs.add("^ESCTFA584082"); //Faroese language
            hs.add("^ESCTFA584274"); //Fang language
            hs.add("^ESCTFA584358"); //Fanti language
            hs.add("^ESCTFA584520"); //Fanakalo language
            hs.add("^ESCTFI492342"); //First language
            hs.add("^ESCTFI584221"); //Fijian language
            hs.add("^ESCTFI584587"); //Finno-Ugric language
            hs.add("^ESCTFI584588"); //Finnic language
            hs.add("^ESCTFI584590"); //Finnish language
            hs.add("^ESCTFI759241"); //Finnish sign language
            hs.add("^ESCTFI759242"); //Finland-Swedish sign language
            hs.add("^ESCTFL584091"); //Flemish language
            hs.add("^ESCTFL584455"); //Flathead language
            hs.add("^ESCTFL759243"); //Flemish sign language
            hs.add("^ESCTFO584359"); //Fon language
            hs.add("^ESCTFO584399"); //Fox language
            hs.add("^ESCTFR584092"); //Frisian language
            hs.add("^ESCTFR584104"); //French language
            hs.add("^ESCTFR584521"); //French Crï¿½ole language
            hs.add("^ESCTFR759209"); //Franco-American sign language
            hs.add("^ESCTFR759244"); //French sign language
            hs.add("^ESCTFR759323"); //French Canadian sign language
            hs.add("^ESCTFU584052"); //Fur language
            hs.add("^ESCTFU584385"); //Fulani language
            hs.add("^ESCTFU584543"); //Fukienese dialect
            hs.add("^ESCTFU729883"); //Fulah language
            hs.add("^ESCTGA584110"); //Galician Portuguese dialect
            hs.add("^ESCTGA584275"); //Ganda language
            hs.add("^ESCTGA584360"); //Ga language
            hs.add("^ESCTGA584567"); //Garo language
            hs.add("^ESCTGA722813"); //Gaelic language
            hs.add("^ESCTGA724108"); //Galician language
            hs.add("^ESCTGB584261"); //Gbaya language
            hs.add("^ESCTGE583891"); //Geez language
            hs.add("^ESCTGE583975"); //Georgian language
            hs.add("^ESCTGE583992"); //Ge-Pano-Carib language
            hs.add("^ESCTGE583995"); //Ge language
            hs.add("^ESCTGE584078"); //Germanic language
            hs.add("^ESCTGE584094"); //German language
            hs.add("^ESCTGE759247"); //German sign language
            hs.add("^ESCTGH759210"); //Ghanian sign language
            hs.add("^ESCTGI584168"); //Gilaki language
            hs.add("^ESCTGI584213"); //Gilbertese language
            hs.add("^ESCTGI584277"); //Gisu language
            hs.add("^ESCTGO584039"); //Gondi language
            hs.add("^ESCTGO584074"); //Goidelic Celtic language
            hs.add("^ESCTGO584386"); //Gola language
            hs.add("^ESCTGR584099"); //Greek language
            hs.add("^ESCTGR584362"); //Grebo language
            hs.add("^ESCTGR722022"); //Greenlandic
            hs.add("^ESCTGR759249"); //Greek sign language
            hs.add("^ESCTGU453885"); //Gujarati language
            hs.add("^ESCTGU583892"); //Gurage language
            hs.add("^ESCTGU583987"); //Guarani language
            hs.add("^ESCTGU584002"); //Guaymi language
            hs.add("^ESCTGU584339"); //Gur language
            hs.add("^ESCTGU584344"); //Gurma language
            hs.add("^ESCTGU759212"); //Guinean sign language
            hs.add("^ESCTGU759324"); //Guatemalan sign language
            hs.add("^ESCTHA583869"); //Hausa language
            hs.add("^ESCTHA583893"); //Harari language
            hs.add("^ESCTHA584177"); //Hatsa language
            hs.add("^ESCTHA584226"); //Hawaiian language
            hs.add("^ESCTHA584417"); //Haida language
            hs.add("^ESCTHA584514"); //Hagen language
            hs.add("^ESCTHA584549"); //Hakka dialect
            hs.add("^ESCTHA759213"); //Hausa sign language
            hs.add("^ESCTHA759288"); //Hawaii Pidgin sign language
            hs.add("^ESCTHA819465"); //Haitian language
            hs.add("^ESCTHE583886"); //Hebrew language
            hs.add("^ESCTHE584098"); //Hellenic language
            hs.add("^ESCTHE584278"); //Hehe language
            hs.add("^ESCTHE584279"); //Herero language
            hs.add("^ESCTHI453887"); //Hindi language
            hs.add("^ESCTHI493047"); //Hinko language
            hs.add("^ESCTHI584143"); //Hindustani language
            hs.add("^ESCTHI584527"); //Hiri Motu language
            hs.add("^ESCTHM764109"); //Hmong language
            hs.add("^ESCTHO584179"); //Hottentot language
            hs.add("^ESCTHO584195"); //Hocano language
            hs.add("^ESCTHO584249"); //Ho language
            hs.add("^ESCTHO584477"); //Hopi language
            hs.add("^ESCTHO759289"); //Hong Kong sign language
            hs.add("^ESCTHO759325"); //Honduran sign language
            hs.add("^ESCTHS584550"); //Hsiang dialect
            hs.add("^ESCTHU584429"); //Huastec language
            hs.add("^ESCTHU584599"); //Hungarian language
            hs.add("^ESCTHU759250"); //Hungarian sign language
            hs.add("^ESCTIB584334"); //Ibibio language
            hs.add("^ESCTIB584363"); //Ibo language
            hs.add("^ESCTIB722138"); //Iban language
            hs.add("^ESCTIC584084"); //Icelandic language
            hs.add("^ESCTIC759251"); //Icelandic sign language
            hs.add("^ESCTID584366"); //Idoma language
            hs.add("^ESCTID762722"); //Ido language
            hs.add("^ESCTIG584196"); //Igorot language
            hs.add("^ESCTIJ584336"); //Ijo language
            hs.add("^ESCTIN583954"); //Interlingua
            hs.add("^ESCTIN583973"); //Ingush language
            hs.add("^ESCTIN584053"); //Independent language
            hs.add("^ESCTIN584063"); //Indo-European language
            hs.add("^ESCTIN584136"); //Indo-Iranian language
            hs.add("^ESCTIN584137"); //Indic language
            hs.add("^ESCTIN584187"); //Indonesian language
            hs.add("^ESCTIN721561"); //Inupiaq language
            hs.add("^ESCTIN722139"); //Inuktitut language
            hs.add("^ESCTIN759290"); //Indian sign language
            hs.add("^ESCTIN759291"); //Indonesian sign language
            hs.add("^ESCTIN759326"); //Inuit sign language
            hs.add("^ESCTIR584075"); //Irish Gaelic language
            hs.add("^ESCTIR584166"); //Iranian language
            hs.add("^ESCTIR584419"); //Iroquoian language
            hs.add("^ESCTIR759252"); //Irish sign language
            hs.add("^ESCTIS759272"); //Israeli sign language
            hs.add("^ESCTIT584100"); //Italic language
            hs.add("^ESCTIT584107"); //Italian language
            hs.add("^ESCTIT584501"); //Itelmen language
            hs.add("^ESCTIT759253"); //Italian sign language
            hs.add("^ESCTJA584059"); //Japanese language
            hs.add("^ESCTJA584197"); //Jarai language
            hs.add("^ESCTJA584198"); //Javanese language
            hs.add("^ESCTJA759292"); //Japanese sign language
            hs.add("^ESCTJA759327"); //Jamaican Country sign language
            hs.add("^ESCTJI583988"); //Jivaro language
            hs.add("^ESCTJO584568"); //Jonkha language
            hs.add("^ESCTJO759274"); //Jordanian sign language
            hs.add("^ESCTKA583861"); //Kabyle language
            hs.add("^ESCTKA583904"); //Kalmyk language
            hs.add("^ESCTKA583926"); //Kara-Kalpak language
            hs.add("^ESCTKA583927"); //Karachai language
            hs.add("^ESCTKA583928"); //Kazakh language
            hs.add("^ESCTKA583981"); //Kabardian language
            hs.add("^ESCTKA584018"); //Karamojong language
            hs.add("^ESCTKA584024"); //Kalenjin language
            hs.add("^ESCTKA584040"); //Kanarese language
            hs.add("^ESCTKA584144"); //Kashmiri language
            hs.add("^ESCTKA584280"); //Kamba language
            hs.add("^ESCTKA584345"); //Kabre language
            hs.add("^ESCTKA584515"); //Kate language
            hs.add("^ESCTKA584533"); //Kanuri language
            hs.add("^ESCTKA584570"); //Kachin language
            hs.add("^ESCTKA584572"); //Karen language
            hs.add("^ESCTKA584740"); //Kashubian Polish dialect
            hs.add("^ESCTKE584424"); //Keresan language
            hs.add("^ESCTKE584430"); //Kekchi language
            hs.add("^ESCTKE584503"); //Ket language
            hs.add("^ESCTKE759214"); //Kenyan sign language
            hs.add("^ESCTKH583920"); //Khakass language
            hs.add("^ESCTKH584175"); //Khoisan language
            hs.add("^ESCTKH584239"); //Khasi language
            hs.add("^ESCTKH584240"); //Khmer language
            hs.add("^ESCTKI583929"); //Kirgiz language
            hs.add("^ESCTKI584281"); //Kikuyu language
            hs.add("^ESCTKI584282"); //Kisii language
            hs.add("^ESCTKI584387"); //Kissi language
            hs.add("^ESCTKI584478"); //Kiowa language
            hs.add("^ESCTKI584523"); //Kituba language
            hs.add("^ESCTKL584450"); //Klamath language
            hs.add("^ESCTKO584060"); //Korean language
            hs.add("^ESCTKO584145"); //Konkani language
            hs.add("^ESCTKO584250"); //Korku language
            hs.add("^ESCTKO584283"); //Kongo language
            hs.add("^ESCTKO584505"); //Koryak language
            hs.add("^ESCTKO584591"); //Komi language
            hs.add("^ESCTKO759294"); //Korean sign language
            hs.add("^ESCTKP584375"); //Kpelle language
            hs.add("^ESCTKR584367"); //Kru language
            hs.add("^ESCTKR584524"); //Krio language
            hs.add("^ESCTKU583931"); //Kumyk language
            hs.add("^ESCTKU584042"); //Kui language
            hs.add("^ESCTKU584043"); //Kurukh language
            hs.add("^ESCTKU584169"); //Kurdish language
            hs.add("^ESCTKU722752"); //Kutchi language
            hs.add("^ESCTKU759275"); //Kuwaiti sign language
            hs.add("^ESCTKW584349"); //Kwa language
            hs.add("^ESCTKW584488"); //Kwakiutl language
            hs.add("^ESCTKW730185"); //Kwanyama language
            hs.add("^ESCTLA583968"); //Lak language
            hs.add("^ESCTLA584031"); //Lango language
            hs.add("^ESCTLA584067"); //Latvian language
            hs.add("^ESCTLA584101"); //Latin language
            hs.add("^ESCTLA584556"); //Lao language
            hs.add("^ESCTLA584573"); //Lahu language
            hs.add("^ESCTLA584592"); //Lappish language
            hs.add("^ESCTLA584613"); //Language commonly spoken in Europe
            hs.add("^ESCTLA759254"); //Latvian sign language
            hs.add("^ESCTLA759293"); //Laos sign language
            hs.add("^ESCTLE583969"); //Lezgin language
            hs.add("^ESCTLE584003"); //Lenca language
            hs.add("^ESCTLE584152"); //Lehnda punjabi language
            hs.add("^ESCTLE584574"); //Lepcha language
            hs.add("^ESCTLE759279"); //Lebanese sign language
            hs.add("^ESCTLI584069"); //Lithuanian language
            hs.add("^ESCTLI584285"); //Lingala language
            hs.add("^ESCTLI584456"); //Lillooet language
            hs.add("^ESCTLI584575"); //Lisu language
            hs.add("^ESCTLI730685"); //Limburgan language
            hs.add("^ESCTLI759215"); //Libyan sign language
            hs.add("^ESCTLI759255"); //Lithuanian sign language
            hs.add("^ESCTLO584019"); //Lotuko language
            hs.add("^ESCTLO584286"); //Lomwe language
            hs.add("^ESCTLO584287"); //Lozi language
            hs.add("^ESCTLO584376"); //Loma language
            hs.add("^ESCTLU584009"); //Lugbara language
            hs.add("^ESCTLU584032"); //Luo language
            hs.add("^ESCTLU584095"); //Luxembourgian language
            hs.add("^ESCTLU584288"); //Luba language
            hs.add("^ESCTLU584289"); //Luhya language
            hs.add("^ESCTLU584290"); //Lunda language
            hs.add("^ESCTLU584576"); //Lushei language
            hs.add("^ESCTLW584291"); //Lwea language
            hs.add("^ESCTLY759246"); //Lyons sign language
            hs.add("^ESCTMA583900"); //Maltese language
            hs.add("^ESCTMA583912"); //Manchu language
            hs.add("^ESCTMA583998"); //Macro-Chibchan language
            hs.add("^ESCTMA584010"); //Madi language
            hs.add("^ESCTMA584011"); //Mangbetu language
            hs.add("^ESCTMA584020"); //Masai language
            hs.add("^ESCTMA584045"); //Malayalam language
            hs.add("^ESCTMA584126"); //Macedonian language
            hs.add("^ESCTMA584146"); //Maldivian language
            hs.add("^ESCTMA584147"); //Marathi language
            hs.add("^ESCTMA584170"); //Mazanderani language
            hs.add("^ESCTMA584183"); //Maban language
            hs.add("^ESCTMA584184"); //Maba language
            hs.add("^ESCTMA584185"); //Malayo-Polynesian language
            hs.add("^ESCTMA584199"); //Madurese language
            hs.add("^ESCTMA584200"); //Malagasy language
            hs.add("^ESCTMA584201"); //Malay language
            hs.add("^ESCTMA584202"); //Maranao language
            hs.add("^ESCTMA584214"); //Marshallese language
            hs.add("^ESCTMA584227"); //Maori language
            hs.add("^ESCTMA584228"); //Marquesan language
            hs.add("^ESCTMA584295"); //Makonde language
            hs.add("^ESCTMA584296"); //Makua language
            hs.add("^ESCTMA584372"); //Mande language
            hs.add("^ESCTMA584377"); //Malinke language
            hs.add("^ESCTMA584425"); //Mayan language
            hs.add("^ESCTMA584431"); //Mam language
            hs.add("^ESCTMA584432"); //Maya language
            hs.add("^ESCTMA584444"); //Mazahua language
            hs.add("^ESCTMA584445"); //Mazatec language
            hs.add("^ESCTMA584479"); //Mayo language
            hs.add("^ESCTMA584516"); //Marind language
            hs.add("^ESCTMA584551"); //Mandarin dialect
            hs.add("^ESCTMA584593"); //Mari language
            hs.add("^ESCTMA676350"); //Main spoken language French Crï¿½ole
            hs.add("^ESCTMA729804"); //Manx language
            hs.add("^ESCTMA751104"); //Main spoken language Abkhaz
            hs.add("^ESCTMA751114"); //Main spoken language Faroese
            hs.add("^ESCTMA758475"); //Makaton vocabulary
            hs.add("^ESCTMA759216"); //Malagasy sign language
            hs.add("^ESCTMA759256"); //Maltese sign language
            hs.add("^ESCTMA759295"); //Macao sign language
            hs.add("^ESCTMA759296"); //Malaysian sign language
            hs.add("^ESCTMA759328"); //Maritime sign language
            hs.add("^ESCTMA759329"); //Mayan sign language
            hs.add("^ESCTMA822830"); //Main spoken language Fulani
            hs.add("^ESCTMA835629"); //Main spoken language Scottish Gaelic
            hs.add("^ESCTMA835630"); //Main spoken language Nuosu
            hs.add("^ESCTMA835636"); //Main spoken language Volapï¿½k
            hs.add("^ESCTMB584258"); //Mbum language
            hs.add("^ESCTMB584297"); //Mbundu language
            hs.add("^ESCTMB759219"); //Mbour sign language
            hs.add("^ESCTME584220"); //Melanesian language
            hs.add("^ESCTME584298"); //Meru language
            hs.add("^ESCTME584378"); //Mende language
            hs.add("^ESCTME584577"); //Meithei language
            hs.add("^ESCTME759330"); //Mexican sign language
            hs.add("^ESCTME795367"); //Menominee language
            hs.add("^ESCTMI584004"); //Miskito language
            hs.add("^ESCTMI584203"); //Minangkabau language
            hs.add("^ESCTMI584212"); //Micronesian language
            hs.add("^ESCTMI584400"); //Micmac language
            hs.add("^ESCTMI584446"); //Mixtec language
            hs.add("^ESCTMI584494"); //Mixe language
            hs.add("^ESCTMI584537"); //Miao-Yao language
            hs.add("^ESCTMI584538"); //Miao language
            hs.add("^ESCTMI759199"); //Middle Eastern sign language
            hs.add("^ESCTMO492341"); //Mother tongue
            hs.add("^ESCTMO583902"); //Mongolian language
            hs.add("^ESCTMO584108"); //Moldavian language
            hs.add("^ESCTMO584222"); //Motu language
            hs.add("^ESCTMO584237"); //Mon-Khmer language
            hs.add("^ESCTMO584242"); //Mon language
            hs.add("^ESCTMO584299"); //Mongo language
            hs.add("^ESCTMO584346"); //Mossi language
            hs.add("^ESCTMO584421"); //Mohawk language
            hs.add("^ESCTMO584491"); //Mohave language
            hs.add("^ESCTMO584595"); //Mordvin language
            hs.add("^ESCTMO759217"); //Moroccan sign language
            hs.add("^ESCTMO759218"); //Mozambican sign language
            hs.add("^ESCTMO759257"); //Moldova sign language
            hs.add("^ESCTMO759299"); //Mongolian sign language
            hs.add("^ESCTMU583962"); //Murngin language
            hs.add("^ESCTMU584061"); //Muong language
            hs.add("^ESCTMU584248"); //Munda language
            hs.add("^ESCTMU584251"); //Mundari language
            hs.add("^ESCTMU584437"); //Muskogean language
            hs.add("^ESCTMU584578"); //Murmi language
            hs.add("^ESCTNA583913"); //Nanai language
            hs.add("^ESCTNA584215"); //Nauruan language
            hs.add("^ESCTNA584391"); //Native North American language
            hs.add("^ESCTNA584401"); //Naskapi language
            hs.add("^ESCTNA584413"); //Navajo language
            hs.add("^ESCTNA584480"); //Nahuatl language
            hs.add("^ESCTNA584579"); //Nakhi language
            hs.add("^ESCTNA759220"); //Namibian sign language
            hs.add("^ESCTND584300"); //Ndebele language
            hs.add("^ESCTND730828"); //Ndonga language
            hs.add("^ESCTNE584148"); //Nepali language
            hs.add("^ESCTNE584451"); //Nez Perce language
            hs.add("^ESCTNE584581"); //Newari language
            hs.add("^ESCTNE584607"); //Nenets language
            hs.add("^ESCTNE759300"); //Nepalese sign language
            hs.add("^ESCTNE759301"); //New Zealand sign language
            hs.add("^ESCTNG584301"); //Ngala language
            hs.add("^ESCTNG584609"); //Nganasan language
            hs.add("^ESCTNI584014"); //Nilotic language
            hs.add("^ESCTNI584229"); //Niuean language
            hs.add("^ESCTNI584243"); //Nicobarese language
            hs.add("^ESCTNI584255"); //Niger-Congo language
            hs.add("^ESCTNI584506"); //Nivkh language
            hs.add("^ESCTNI584517"); //Nimboran language
            hs.add("^ESCTNI759221"); //Nigerian sign language
            hs.add("^ESCTNI759331"); //Nicaraguan sign language
            hs.add("^ESCTNK584302"); //Nkole language
            hs.add("^ESCTNO583896"); //North Arabic language
            hs.add("^ESCTNO583906"); //Northern Tungusic language
            hs.add("^ESCTNO583918"); //Northeastern Turkic language
            hs.add("^ESCTNO583922"); //Northwestern Turkic language
            hs.add("^ESCTNO583932"); //Nogai language
            hs.add("^ESCTNO584079"); //Northern Germanic language
            hs.add("^ESCTNO584085"); //Norwegian language
            hs.add("^ESCTNO584489"); //Nootka language
            hs.add("^ESCTNO584545"); //Northern Fukienese dialect
            hs.add("^ESCTNO729891"); //Norwegian Nynorsk language
            hs.add("^ESCTNO730235"); //Northern Sami language
            hs.add("^ESCTNO730963"); //North Ndebele language
            hs.add("^ESCTNO759258"); //Norwegian sign language
            hs.add("^ESCTNU584033"); //Nuer language
            hs.add("^ESCTNU584035"); //Nubian language
            hs.add("^ESCTNU584368"); //Nupe language
            hs.add("^ESCTNU584557"); //Nung language
            hs.add("^ESCTNU730414"); //Nuosu language
            hs.add("^ESCTNY584304"); //Nyamwezi language
            hs.add("^ESCTNY584305"); //Nyanja language
            hs.add("^ESCTNY584307"); //Nyoro language
            hs.add("^ESCTOC583956"); //Occidental
            hs.add("^ESCTOC721097"); //Occitan language
            hs.add("^ESCTOD795324"); //Odawa language
            hs.add("^ESCTOJ584405"); //Ojibwa language
            hs.add("^ESCTOJ795295"); //Oji-Cree language
            hs.add("^ESCTOK584457"); //Okanagan language
            hs.add("^ESCTOM584463"); //Omaha language
            hs.add("^ESCTOM759280"); //Omani sign language
            hs.add("^ESCTON584422"); //Oneida language
            hs.add("^ESCTOR583873"); //Oromo language
            hs.add("^ESCTOR584149"); //Oriya language
            hs.add("^ESCTOS584171"); //Ossetian language
            hs.add("^ESCTOS584464"); //Osage language
            hs.add("^ESCTOS584600"); //Ostyak language
            hs.add("^ESCTOT584442"); //Oto-Manguean language
            hs.add("^ESCTOT584447"); //Otomi language
            hs.add("^ESCTPA453889"); //Pashtu language
            hs.add("^ESCTPA583996"); //Panoan language
            hs.add("^ESCTPA584150"); //Pakistani punjabi language
            hs.add("^ESCTPA584204"); //Palau language
            hs.add("^ESCTPA584205"); //Pampangan language
            hs.add("^ESCTPA584206"); //Pangasinan language
            hs.add("^ESCTPA584244"); //Palaung language
            hs.add("^ESCTPA584407"); //Passamaquoddy language
            hs.add("^ESCTPA584416"); //Pawnee language
            hs.add("^ESCTPA584481"); //Paiute language
            hs.add("^ESCTPA584482"); //Papago language
            hs.add("^ESCTPA584499"); //Paleo-Asiatic language
            hs.add("^ESCTPA584510"); //Papuan language
            hs.add("^ESCTPA584525"); //Papiamento language
            hs.add("^ESCTPA759276"); //Palestinian sign language
            hs.add("^ESCTPA759302"); //Pakistan sign language
            hs.add("^ESCTPA759332"); //Panamanian sign language
            hs.add("^ESCTPA762719"); //Pali language
            hs.add("^ESCTPE584172"); //Persian language
            hs.add("^ESCTPE584308"); //Pedi language
            hs.add("^ESCTPE584449"); //Penutian language
            hs.add("^ESCTPE759277"); //Persian sign language
            hs.add("^ESCTPE759298"); //Penang sign language
            hs.add("^ESCTPE759333"); //Peruvian sign language
            hs.add("^ESCTPH759287"); //Philippine sign language
            hs.add("^ESCTPI584483"); //Pima language
            hs.add("^ESCTPI584518"); //Pidgin and Crï¿½ole language
            hs.add("^ESCTPI584526"); //Pidgin English language
            hs.add("^ESCTPL759334"); //Plains Indian sign language
            hs.add("^ESCTPO584109"); //Portuguese language
            hs.add("^ESCTPO584131"); //Polish language
            hs.add("^ESCTPO584217"); //Ponapean language
            hs.add("^ESCTPO584225"); //Polynesian language
            hs.add("^ESCTPO759259"); //Polish sign language
            hs.add("^ESCTPO759260"); //Portuguese sign language
            hs.add("^ESCTPR584111"); //Provencal language
            hs.add("^ESCTPR759335"); //Providencia sign language
            hs.add("^ESCTPU453894"); //Punjabi language
            hs.add("^ESCTPU584558"); //Puyi language
            hs.add("^ESCTPU759336"); //Puerto Rican sign language
            hs.add("^ESCTQA759281"); //Qatari sign language
            hs.add("^ESCTQU583989"); //Quechua language
            hs.add("^ESCTQU584434"); //Quiche language
            hs.add("^ESCTRA584158"); //Rajasthani language
            hs.add("^ESCTRA584230"); //Rarotongan language
            hs.add("^ESCTRH584112"); //Rhaeto-Romanic language
            hs.add("^ESCTRH584207"); //Rhade language
            hs.add("^ESCTRI583862"); //Riffian language
            hs.add("^ESCTRO584102"); //Romance language
            hs.add("^ESCTRO584159"); //Romany language
            hs.add("^ESCTRO723060"); //Romansh language
            hs.add("^ESCTRO759261"); //Romanian sign language
            hs.add("^ESCTRU584113"); //Rumanian language
            hs.add("^ESCTRU584122"); //Russian language
            hs.add("^ESCTRU584309"); //Ruanda language
            hs.add("^ESCTRU584312"); //Rundi language
            hs.add("^ESCTRU759262"); //Russian sign language
            hs.add("^ESCTSA583875"); //Saho language
            hs.add("^ESCTSA583939"); //Salar language
            hs.add("^ESCTSA584012"); //Sara language
            hs.add("^ESCTSA584115"); //Sardinian language
            hs.add("^ESCTSA584161"); //Sanskrit language
            hs.add("^ESCTSA584180"); //Sandawe language
            hs.add("^ESCTSA584231"); //Samoan language
            hs.add("^ESCTSA584252"); //Santali language
            hs.add("^ESCTSA584253"); //Savara language
            hs.add("^ESCTSA584262"); //Sango language
            hs.add("^ESCTSA584454"); //Salishan language
            hs.add("^ESCTSA584530"); //Saramacca language
            hs.add("^ESCTSA584532"); //Saharan language
            hs.add("^ESCTSA584604"); //Samoyed language
            hs.add("^ESCTSA759278"); //Saudi Arabian sign language
            hs.add("^ESCTSA759303"); //Samoa sign language
            hs.add("^ESCTSA759337"); //Salvadoran sign language
            hs.add("^ESCTSC584077"); //Scottish Gaelic language
            hs.add("^ESCTSE583881"); //Semitic language
            hs.add("^ESCTSE584127"); //Serbo-Croatian language
            hs.add("^ESCTSE584245"); //Sedang language
            hs.add("^ESCTSE584348"); //Senufo language
            hs.add("^ESCTSE584388"); //Serer language
            hs.add("^ESCTSE584423"); //Seneca language
            hs.add("^ESCTSE584441"); //Seminole language
            hs.add("^ESCTSE584611"); //Selkup language
            hs.add("^ESCTSE730882"); //Serbian language
            hs.add("^ESCTSE759297"); //Selangor sign language
            hs.add("^ESCTSH583863"); //Shawia language
            hs.add("^ESCTSH583864"); //Shluh language
            hs.add("^ESCTSH584034"); //Shilluk language
            hs.add("^ESCTSH584314"); //Shona language
            hs.add("^ESCTSH584458"); //Shuswap language
            hs.add("^ESCTSH584484"); //Shoshone language
            hs.add("^ESCTSH584560"); //Shan language
            hs.add("^ESCTSI583876"); //Sidamo language
            hs.add("^ESCTSI583915"); //Sibo language
            hs.add("^ESCTSI584156"); //Sikh punjabi language
            hs.add("^ESCTSI584162"); //Sindhi language
            hs.add("^ESCTSI584163"); //Sinhalese language
            hs.add("^ESCTSI584460"); //Siouan language
            hs.add("^ESCTSI584465"); //Sioux language
            hs.add("^ESCTSI584536"); //Sino-Tibetan language
            hs.add("^ESCTSI584540"); //Sinitic language
            hs.add("^ESCTSI726367"); //Sign language
            hs.add("^ESCTSI759201"); //Sign language of the Americas
            hs.add("^ESCTSI759222"); //Sierra Leone sign language
            hs.add("^ESCTSI759304"); //Singapore sign language
            hs.add("^ESCTSL584117"); //Slavic language
            hs.add("^ESCTSL584128"); //Slovenian language
            hs.add("^ESCTSL584132"); //Slovak language
            hs.add("^ESCTSL759263"); //Slovakian sign language
            hs.add("^ESCTSL759270"); //Slovenian sign language
            hs.add("^ESCTSO583877"); //Somali language
            hs.add("^ESCTSO583899"); //South Arabic language
            hs.add("^ESCTSO583911"); //Southern Tungusic language
            hs.add("^ESCTSO583935"); //Southeastern Turkic language
            hs.add("^ESCTSO583944"); //Southwestern Turkic language
            hs.add("^ESCTSO583974"); //Southern Caucasian language
            hs.add("^ESCTSO584023"); //Southern Nilotic language
            hs.add("^ESCTSO584124"); //Southern Slavic language
            hs.add("^ESCTSO584133"); //Sorbian language
            hs.add("^ESCTSO584315"); //Sotho language
            hs.add("^ESCTSO584379"); //Soninke language
            hs.add("^ESCTSO584547"); //Southern Fukienese dialect
            hs.add("^ESCTSO584585"); //Songhai language
            hs.add("^ESCTSO722242"); //Southern Sotho language
            hs.add("^ESCTSO729923"); //South Ndebele language
            hs.add("^ESCTSO759223"); //Somali sign language
            hs.add("^ESCTSO759224"); //South African sign language
            hs.add("^ESCTSP584116"); //Spanish language
            hs.add("^ESCTSP759264"); //Spanish sign language
            hs.add("^ESCTSR759305"); //Sri Lankan sign language
            hs.add("^ESCTSU584025"); //Suk language
            hs.add("^ESCTSU584208"); //Sundanese language
            hs.add("^ESCTSU584316"); //Sukuma language
            hs.add("^ESCTSU584380"); //Susu language
            hs.add("^ESCTSW584086"); //Swedish language
            hs.add("^ESCTSW584106"); //Swiss French dialect
            hs.add("^ESCTSW584317"); //Swahili language
            hs.add("^ESCTSW584319"); //Swazi language
            hs.add("^ESCTSW584616"); //Swiss German dialect
            hs.add("^ESCTSW759245"); //Swiss-French sign language
            hs.add("^ESCTSW759248"); //Swiss-German sign language
            hs.add("^ESCTSW759266"); //Swedish sign language
            hs.add("^ESCTSY583884"); //Syriac language
            hs.add("^ESCTTA583865"); //Tamazight language
            hs.add("^ESCTTA583933"); //Tatar language
            hs.add("^ESCTTA583970"); //Tabasaran language
            hs.add("^ESCTTA583997"); //Tacana language
            hs.add("^ESCTTA584046"); //Tamil language
            hs.add("^ESCTTA584173"); //Tajik language
            hs.add("^ESCTTA584209"); //Tagalog language
            hs.add("^ESCTTA584232"); //Tahitian language
            hs.add("^ESCTTA584468"); //Tanoan language
            hs.add("^ESCTTA584472"); //Tarasco language
            hs.add("^ESCTTA584485"); //Tarahumara language
            hs.add("^ESCTTA584531"); //Taki-Taki language
            hs.add("^ESCTTA584553"); //Tai language
            hs.add("^ESCTTA759225"); //Tanzanian sign language
            hs.add("^ESCTTA759306"); //Taiwanese sign language
            hs.add("^ESCTTE584021"); //Teso language
            hs.add("^ESCTTE584047"); //Telugu language
            hs.add("^ESCTTE584389"); //Temne language
            hs.add("^ESCTTE584469"); //Tewa language
            hs.add("^ESCTTE584534"); //Teda language
            hs.add("^ESCTTE731906"); //Tetum language
            hs.add("^ESCTTH584459"); //Thompson language
            hs.add("^ESCTTH584561"); //Thai language
            hs.add("^ESCTTH759308"); //Thai sign language
            hs.add("^ESCTTI583894"); //Tigre language
            hs.add("^ESCTTI583895"); //Tigrinya language
            hs.add("^ESCTTI584335"); //Tiv language
            hs.add("^ESCTTI584470"); //Tiwa language
            hs.add("^ESCTTI584563"); //Tibeto-Burman language
            hs.add("^ESCTTI584582"); //Tibetan language
            hs.add("^ESCTTI759307"); //Tibetan sign language
            hs.add("^ESCTTL584473"); //Tlingit language
            hs.add("^ESCTTO584223"); //Tolai language
            hs.add("^ESCTTO584233"); //Tongan language
            hs.add("^ESCTTO584322"); //Toro language
            hs.add("^ESCTTO584471"); //Towa language
            hs.add("^ESCTTO584474"); //Totonac language
            hs.add("^ESCTTR584218"); //Trukese language
            hs.add("^ESCTTS584323"); //Tsonga language
            hs.add("^ESCTTS584324"); //Tswana language
            hs.add("^ESCTTS584452"); //Tsimshian language
            hs.add("^ESCTTU583866"); //Tuareg language
            hs.add("^ESCTTU583905"); //Tungusic language
            hs.add("^ESCTTU583916"); //Turkic language
            hs.add("^ESCTTU583921"); //Tuvinian language
            hs.add("^ESCTTU583947"); //Turkish language
            hs.add("^ESCTTU583948"); //Turkmen language
            hs.add("^ESCTTU583991"); //Tupi language
            hs.add("^ESCTTU584022"); //Turkana language
            hs.add("^ESCTTU584048"); //Tulu language
            hs.add("^ESCTTU584235"); //Tuamotu language
            hs.add("^ESCTTU584326"); //Tumbuka language
            hs.add("^ESCTTU584562"); //Tung language
            hs.add("^ESCTTU759226"); //Tunisian sign language
            hs.add("^ESCTTU759267"); //Turkish sign language
            hs.add("^ESCTTU795368"); //Tuscarora language
            hs.add("^ESCTTW584369"); //Twi language
            hs.add("^ESCTTZ584435"); //Tzeltal language
            hs.add("^ESCTTZ584436"); //Tzotzil language
            hs.add("^ESCTUD584596"); //Udmurt language
            hs.add("^ESCTUG584598"); //Ugric language
            hs.add("^ESCTUG759227"); //Ugandan sign language
            hs.add("^ESCTUK584123"); //Ukrainian language
            hs.add("^ESCTUK759268"); //Ukrainian sign language
            hs.add("^ESCTUN759282"); //United Arab Emirates sign language
            hs.add("^ESCTUR453897"); //Urdu language
            hs.add("^ESCTUR584370"); //Urhobo language
            hs.add("^ESCTUR584586"); //Uralic language
            hs.add("^ESCTUR759316"); //Urubï¿½-Kaapor sign language
            hs.add("^ESCTUR759338"); //Uruguayan sign language
            hs.add("^ESCTUT584475"); //Uto-Aztecan language
            hs.add("^ESCTUT584486"); //Ute language
            hs.add("^ESCTUV584236"); //Uvea language
            hs.add("^ESCTUY583940"); //Uyghur language
            hs.add("^ESCTUZ583943"); //Uzbek language
            hs.add("^ESCTVA584381"); //Vai language
            hs.add("^ESCTVA759265"); //Valencian sign language
            hs.add("^ESCTVE584327"); //Venda language
            hs.add("^ESCTVE759339"); //Venezuelan sign language
            hs.add("^ESCTVI584211"); //Visayan language
            hs.add("^ESCTVO584602"); //Vogul language
            hs.add("^ESCTVO762718"); //Volapuk language
            hs.add("^ESCTWA584246"); //Wa language
            hs.add("^ESCTWA584487"); //Wakashan language
            hs.add("^ESCTWA730067"); //Walloon language
            hs.add("^ESCTWE583976"); //Western Caucasian language
            hs.add("^ESCTWE584027"); //Western Nilotic language
            hs.add("^ESCTWE584073"); //Welsh language
            hs.add("^ESCTWE584087"); //Western Germanic language
            hs.add("^ESCTWE584129"); //Western Slavic language
            hs.add("^ESCTWE584338"); //Western Sudanic language
            hs.add("^ESCTWE584382"); //West Atlantic language
            hs.add("^ESCTWE729892"); //Western Frisian language
            hs.add("^ESCTWI584467"); //Winnebago language
            hs.add("^ESCTWO584390"); //Wolof language
            hs.add("^ESCTWU584552"); //Wu dialect
            hs.add("^ESCTXH584328"); //Xhosa language
            hs.add("^ESCTYA583950"); //Yakut language
            hs.add("^ESCTYA584219"); //Yapese language
            hs.add("^ESCTYA584224"); //Yabim language
            hs.add("^ESCTYA584329"); //Yao language - Bantu
            hs.add("^ESCTYA584331"); //Yaunde language
            hs.add("^ESCTYA584453"); //Yakima language
            hs.add("^ESCTYA584615"); //Yao language - Sino-Tibet
            hs.add("^ESCTYI584097"); //Yiddish language
            hs.add("^ESCTYI584583"); //Yi language
            hs.add("^ESCTYI759273"); //Yiddish sign language
            hs.add("^ESCTYO584371"); //Yoruba language
            hs.add("^ESCTYU584490"); //Yuman language
            hs.add("^ESCTYU584492"); //Yuma language
            hs.add("^ESCTYU584508"); //Yukagir language
            hs.add("^ESCTYU759269"); //Yugoslavian sign language
            hs.add("^ESCTZA584263"); //Zande language
            hs.add("^ESCTZA584448"); //Zapotec language
            hs.add("^ESCTZA759228"); //Zambian sign language
            hs.add("^ESCTZI759229"); //Zimbabwe sign language
            hs.add("^ESCTZO584493"); //Zoquean language
            hs.add("^ESCTZO584495"); //Zoque language
            hs.add("^ESCTZU584332"); //Zulu language
            hs.add("^ESCTZU584497"); //Zunian language
            hs.add("^ESCTZU584498"); //Zuni language

            hsEmisLanguageCodes = hs;
        }

        return hsEmisLanguageCodes.contains(code);
    }

    private static boolean isTppLanguageCode(String code) {

        if (hsTppLanguageCodes == null) {

            Set<String> hs = new HashSet<>();

            hs.add("13Z61"); //Bengali language
            hs.add("13Z62"); //Gujarati language
            hs.add("13Z63"); //Hindi language
            hs.add("13Z64"); //Pashtu language
            hs.add("13Z65"); //Punjabi language
            hs.add("13Z66"); //Urdu language
            hs.add("Ua0HJ"); //Main spoken language
            hs.add("Ua0dA"); //Hinko language
            hs.add("Xa4A6"); //Using lip-reading
            hs.add("Xa6bm"); //World languages
            hs.add("Xa6bn"); //Afro-Asiatic language
            hs.add("Xa6bo"); //Berber language
            hs.add("Xa6bp"); //Kabyle language
            hs.add("Xa6bq"); //Riffian language
            hs.add("Xa6br"); //Shawia language
            hs.add("Xa6bs"); //Shluh language
            hs.add("Xa6bt"); //Tamazight language
            hs.add("Xa6bu"); //Tuareg language
            hs.add("Xa6bv"); //Chadic language
            hs.add("Xa6bw"); //Hausa language
            hs.add("Xa6bx"); //Cushitic language
            hs.add("Xa6by"); //Afar language
            hs.add("Xa6bz"); //Beja language
            hs.add("Xa6c0"); //Oromo language
            hs.add("Xa6c1"); //Saho language
            hs.add("Xa6c2"); //Sidamo language
            hs.add("Xa6c3"); //Somali language
            hs.add("Xa6c4"); //Egyptian language
            hs.add("Xa6c5"); //Coptic language
            hs.add("Xa6c6"); //Semitic language
            hs.add("Xa6c7"); //Aramaic language
            hs.add("Xa6c8"); //Assyrian language
            hs.add("Xa6c9"); //Syriac language
            hs.add("Xa6cA"); //Canaanitic language
            hs.add("Xa6cB"); //Hebrew language
            hs.add("Xa6cC"); //Ethiopic language
            hs.add("Xa6cD"); //Amharic language
            hs.add("Xa6cE"); //Geez language
            hs.add("Xa6cF"); //Gurage language
            hs.add("Xa6cG"); //Harari language
            hs.add("Xa6cH"); //Tigre language
            hs.add("Xa6cI"); //Tigrinya language
            hs.add("Xa6cJ"); //North Arabic language
            hs.add("Xa6cK"); //Arabic language
            hs.add("Xa6cL"); //Classical Arabic language
            hs.add("Xa6cM"); //South Arabic language
            hs.add("Xa6cN"); //Maltese language
            hs.add("Xa6cO"); //Altaic language
            hs.add("Xa6cP"); //Mongolian language
            hs.add("Xa6cQ"); //Buryat language
            hs.add("Xa6cR"); //Kalmyk language
            hs.add("Xa6cS"); //Tungusic language
            hs.add("Xa6cT"); //Northern Tungusic language
            hs.add("Xa6cU"); //Evenki language
            hs.add("Xa6cV"); //Even language
            hs.add("Xa6cW"); //Southern Tungusic language
            hs.add("Xa6cX"); //Manchu language
            hs.add("Xa6cY"); //Nanai language
            hs.add("Xa6cZ"); //Sibo language
            hs.add("Xa6ca"); //Turkic language
            hs.add("Xa6cb"); //Chuvash language
            hs.add("Xa6cc"); //Northeastern Turkic language
            hs.add("Xa6cd"); //Altai language
            hs.add("Xa6ce"); //Khakass language
            hs.add("Xa6cf"); //Tuvinian language
            hs.add("Xa6cg"); //Northwestern Turkic language
            hs.add("Xa6ch"); //Balkar language
            hs.add("Xa6ci"); //Bashkir language
            hs.add("Xa6cj"); //Kara-Kalpak language
            hs.add("Xa6ck"); //Karachai language
            hs.add("Xa6cl"); //Kazakh language
            hs.add("Xa6cm"); //Kirgiz language
            hs.add("Xa6cn"); //Kumyk language
            hs.add("Xa6co"); //Nogai language
            hs.add("Xa6cp"); //Tatar language
            hs.add("Xa6cq"); //Southeastern Turkic language
            hs.add("Xa6cr"); //Salar language
            hs.add("Xa6cs"); //Uigur language
            hs.add("Xa6ct"); //Uzbek language
            hs.add("Xa6cu"); //Southwestern Turkic language
            hs.add("Xa6cv"); //Azerbaijani language
            hs.add("Xa6cw"); //Turkish language
            hs.add("Xa6cx"); //Turkmen language
            hs.add("Xa6cy"); //Yakut language
            hs.add("Xa6cz"); //Artificial language
            hs.add("Xa6d0"); //Esperanto
            hs.add("Xa6d1"); //Interlingua
            hs.add("Xa6d2"); //Occidental
            hs.add("Xa6d3"); //Australian language
            hs.add("Xa6d4"); //Aranda language
            hs.add("Xa6d5"); //Murngin language
            hs.add("Xa6d6"); //Caucasian language
            hs.add("Xa6d7"); //Dagestan Caucasian language
            hs.add("Xa6d8"); //Avar language
            hs.add("Xa6d9"); //Dargin language
            hs.add("Xa6dA"); //Lak language
            hs.add("Xa6dB"); //Lezgin language
            hs.add("Xa6dC"); //Tabasaran language
            hs.add("Xa6dD"); //Eastern Caucasian language
            hs.add("Xa6dE"); //Chechen language
            hs.add("Xa6dF"); //Ingush language
            hs.add("Xa6dG"); //Southern Caucasian language
            hs.add("Xa6dH"); //Georgian language
            hs.add("Xa6dI"); //Western Caucasian language
            hs.add("Xa6dJ"); //Abazinian language
            hs.add("Xa6dK"); //Abkhazian language
            hs.add("Xa6dL"); //Adygei language
            hs.add("Xa6dM"); //Circassian language
            hs.add("Xa6dN"); //Kabardian language
            hs.add("Xa6dO"); //Central and South American Indian language
            hs.add("Xa6dP"); //Andean equatorial language
            hs.add("Xa6dQ"); //Araucanian language
            hs.add("Xa6dR"); //Arawak language
            hs.add("Xa6dS"); //Aymara language
            hs.add("Xa6dT"); //Guarani language
            hs.add("Xa6dU"); //Jivaro language
            hs.add("Xa6dV"); //Quechua language
            hs.add("Xa6dW"); //Tupi language
            hs.add("Xa6dX"); //Ge-Pano-Carib language
            hs.add("Xa6dY"); //Carib language
            hs.add("Xa6dZ"); //Chiquito language
            hs.add("Xa6da"); //Ge language
            hs.add("Xa6db"); //Panoan language
            hs.add("Xa6dc"); //Tacana language
            hs.add("Xa6dd"); //Macro-Chibchan language
            hs.add("Xa6de"); //Bribri language
            hs.add("Xa6df"); //Cabecar language
            hs.add("Xa6dg"); //Cuna language
            hs.add("Xa6dh"); //Guaymi language
            hs.add("Xa6di"); //Lenca language
            hs.add("Xa6dj"); //Miskito language
            hs.add("Xa6dk"); //Chari-Nile language
            hs.add("Xa6dl"); //Central Sudanic language
            hs.add("Xa6dm"); //Lugbara language
            hs.add("Xa6dn"); //Madi language
            hs.add("Xa6do"); //Mangbetu language
            hs.add("Xa6dp"); //Sara language
            hs.add("Xa6dq"); //Eastern Sudanic language
            hs.add("Xa6dr"); //Nilotic language
            hs.add("Xa6ds"); //Eastern Nilotic language
            hs.add("Xa6dt"); //Bari language
            hs.add("Xa6du"); //Karamojong language
            hs.add("Xa6dv"); //Lotuko language
            hs.add("Xa6dw"); //Masai language
            hs.add("Xa6dx"); //Teso language
            hs.add("Xa6dy"); //Turkana language
            hs.add("Xa6dz"); //Southern Nilotic language
            hs.add("Xa6e0"); //Kalenjin language
            hs.add("Xa6e1"); //Suk language
            hs.add("Xa6e2"); //Western Nilotic language
            hs.add("Xa6e3"); //Acholi language
            hs.add("Xa6e4"); //Alur language
            hs.add("Xa6e5"); //Dinka language
            hs.add("Xa6e6"); //Lango language
            hs.add("Xa6e7"); //Luo language
            hs.add("Xa6e8"); //Nuer language
            hs.add("Xa6e9"); //Shilluk language
            hs.add("Xa6eA"); //Nubian language
            hs.add("Xa6eB"); //Djerma language
            hs.add("Xa6eC"); //Dravidian language
            hs.add("Xa6eD"); //Brahui language
            hs.add("Xa6eE"); //Gondi language
            hs.add("Xa6eF"); //Kanarese language
            hs.add("Xa6eG"); //Kui language
            hs.add("Xa6eH"); //Kurukh language
            hs.add("Xa6eI"); //Malayalam language
            hs.add("Xa6eJ"); //Tamil language
            hs.add("Xa6eK"); //Telugu language
            hs.add("Xa6eL"); //Tulu language
            hs.add("Xa6eM"); //Eskimo-Aleut language
            hs.add("Xa6eN"); //Aleut language
            hs.add("Xa6eO"); //Eskimo language
            hs.add("Xa6eP"); //Fur language
            hs.add("Xa6eQ"); //Independent language
            hs.add("Xa6eR"); //Ainu language
            hs.add("Xa6eS"); //Barushaski language
            hs.add("Xa6eT"); //Basque language
            hs.add("Xa6eU"); //Japanese language
            hs.add("Xa6eV"); //Korean language
            hs.add("Xa6eW"); //Muong language
            hs.add("Xa6eX"); //Vietnamese language
            hs.add("Xa6eY"); //Indo-European language
            hs.add("Xa6eZ"); //Albanian language
            hs.add("Xa6ea"); //Armenian language
            hs.add("Xa6eb"); //Baltic language
            hs.add("Xa6ec"); //Latvian language
            hs.add("Xa6ed"); //Lithuanian language
            hs.add("Xa6ee"); //Celtic language
            hs.add("Xa6ef"); //Brythonic Celtic language
            hs.add("Xa6eg"); //Breton language
            hs.add("Xa6eh"); //Welsh language
            hs.add("Xa6ei"); //Goidelic Celtic language
            hs.add("Xa6ej"); //Irish Gaelic language
            hs.add("Xa6ek"); //Scottish Gaelic language
            hs.add("Xa6el"); //Germanic language
            hs.add("Xa6em"); //Northern Germanic language
            hs.add("Xa6en"); //Danish language
            hs.add("Xa6eo"); //Faroese language
            hs.add("Xa6ep"); //Icelandic language
            hs.add("Xa6eq"); //Norwegian language
            hs.add("Xa6er"); //Swedish language
            hs.add("Xa6es"); //Western Germanic language
            hs.add("Xa6et"); //Afrikaans language
            hs.add("Xa6eu"); //Dutch language
            hs.add("Xa6ev"); //English language
            hs.add("Xa6ew"); //Flemish language
            hs.add("Xa6ex"); //Frisian language
            hs.add("Xa6ey"); //German language
            hs.add("Xa6ez"); //Luxembourgian language
            hs.add("Xa6f0"); //Yiddish language
            hs.add("Xa6f1"); //Hellenic language
            hs.add("Xa6f2"); //Greek language
            hs.add("Xa6f3"); //Italic language
            hs.add("Xa6f4"); //Latin language
            hs.add("Xa6f5"); //Romance language
            hs.add("Xa6f6"); //Catalan language
            hs.add("Xa6f7"); //French language
            hs.add("Xa6f8"); //Swiss French dialect
            hs.add("Xa6f9"); //Italian language
            hs.add("Xa6fA"); //Moldavian language
            hs.add("Xa6fB"); //Portuguese language
            hs.add("Xa6fC"); //Galician Portuguese dialect
            hs.add("Xa6fD"); //Provencal language
            hs.add("Xa6fE"); //Rhaeto-Romanic language
            hs.add("Xa6fF"); //Rumanian language
            hs.add("Xa6fG"); //Sardinian language
            hs.add("Xa6fH"); //Spanish language
            hs.add("Xa6fI"); //Slavic language
            hs.add("Xa6fJ"); //Eastern Slavic language
            hs.add("Xa6fK"); //Belorussian language
            hs.add("Xa6fL"); //Russian language
            hs.add("Xa6fM"); //Ukrainian language
            hs.add("Xa6fN"); //Southern Slavic language
            hs.add("Xa6fO"); //Bulgarian language
            hs.add("Xa6fP"); //Macedonian language
            hs.add("Xa6fQ"); //Serbo-Croatian language
            hs.add("Xa6fR"); //Slovenian language
            hs.add("Xa6fS"); //Western Slavic language
            hs.add("Xa6fT"); //Czech language
            hs.add("Xa6fU"); //Polish language
            hs.add("Xa6fV"); //Slovak language
            hs.add("Xa6fW"); //Sorbian language
            hs.add("Xa6fX"); //Indo-Iranian language
            hs.add("Xa6fY"); //Indic language
            hs.add("Xa6fZ"); //Assamese language
            hs.add("Xa6fa"); //Bhili language
            hs.add("Xa6fc"); //Sylhety
            hs.add("Xa6fe"); //Bihari language
            hs.add("Xa6fh"); //Hindustani language
            hs.add("Xa6fi"); //Kashmiri language
            hs.add("Xa6fj"); //Konkani language
            hs.add("Xa6fk"); //Maldivian language
            hs.add("Xa6fl"); //Marathi language
            hs.add("Xa6fm"); //Nepali language
            hs.add("Xa6fn"); //Oriya language
            hs.add("Xa6fp"); //Pakistani punjabi language
            hs.add("Xa6fq"); //Lehnda punjabi language
            hs.add("Xa6fr"); //Sikh punjabi language
            hs.add("Xa6fs"); //Rajasthani language
            hs.add("Xa6ft"); //Romany language
            hs.add("Xa6fu"); //Sanskrit language
            hs.add("Xa6fv"); //Sindhi language
            hs.add("Xa6fw"); //Sinhalese language
            hs.add("Xa6fy"); //Iranian language
            hs.add("Xa6fz"); //Baluchi language
            hs.add("Xa6g0"); //Gilaki language
            hs.add("Xa6g1"); //Kurdish language
            hs.add("Xa6g2"); //Mazanderani language
            hs.add("Xa6g3"); //Ossetian language
            hs.add("Xa6g5"); //Persian language
            hs.add("Xa6g6"); //Tajik language
            hs.add("Xa6g7"); //Khoisan language
            hs.add("Xa6g8"); //Bushman language
            hs.add("Xa6g9"); //Hatsa language
            hs.add("Xa6gA"); //Hottentot language
            hs.add("Xa6gB"); //Sandawe language
            hs.add("Xa6gC"); //Maban language
            hs.add("Xa6gD"); //Maba language
            hs.add("Xa6gE"); //Malayo-Polynesian language
            hs.add("Xa6gF"); //Indonesian language
            hs.add("Xa6gG"); //Achinese language
            hs.add("Xa6gH"); //Balinese language
            hs.add("Xa6gI"); //Batak language
            hs.add("Xa6gJ"); //Bikol language
            hs.add("Xa6gK"); //Buginese language
            hs.add("Xa6gL"); //Cham language
            hs.add("Xa6gM"); //Chamorro language
            hs.add("Xa6gN"); //Hocano language
            hs.add("Xa6gO"); //Igorot language
            hs.add("Xa6gP"); //Jarai language
            hs.add("Xa6gQ"); //Javanese language
            hs.add("Xa6gR"); //Madurese language
            hs.add("Xa6gS"); //Malagasy language
            hs.add("Xa6gT"); //Malay language
            hs.add("Xa6gU"); //Maranao language
            hs.add("Xa6gV"); //Minangkabau language
            hs.add("Xa6gW"); //Palau language
            hs.add("Xa6gX"); //Pampangan language
            hs.add("Xa6gY"); //Pangasinan language
            hs.add("Xa6gZ"); //Rhade language
            hs.add("Xa6ga"); //Sundanese language
            hs.add("Xa6gb"); //Tagalog language
            hs.add("Xa6gc"); //Visayan language
            hs.add("Xa6gd"); //Micronesian language
            hs.add("Xa6ge"); //Gilbertese language
            hs.add("Xa6gf"); //Marshallese language
            hs.add("Xa6gg"); //Nauruan language
            hs.add("Xa6gh"); //Ponapean language
            hs.add("Xa6gi"); //Trukese language
            hs.add("Xa6gj"); //Yapese language
            hs.add("Xa6gk"); //Melanesian language
            hs.add("Xa6gl"); //Fijian language
            hs.add("Xa6gm"); //Motu language
            hs.add("Xa6gn"); //Tolai language
            hs.add("Xa6go"); //Yabim language
            hs.add("Xa6gp"); //Polynesian language
            hs.add("Xa6gq"); //Hawaiian language
            hs.add("Xa6gr"); //Maori language
            hs.add("Xa6gs"); //Marquesan language
            hs.add("Xa6gt"); //Niuean language
            hs.add("Xa6gu"); //Rarotongan language
            hs.add("Xa6gv"); //Samoan language
            hs.add("Xa6gw"); //Tahitian language
            hs.add("Xa6gx"); //Tongan language
            hs.add("Xa6gy"); //Tuamotu language
            hs.add("Xa6gz"); //Uvea language
            hs.add("Xa6h0"); //Mon-Khmer language
            hs.add("Xa6h1"); //Bahnar language
            hs.add("Xa6h2"); //Khasi language
            hs.add("Xa6h3"); //Khmer language
            hs.add("Xa6h4"); //Mon language
            hs.add("Xa6h5"); //Nicobarese language
            hs.add("Xa6h6"); //Palaung language
            hs.add("Xa6h7"); //Sedang language
            hs.add("Xa6h8"); //Wa language
            hs.add("Xa6h9"); //Munda language
            hs.add("Xa6hA"); //Ho language
            hs.add("Xa6hB"); //Korku language
            hs.add("Xa6hC"); //Mundari language
            hs.add("Xa6hD"); //Santali language
            hs.add("Xa6hE"); //Savara language
            hs.add("Xa6hF"); //Niger-Congo language
            hs.add("Xa6hG"); //Adamawa-Eastern language
            hs.add("Xa6hH"); //Adamawa language
            hs.add("Xa6hI"); //Mbum language
            hs.add("Xa6hJ"); //Eastern language (Niger-Congo)
            hs.add("Xa6hK"); //Banda language
            hs.add("Xa6hL"); //Gbaya language
            hs.add("Xa6hM"); //Sango language
            hs.add("Xa6hN"); //Zande language
            hs.add("Xa6hO"); //Benue-Congo language
            hs.add("Xa6hP"); //Bantu language
            hs.add("Xa6hQ"); //Ambo language
            hs.add("Xa6hR"); //Bemba language
            hs.add("Xa6hS"); //Bubi language
            hs.add("Xa6hT"); //Bulu language
            hs.add("Xa6hU"); //Chagga language
            hs.add("Xa6hV"); //Chiga language
            hs.add("Xa6hW"); //Chokwe language
            hs.add("Xa6hX"); //Duala language
            hs.add("Xa6hY"); //Fang language
            hs.add("Xa6hZ"); //Ganda language
            hs.add("Xa6ha"); //Gisu language
            hs.add("Xa6hb"); //Hehe language
            hs.add("Xa6hc"); //Herero language
            hs.add("Xa6hd"); //Kamba language
            hs.add("Xa6he"); //Kikuyu language
            hs.add("Xa6hf"); //Kisii language
            hs.add("Xa6hg"); //Kongo language
            hs.add("Xa6hh"); //Lingala language
            hs.add("Xa6hi"); //Lomwe language
            hs.add("Xa6hj"); //Lozi language
            hs.add("Xa6hk"); //Luba language
            hs.add("Xa6hl"); //Luhya language
            hs.add("Xa6hm"); //Lunda language
            hs.add("Xa6hn"); //Lwea language
            hs.add("Xa6ho"); //Makonde language
            hs.add("Xa6hp"); //Makua language
            hs.add("Xa6hq"); //Mbundu language
            hs.add("Xa6hr"); //Meru language
            hs.add("Xa6hs"); //Mongo language
            hs.add("Xa6ht"); //Ndebele language
            hs.add("Xa6hu"); //Ngala language
            hs.add("Xa6hv"); //Nkole language
            hs.add("Xa6hw"); //Nyamwezi language
            hs.add("Xa6hx"); //Nyanja language
            hs.add("Xa6hy"); //Nyoro language
            hs.add("Xa6hz"); //Pedi language
            hs.add("Xa6i0"); //Ruanda language
            hs.add("Xa6i1"); //Rundi language
            hs.add("Xa6i2"); //Shona language
            hs.add("Xa6i3"); //Sotho language
            hs.add("Xa6i4"); //Sukuma language
            hs.add("Xa6i5"); //Swahili language
            hs.add("Xa6i6"); //Swazi language
            hs.add("Xa6i7"); //Tonga language
            hs.add("Xa6i8"); //Toro language
            hs.add("Xa6i9"); //Tsonga language
            hs.add("Xa6iA"); //Tswana language
            hs.add("Xa6iB"); //Tumbuka language
            hs.add("Xa6iC"); //Venda language
            hs.add("Xa6iD"); //Xhosa language
            hs.add("Xa6iE"); //Yao language - Bantu
            hs.add("Xa6iF"); //Yaunde language
            hs.add("Xa6iG"); //Zulu language
            hs.add("Xa6iH"); //Efik language
            hs.add("Xa6iI"); //Ibibio language
            hs.add("Xa6iJ"); //Tiv language
            hs.add("Xa6iK"); //Ijo language
            hs.add("Xa6iL"); //Western Sudanic language
            hs.add("Xa6iM"); //Gur language
            hs.add("Xa6iN"); //Bariba language
            hs.add("Xa6iO"); //Dagomba language
            hs.add("Xa6iP"); //Gurma language
            hs.add("Xa6iQ"); //Kabre language
            hs.add("Xa6iR"); //Mossi language
            hs.add("Xa6iS"); //Senufo language
            hs.add("Xa6iT"); //Kwa language
            hs.add("Xa6iU"); //Adangme language
            hs.add("Xa6iV"); //Agni language
            hs.add("Xa6iW"); //Bassa language
            hs.add("Xa6iX"); //Baule language
            hs.add("Xa6iY"); //Edo language
            hs.add("Xa6iZ"); //Ewe language
            hs.add("Xa6ia"); //Fanti language
            hs.add("Xa6ib"); //Fon language
            hs.add("Xa6ic"); //Ga language
            hs.add("Xa6id"); //Grebo language
            hs.add("Xa6ie"); //Ibo language
            hs.add("Xa6if"); //Idoma language
            hs.add("Xa6ig"); //Kru language
            hs.add("Xa6ih"); //Nupe language
            hs.add("Xa6ii"); //Twi language
            hs.add("Xa6ij"); //Urhobo language
            hs.add("Xa6ik"); //Yoruba language
            hs.add("Xa6il"); //Mande language
            hs.add("Xa6im"); //Bambara language
            hs.add("Xa6in"); //Dyula language
            hs.add("Xa6io"); //Kpelle language
            hs.add("Xa6ip"); //Loma language
            hs.add("Xa6iq"); //Malinke language
            hs.add("Xa6ir"); //Mende language
            hs.add("Xa6is"); //Soninke language
            hs.add("Xa6it"); //Susu language
            hs.add("Xa6iu"); //Vai language
            hs.add("Xa6iv"); //West Atlantic language
            hs.add("Xa6iw"); //Balante language
            hs.add("Xa6ix"); //Dyola language
            hs.add("Xa6iy"); //Fulani language
            hs.add("Xa6iz"); //Gola language
            hs.add("Xa6j0"); //Kissi language
            hs.add("Xa6j1"); //Serer language
            hs.add("Xa6j2"); //Temne language
            hs.add("Xa6j3"); //Wolof language
            hs.add("Xa6j4"); //Native North American language
            hs.add("Xa6j5"); //Algonkian language
            hs.add("Xa6j6"); //Arapaho language
            hs.add("Xa6j7"); //Blackfoot language
            hs.add("Xa6j8"); //Cheyenne language
            hs.add("Xa6j9"); //Cree language
            hs.add("Xa6jA"); //Delaware language
            hs.add("Xa6jB"); //Fox language
            hs.add("Xa6jC"); //Micmac language
            hs.add("Xa6jD"); //Naskapi language
            hs.add("Xa6jE"); //Ojibwa language
            hs.add("Xa6jF"); //Passamaquoddy language
            hs.add("Xa6jG"); //Athapascan language
            hs.add("Xa6jH"); //Apache language
            hs.add("Xa6jI"); //Carrier language
            hs.add("Xa6jJ"); //Chilcotin language
            hs.add("Xa6jK"); //Chipewyan language
            hs.add("Xa6jL"); //Navajo language
            hs.add("Xa6jM"); //Caddoan language
            hs.add("Xa6jN"); //Caddo language
            hs.add("Xa6jO"); //Pawnee language
            hs.add("Xa6jP"); //Haida language
            hs.add("Xa6jQ"); //Iroquoian language
            hs.add("Xa6jR"); //Cherokee language
            hs.add("Xa6jS"); //Mohawk language
            hs.add("Xa6jT"); //Oneida language
            hs.add("Xa6jU"); //Seneca language
            hs.add("Xa6jV"); //Keresan language
            hs.add("Xa6jW"); //Mayan language
            hs.add("Xa6jX"); //Cakchiquel language
            hs.add("Xa6jY"); //Chol language
            hs.add("Xa6jZ"); //Chontal language
            hs.add("Xa6ja"); //Huastec language
            hs.add("Xa6jb"); //Kekchi language
            hs.add("Xa6jc"); //Mam language
            hs.add("Xa6jd"); //Maya language
            hs.add("Xa6je"); //Quiche language
            hs.add("Xa6jf"); //Tzeltal language
            hs.add("Xa6jg"); //Tzotzil language
            hs.add("Xa6jh"); //Muskogean language
            hs.add("Xa6ji"); //Chickasaw language
            hs.add("Xa6jj"); //Choctaw language
            hs.add("Xa6jk"); //Creek language
            hs.add("Xa6jl"); //Seminole language
            hs.add("Xa6jm"); //Oto-Manguean language
            hs.add("Xa6jn"); //Chinantec language
            hs.add("Xa6jo"); //Mazahua language
            hs.add("Xa6jp"); //Mazatec language
            hs.add("Xa6jq"); //Mixtec language
            hs.add("Xa6jr"); //Otomi language
            hs.add("Xa6js"); //Zapotec language
            hs.add("Xa6jt"); //Penutian language
            hs.add("Xa6ju"); //Klamath language
            hs.add("Xa6jv"); //Nez Perce language
            hs.add("Xa6jw"); //Tsimshian language
            hs.add("Xa6jx"); //Yakima language
            hs.add("Xa6jy"); //Salishan language
            hs.add("Xa6jz"); //Flathead language
            hs.add("Xa6k0"); //Lillooet language
            hs.add("Xa6k1"); //Okanagan language
            hs.add("Xa6k2"); //Shuswap language
            hs.add("Xa6k3"); //Thompson language
            hs.add("Xa6k4"); //Siouan language
            hs.add("Xa6k5"); //Assiniboin language
            hs.add("Xa6k6"); //Crow language
            hs.add("Xa6k7"); //Omaha language
            hs.add("Xa6k8"); //Osage language
            hs.add("Xa6k9"); //Sioux language
            hs.add("Xa6kA"); //Winnebago language
            hs.add("Xa6kB"); //Tanoan language
            hs.add("Xa6kC"); //Tewa language
            hs.add("Xa6kD"); //Tiwa language
            hs.add("Xa6kE"); //Towa language
            hs.add("Xa6kF"); //Tarasco language
            hs.add("Xa6kG"); //Tlingit language
            hs.add("Xa6kH"); //Totonac language
            hs.add("Xa6kI"); //Uto-Aztecan language
            hs.add("Xa6kJ"); //Comanche language
            hs.add("Xa6kK"); //Hopi language
            hs.add("Xa6kL"); //Kiowa language
            hs.add("Xa6kM"); //Mayo language
            hs.add("Xa6kN"); //Nahuatl language
            hs.add("Xa6kO"); //Paiute language
            hs.add("Xa6kP"); //Papago language
            hs.add("Xa6kQ"); //Pima language
            hs.add("Xa6kR"); //Shoshone language
            hs.add("Xa6kS"); //Tarahumara language
            hs.add("Xa6kT"); //Ute language
            hs.add("Xa6kU"); //Wakashan language
            hs.add("Xa6kV"); //Kwakiutl language
            hs.add("Xa6kW"); //Nootka language
            hs.add("Xa6kX"); //Yuman language
            hs.add("Xa6kY"); //Mohave language
            hs.add("Xa6kZ"); //Yuma language
            hs.add("Xa6ka"); //Zoquean language
            hs.add("Xa6kb"); //Mixe language
            hs.add("Xa6kc"); //Zoque language
            hs.add("Xa6kd"); //Zunian language
            hs.add("Xa6ke"); //Zuni language
            hs.add("Xa6kf"); //Paleo-Asiatic language
            hs.add("Xa6kg"); //Chukchi language
            hs.add("Xa6kh"); //Itelmen language
            hs.add("Xa6ki"); //Ket language
            hs.add("Xa6kj"); //Koryak language
            hs.add("Xa6kk"); //Nivkh language
            hs.add("Xa6kl"); //Yukagir language
            hs.add("Xa6km"); //Papuan language
            hs.add("Xa6kn"); //Baining language
            hs.add("Xa6ko"); //Chimbu language
            hs.add("Xa6kp"); //Enga language
            hs.add("Xa6kq"); //Hagen language
            hs.add("Xa6kr"); //Kate language
            hs.add("Xa6ks"); //Marind language
            hs.add("Xa6kt"); //Nimboran language
            hs.add("Xa6ku"); //Pidgin and creole language
            hs.add("Xa6kv"); //Fanakalo language
            hs.add("Xa6kw"); //French creole language
            hs.add("Xa6kx"); //Kituba language
            hs.add("Xa6ky"); //Krio language
            hs.add("Xa6kz"); //Papiamento language
            hs.add("Xa6l0"); //Pidgin English language
            hs.add("Xa6l1"); //Police Motu language
            hs.add("Xa6l2"); //Saramacca language
            hs.add("Xa6l3"); //Taki-Taki language
            hs.add("Xa6l4"); //Saharan language
            hs.add("Xa6l5"); //Kanuri language
            hs.add("Xa6l6"); //Teda language
            hs.add("Xa6l7"); //Sino-Tibetan language
            hs.add("Xa6l8"); //Miao-Yao language
            hs.add("Xa6l9"); //Miao language
            hs.add("Xa6lA"); //Sinitic language
            hs.add("Xa6lB"); //Chinese language
            hs.add("Xa6lD"); //Cantonese Chinese dialect
            hs.add("Xa6lE"); //Fukienese dialect
            hs.add("Xa6lF"); //Northern Fukienese dialect
            hs.add("Xa6lG"); //Southern Fukienese dialect
            hs.add("Xa6lH"); //Hakka dialect
            hs.add("Xa6lI"); //Hsiang dialect
            hs.add("Xa6lJ"); //Mandarin dialect
            hs.add("Xa6lK"); //Wu dialect
            hs.add("Xa6lL"); //Tai language
            hs.add("Xa6lM"); //Chuang language
            hs.add("Xa6lN"); //Lao language
            hs.add("Xa6lO"); //Nung language
            hs.add("Xa6lP"); //Puyi language
            hs.add("Xa6lQ"); //Shan language
            hs.add("Xa6lR"); //Thai language
            hs.add("Xa6lS"); //Tung language
            hs.add("Xa6lT"); //Tibeto-Burman language
            hs.add("Xa6lU"); //Bodo language
            hs.add("Xa6lV"); //Burmese language
            hs.add("Xa6lW"); //Chin language
            hs.add("Xa6lX"); //Garo language
            hs.add("Xa6lY"); //Jonkha language
            hs.add("Xa6lZ"); //Kachin language
            hs.add("Xa6la"); //Karen language
            hs.add("Xa6lb"); //Lahu language
            hs.add("Xa6lc"); //Lepcha language
            hs.add("Xa6ld"); //Lisu language
            hs.add("Xa6le"); //Lushei language
            hs.add("Xa6lf"); //Meithei language
            hs.add("Xa6lg"); //Murmi language
            hs.add("Xa6lh"); //Nakhi language
            hs.add("Xa6li"); //Newari language
            hs.add("Xa6lj"); //Tibetan language
            hs.add("Xa6lk"); //Yi language
            hs.add("Xa6ll"); //Songhai language
            hs.add("Xa6lm"); //Uralic language
            hs.add("Xa6ln"); //Finno-Ugric language
            hs.add("Xa6lo"); //Finnic language
            hs.add("Xa6lp"); //Estonian language
            hs.add("Xa6lq"); //Finnish language
            hs.add("Xa6lr"); //Komi language
            hs.add("Xa6ls"); //Lappish language
            hs.add("Xa6lt"); //Mari language
            hs.add("Xa6lu"); //Mordvin language
            hs.add("Xa6lv"); //Udmurt language
            hs.add("Xa6lw"); //Ugric language
            hs.add("Xa6lx"); //Hungarian language
            hs.add("Xa6ly"); //Ostyak language
            hs.add("Xa6lz"); //Vogul language
            hs.add("Xa6m0"); //Samoyed language
            hs.add("Xa6m1"); //Enets language
            hs.add("Xa6m2"); //Nenets language
            hs.add("Xa6m3"); //Nganasan language
            hs.add("Xa6m4"); //Selkup language
            hs.add("Xa6m5"); //Language commonly spoken in Europe
            hs.add("Xa6m7"); //Yao language - Sino-Tibet
            hs.add("Xa6m8"); //Swiss German dialect
            hs.add("Xa6nz"); //Kashubian Polish dialect
            hs.add("XaEF2"); //Language not given - patient refused
            hs.add("XaELA"); //Creole language
            hs.add("XaELB"); //Mirpuri language
            hs.add("XaG5p"); //Main spoken language Arabic
            hs.add("XaG5q"); //Main spoken language Bengali
            hs.add("XaG5r"); //Main spoken language Cantonese
            hs.add("XaG5s"); //Main spoken language Czech
            hs.add("XaG5t"); //Main spoken language English
            hs.add("XaG5u"); //Main spoken language French
            hs.add("XaG5v"); //Main spoken language Gujarati
            hs.add("XaG5w"); //Main spoken language Hausa
            hs.add("XaG5x"); //Main spoken language Hindi
            hs.add("XaG5y"); //Main spoken language Iban
            hs.add("XaG5z"); //Main spoken language Kutchi
            hs.add("XaG60"); //Main spoken language Mandarin
            hs.add("XaG61"); //Main spoken language Polish
            hs.add("XaG62"); //Main spoken language Portuguese
            hs.add("XaG63"); //Main spoken language Punjabi
            hs.add("XaG64"); //Main spoken language Russian
            hs.add("XaG65"); //Main spoken language Somali
            hs.add("XaG66"); //Main spoken language Spanish
            hs.add("XaG67"); //Main spoken language Swahili
            hs.add("XaG68"); //Main spoken language Sylheti
            hs.add("XaG69"); //Main spoken language Tamil
            hs.add("XaG6A"); //Main spoken language Urdu
            hs.add("XaG6B"); //Main spoken language Yoruba
            hs.add("XaILE"); //Using British sign language
            hs.add("XaIpr"); //Main spoken language Farsi
            hs.add("XaIps"); //Main spoken language Kurdish
            hs.add("XaIpt"); //Main spoken language Shona
            hs.add("XaJD5"); //Main spoken language Italian
            hs.add("XaJD6"); //Main spoken language German
            hs.add("XaJDK"); //Main spoken language Albanian
            hs.add("XaJDL"); //Main spoken language Croatian
            hs.add("XaJDM"); //Main spoken language Greek
            hs.add("XaJDN"); //Main spoken language Japanese
            hs.add("XaJDO"); //Main spoken language Korean
            hs.add("XaJDP"); //Main spoken language Lithuanian
            hs.add("XaJDQ"); //Main spoken language Turkish
            hs.add("XaJDR"); //Main spoken language Ukrainian
            hs.add("XaJDS"); //Main spoken language Vietnamese
            hs.add("XaJOq"); //Main spoken language Akan
            hs.add("XaJOr"); //Main spoken language Amharic
            hs.add("XaJOs"); //Main spoken language Brawa
            hs.add("XaJOt"); //Main spoken language Dutch
            hs.add("XaJOu"); //Main spoken language Ethiopian
            hs.add("XaJOv"); //Main spoken language Flemish
            hs.add("XaJOw"); //Main spoken language French Creole
            hs.add("XaJOx"); //Main spoken language Gaelic
            hs.add("XaJOy"); //Main spoken language Hakka
            hs.add("XaJOz"); //Main spoken language Hebrew
            hs.add("XaJP0"); //Main spoken language Igbo
            hs.add("XaJP1"); //Main spoken language Lingala
            hs.add("XaJP2"); //Main spoken language Luganda
            hs.add("XaJP3"); //Main spoken language Malayalam
            hs.add("XaJP4"); //Main spoken language Norwegian
            hs.add("XaJP5"); //Main spoken language Pashto
            hs.add("XaJP6"); //Main spoken language Patois
            hs.add("XaJP7"); //Main spoken language Serbian
            hs.add("XaJP8"); //Main spoken language Sinhala
            hs.add("XaJP9"); //Main spoken language Swedish
            hs.add("XaJPA"); //Main spoken language Tagalog
            hs.add("XaJPB"); //Main spoken language Thai
            hs.add("XaJPC"); //Main spoken language Tigrinya
            hs.add("XaJPD"); //Main spoken language Welsh
            hs.add("XaJPI"); //Using Makaton sign language
            hs.add("XaKIL"); //Main spoken language Finnish
            hs.add("XaP48"); //Main spoken language Bulgarian
            hs.add("XaP49"); //Main spoken language Romanian
            hs.add("XaP9z"); //Main spoken language Slovak
            hs.add("XaPA2"); //Main spoken language Ndebele
            hs.add("XaPF7"); //Main spoken language Oromo
            hs.add("XaPF8"); //Main spoken language Abkhaz
            hs.add("XaPF9"); //Main spoken language Afar
            hs.add("XaPFA"); //Main spoken language Afrikaans
            hs.add("XaPFB"); //Main spoken language Armenian
            hs.add("XaPFC"); //Main spoken language Assamese
            hs.add("XaPFD"); //Main spoken language Aymara
            hs.add("XaPFF"); //Main spoken language Azerbaijani
            hs.add("XaPFG"); //Main spoken language Bashkir
            hs.add("XaPFH"); //Main spoken language Basque
            hs.add("XaPFI"); //Main spoken language Dzongkha
            hs.add("XaPFK"); //Main spoken language Bihari
            hs.add("XaPFL"); //Main spoken language Bislama
            hs.add("XaPFM"); //Main spoken language Breton
            hs.add("XaPFN"); //Main spoken language Burmese
            hs.add("XaPFO"); //Main spoken language Belarusian
            hs.add("XaPFP"); //Main spoken language Central Khmer
            hs.add("XaPFQ"); //Main spoken language Catalan
            hs.add("XaPFT"); //Main spoken language Corsican
            hs.add("XaPFU"); //Main spoken language Danish
            hs.add("XaPFV"); //Main spoken language Esperanto
            hs.add("XaPFW"); //Main spoken language Estonian
            hs.add("XaPFX"); //Main spoken language Faeroese
            hs.add("XaPFZ"); //Main spoken language Fijian
            hs.add("XaPFc"); //Main spoken language Frisian
            hs.add("XaPFd"); //Main spoken language Galician
            hs.add("XaPFe"); //Main spoken language Georgian
            hs.add("XaPFf"); //Main spoken language Kalaallisut
            hs.add("XaPFg"); //Main spoken language Guarani
            hs.add("XaPFh"); //Main spoken language Hungarian
            hs.add("XaPFi"); //Main spoken language Icelandic
            hs.add("XaPFj"); //Main spoken language Indonesian
            hs.add("XaPFk"); //Main spoken language Interlingua
            hs.add("XaPFl"); //Main spoken language Interlingue
            hs.add("XaPFm"); //Main spoken language Inupiaq
            hs.add("XaPFn"); //Main spoken language Inuktitut
            hs.add("XaPFo"); //Main spoken language Irish
            hs.add("XaPFp"); //Main spoken language Javanese
            hs.add("XaPFq"); //Main spoken language Kannada
            hs.add("XaPFr"); //Main spoken language Kashmiri
            hs.add("XaPFs"); //Main spoken language Kazakh
            hs.add("XaPFt"); //Main spoken language Kinyarwanda
            hs.add("XaPFu"); //Main spoken language Kirghiz
            hs.add("XaPFv"); //Main spoken language Rundi
            hs.add("XaPFw"); //Main spoken language Lao
            hs.add("XaPFy"); //Main spoken language Latvian
            hs.add("XaPFz"); //Main spoken language Macedonian
            hs.add("XaPG0"); //Main spoken language Malagasy
            hs.add("XaPG1"); //Main spoken language Malay
            hs.add("XaPG2"); //Main spoken language Maltese
            hs.add("XaPG3"); //Main spoken language Maori
            hs.add("XaPG4"); //Main spoken language Marathi
            hs.add("XaPG5"); //Main spoken language Moldavian
            hs.add("XaPG6"); //Main spoken language Mongolian
            hs.add("XaPG7"); //Main spoken language Nauru
            hs.add("XaPG8"); //Main spoken language Nepali
            hs.add("XaPG9"); //Main spoken language Occitan
            hs.add("XaPGA"); //Main spoken language Oriya
            hs.add("XaPGC"); //Main spoken language Quechua
            hs.add("XaPGD"); //Main spoken language Romansh
            hs.add("XaPGE"); //Main spoken language Samoan
            hs.add("XaPGF"); //Main spoken language Sango
            hs.add("XaPGI"); //Main spoken language Southern Sotho
            hs.add("XaPGJ"); //Main spoken language Tswana
            hs.add("XaPGK"); //Main spoken language Sindhi
            hs.add("XaPGM"); //Main spoken language Swati
            hs.add("XaPGN"); //Main spoken language Slovenian
            hs.add("XaPGO"); //Main spoken language Sundanese
            hs.add("XaPGP"); //Main spoken language Tajik
            hs.add("XaPGQ"); //Main spoken language Tatar
            hs.add("XaPGR"); //Main spoken language Telugu
            hs.add("XaPGS"); //Main spoken language Tibetan
            hs.add("XaPGT"); //Main spoken language Tongan
            hs.add("XaPGU"); //Main spoken language Tsonga
            hs.add("XaPGV"); //Main spoken language Turkmen
            hs.add("XaPGW"); //Main spoken language Twi
            hs.add("XaPGX"); //Main spoken language Uighur
            hs.add("XaPGY"); //Main spoken language Uzbek
            hs.add("XaPGa"); //Main spoken language Wolof
            hs.add("XaPGb"); //Main spoken language Xhosa
            hs.add("XaPGc"); //Main spoken language Yiddish
            hs.add("XaPGd"); //Main spoken language Zhuang
            hs.add("XaPGe"); //Main spoken language Zulu
            hs.add("XaPGh"); //Main spoken language [A - M (subdivided)]
            hs.add("XaPGi"); //Main spoken language [N - Z (subdivided)]
            hs.add("XaQHz"); //Main spoken language Filipino
            hs.add("XaQht"); //Main spoken language Hindko
            hs.add("XaRAe"); //Main spoken language Bamun
            hs.add("XaWQU"); //Main spoken language Kikuyu
            hs.add("XaWQV"); //Main spoken language Fulani
            hs.add("XaWQW"); //Main spoken language Nyanja
            hs.add("XaX3X"); //Main spoken language Tetum
            hs.add("XaX3Z"); //Main spoken language Konkani
            hs.add("XaX4p"); //Main spoken language Dari
            hs.add("XaYWG"); //Main spoken language Aragonese
            hs.add("XaYZ4"); //Language read not given - patient refused
            hs.add("Xacfm"); //Main spoken language Avaric
            hs.add("Xacfn"); //Main spoken language Avestan
            hs.add("Xacfo"); //Main spoken language Bambara
            hs.add("Xacfp"); //Main spoken language Bosnian
            hs.add("Xacfq"); //Main spoken language Chamorro
            hs.add("Xacfr"); //Main spoken language Chechen
            hs.add("Xacfs"); //Main spoken language Church Slavic
            hs.add("Xacft"); //Main spoken language Chuvash
            hs.add("Xacfu"); //Main spoken language Cornish
            hs.add("Xacfv"); //Main spoken language Cree
            hs.add("Xacfw"); //Main spoken language Dhivehi
            hs.add("Xacfy"); //Main spoken language Ewe
            hs.add("Xacg0"); //Main spoken language Western Frisian
            hs.add("Xacg2"); //Main spoken language Scottish Gaelic
            hs.add("Xacg3"); //Main spoken language Manx
            hs.add("Xacg5"); //Main spoken language Haitian
            hs.add("XackP"); //Main spoken language Herero
            hs.add("XackQ"); //Main spoken language Hiri Motu
            hs.add("XackR"); //Main spoken language Ido
            hs.add("XackS"); //Main spoken language Nuosu
            hs.add("XackT"); //Main spoken language Kanuri
            hs.add("XackU"); //Main spoken language Komi
            hs.add("XackV"); //Main spoken language Kongo
            hs.add("XackW"); //Main spoken language Kuanyama
            hs.add("XackX"); //Main spoken language Latin
            hs.add("XackY"); //Main spoken language Limburgan
            hs.add("XackZ"); //Main spoken language Luxembourgish
            hs.add("Xacka"); //Main spoken language Luba-Katanga
            hs.add("Xackc"); //Main spoken language Marshallese
            hs.add("Xacke"); //Main spoken language Navajo
            hs.add("Xackf"); //Main spoken language South Ndebele
            hs.add("Xackg"); //Main spoken language North Ndebele
            hs.add("Xackh"); //Main spoken language Ndonga
            hs.add("Xackj"); //Main spoken language Norwegian Nynorsk
            hs.add("Xackk"); //Main spoken language Norwegian Bokmal
            hs.add("Xackm"); //Main spoken language Ojibwa
            hs.add("Xacko"); //Main spoken language Ossetian
            hs.add("Xackq"); //Main spoken language Pali
            hs.add("Xackr"); //Main spoken language Pushto
            hs.add("Xacks"); //Main spoken language Sanskrit
            hs.add("Xackt"); //Main spoken language Northern Sami
            hs.add("Xacku"); //Main spoken language Sardinian
            hs.add("Xackw"); //Main spoken language Tahitian
            hs.add("Xacky"); //Main spoken language Venda
            hs.add("Xackz"); //Main spoken language Volapuk
            hs.add("Xacl0"); //Main spoken language Walloon
            hs.add("Xacl1"); //Main spoken language Chinese
            hs.add("Xacoa"); //Main spoken language A - F
            hs.add("Xacoc"); //Main spoken language G - M
            hs.add("Xacod"); //Main spoken language N - S
            hs.add("Xacoe"); //Main spoken language T - Z
            hs.add("XaedX"); //Main spoken language Romany
            hs.add("Y29d5"); //Does lip read

            hsTppLanguageCodes = hs;
        }

        return hsTppLanguageCodes.contains(code);
    }

    private static boolean isSnomedLanguageCode(Long snomedConceptId) {

        if (hsSnomedLanguageCodes == null) {

            Set<Long> hs = new HashSet<>();

            hs.add(new Long(408535006L)); //Main spoken language Serbian (finding)
            hs.add(new Long(408531002L)); //Main spoken language Pashto (finding)
            hs.add(new Long(698887005L)); //Main spoken language Aymara (finding)
            hs.add(new Long(698910004L)); //Main spoken language Maltese (finding)
            hs.add(new Long(698683006L)); //Main spoken language Tsonga (finding)
            hs.add(new Long(698651000L)); //Main spoken language Abkhazian (finding)
            hs.add(new Long(698930000L)); //Main spoken language Uzbek (finding)
            hs.add(new Long(315568007L)); //Main spoken language Cantonese (finding)
            hs.add(new Long(970481000000102L)); //Main spoken language Chamorro (finding)
            hs.add(new Long(698934009L)); //Main spoken language Yiddish (finding)
            hs.add(new Long(970711000000101L)); //Main spoken language Luba-Katanga (finding)
            hs.add(new Long(315571004L)); //Main spoken language French (finding)
            hs.add(new Long(407652002L)); //Main spoken language Greek (finding)
            hs.add(new Long(970911000000104L)); //Main spoken language Pushto (finding)
            hs.add(new Long(698915009L)); //Main spoken language Oriya (finding)
            hs.add(new Long(315582007L)); //Main spoken language Russian (finding)
            hs.add(new Long(698659003L)); //Main spoken language Estonian (finding)
            hs.add(new Long(698652007L)); //Main spoken language Afar (finding)
            hs.add(new Long(698892007L)); //Main spoken language Catalan (finding)
            hs.add(new Long(698657001L)); //Main spoken language Breton (finding)
            hs.add(new Long(408530001L)); //Main spoken language Norwegian (finding)
            hs.add(new Long(407648002L)); //Main spoken language Albanian (finding)
            hs.add(new Long(698907006L)); //Main spoken language Ruanda (finding)
            hs.add(new Long(698890004L)); //Main spoken language Bislama (finding)
            hs.add(new Long(970811000000106L)); //Main spoken language Northern Sami (finding)
            hs.add(new Long(408516006L)); //Main spoken language Swedish (finding)
            hs.add(new Long(315584008L)); //Main spoken language Spanish (finding)
            hs.add(new Long(698919003L)); //Main spoken language Sindhi (finding)
            hs.add(new Long(698893002L)); //Main spoken language Central Khmer (finding)
            hs.add(new Long(970461000000106L)); //Main spoken language Bambara (finding)
            hs.add(new Long(698667006L)); //Main spoken language Irish (finding)
            hs.add(new Long(698923006L)); //Main spoken language Swazi (finding)
            hs.add(new Long(698664004L)); //Main spoken language Interlingua (finding)
            hs.add(new Long(395109004L)); //Main spoken language Kurdish (finding)
            hs.add(new Long(408522002L)); //Main spoken language Gaelic (finding)
            hs.add(new Long(809341000000106L)); //Main spoken language Aragonese (finding)
            hs.add(new Long(408515005L)); //Main spoken language Ethiopian (finding)
            hs.add(new Long(408529006L)); //Main spoken language Malayalam (finding)
            hs.add(new Long(970931000000107L)); //Main spoken language Sardinian (finding)
            hs.add(new Long(970601000000105L)); //Main spoken language Haitian (finding)
            hs.add(new Long(970721000000107L)); //Main spoken language Luxembourgish (finding)
            hs.add(new Long(970531000000100L)); //Main spoken language Cornish (finding)
            hs.add(new Long(698653002L)); //Main spoken language Afrikaans (finding)
            hs.add(new Long(407655000L)); //Main spoken language Korean (finding)
            hs.add(new Long(408517002L)); //Main spoken language Tagalog (finding)
            hs.add(new Long(1036381000000101L)); //Main spoken language Romany (finding)
            hs.add(new Long(1036381000000101L)); //Main spoken language Romany (finding)
            hs.add(new Long(698661007L)); //Main spoken language Frisian (finding)
            hs.add(new Long(698901007L)); //Main spoken language Indonesian (finding)
            hs.add(new Long(970551000000107L)); //Main spoken language Dhivehi (finding)
            hs.add(new Long(315581000L)); //Main spoken language Punjabi (finding)
            hs.add(new Long(698678003L)); //Main spoken language Romanian (finding)
            hs.add(new Long(698898006L)); //Main spoken language Georgian (finding)
            hs.add(new Long(729051000000103L)); //Main spoken language Fulani (finding)
            hs.add(new Long(698896005L)); //Main spoken language Esperanto (finding)
            hs.add(new Long(698924000L)); //Main spoken language Tatar (finding)
            hs.add(new Long(698656005L)); //Main spoken language Bihari (finding)
            hs.add(new Long(698905003L)); //Main spoken language Kanarese (finding)
            hs.add(new Long(698679006L)); //Main spoken language Rundi (finding)
            hs.add(new Long(698671009L)); //Main spoken language Malagasy (finding)
            hs.add(new Long(718512007L)); //Main spoken language Romany (finding)
            hs.add(new Long(395110009L)); //Main spoken language Shona (finding)
            hs.add(new Long(315577000L)); //Main spoken language Kutchi (finding)
            hs.add(new Long(698658006L)); //Main spoken language Corsican (finding)
            hs.add(new Long(970561000000105L)); //Main spoken language Ewe (finding)
            hs.add(new Long(970611000000107L)); //Main spoken language Herero (finding)
            hs.add(new Long(698922001L)); //Main spoken language Southern Sotho (finding)
            hs.add(new Long(609092003L)); //Main spoken language Bamun (finding)
            hs.add(new Long(970751000000102L)); //Main spoken language Marshallese (finding)
            hs.add(new Long(970851000000105L)); //Main spoken language Ojibwa (finding)
            hs.add(new Long(970451000000108L)); //Main spoken language Avestan (finding)
            hs.add(new Long(698906002L)); //Main spoken language Kashmiri (finding)
            hs.add(new Long(315588006L)); //Main spoken language Urdu (finding)
            hs.add(new Long(698669009L)); //Main spoken language Lao (finding)
            hs.add(new Long(698886001L)); //Main spoken language Assamese (finding)
            hs.add(new Long(408533004L)); //Main spoken language Tigrinya (finding)
            hs.add(new Long(970991000000108L)); //Main spoken language Tahitian (finding)
            hs.add(new Long(970781000000108L)); //Main spoken language Ndonga (finding)
            hs.add(new Long(698682001L)); //Main spoken language Tajik (finding)
            hs.add(new Long(408534005L)); //Main spoken language Patois (finding)
            hs.add(new Long(970971000000109L)); //Main spoken language South Ndebele (finding)
            hs.add(new Long(407643006L)); //Main spoken language German (finding)
            hs.add(new Long(408519004L)); //Main spoken language Thai (finding)
            hs.add(new Long(698920009L)); //Main spoken language Slovak (finding)
            hs.add(new Long(971011000000109L)); //Main spoken language Venda (finding)
            hs.add(new Long(408513003L)); //Main spoken language Brawa (finding)
            hs.add(new Long(698677008L)); //Main spoken language Quechua (finding)
            hs.add(new Long(970651000000106L)); //Main spoken language Komi (finding)
            hs.add(new Long(407654001L)); //Main spoken language Japanese (finding)
            hs.add(new Long(698889008L)); //Main spoken language Bashkir (finding)
            hs.add(new Long(315586005L)); //Main spoken language Sylheti (finding)
            hs.add(new Long(407657008L)); //Main spoken language Turkish (finding)
            hs.add(new Long(970821000000100L)); //Main spoken language Norwegian Bokmål (finding)
            hs.add(new Long(970881000000104L)); //Main spoken language Pali (finding)
            hs.add(new Long(315587001L)); //Main spoken language Tamil (finding)
            hs.add(new Long(609095001L)); //Main spoken language Tetum (finding)
            hs.add(new Long(698914008L)); //Main spoken language Occitan (finding)
            hs.add(new Long(315572006L)); //Main spoken language Gujerati (finding)
            hs.add(new Long(698913002L)); //Main spoken language Ndebele (finding)
            hs.add(new Long(698894008L)); //Main spoken language Danish (finding)
            hs.add(new Long(971021000000103L)); //Main spoken language Volapük (finding)
            hs.add(new Long(315580004L)); //Main spoken language Portuguese (finding)
            hs.add(new Long(315574007L)); //Main spoken language Hausa (finding)
            hs.add(new Long(698672002L)); //Main spoken language Malay (finding)
            hs.add(new Long(698936006L)); //Main spoken language Zulu (finding)
            hs.add(new Long(698665003L)); //Main spoken language Inuktitut (finding)
            hs.add(new Long(698681008L)); //Main spoken language Sundanese (finding)
            hs.add(new Long(698916005L)); //Main spoken language Oromo (finding)
            hs.add(new Long(315575008L)); //Main spoken language Hindi (finding)
            hs.add(new Long(408523007L)); //Main spoken language Hakka (finding)
            hs.add(new Long(698673007L)); //Main spoken language Maori (finding)
            hs.add(new Long(698891000L)); //Main spoken language Burmese (finding)
            hs.add(new Long(698935005L)); //Main spoken language Chuang (finding)
            hs.add(new Long(698926003L)); //Main spoken language Tibetan (finding)
            hs.add(new Long(408525000L)); //Main spoken language Akan (finding)
            hs.add(new Long(971031000000101L)); //Main spoken language Walloon (finding)
            hs.add(new Long(408528003L)); //Main spoken language Dutch (finding)
            hs.add(new Long(315585009L)); //Main spoken language Swahili (finding)
            hs.add(new Long(698895009L)); //Main spoken language Jonkha (finding)
            hs.add(new Long(408514009L)); //Main spoken language Igbo (finding)
            hs.add(new Long(698928002L)); //Main spoken language Turkmen (finding)
            hs.add(new Long(698917001L)); //Main spoken language Romansh (finding)
            hs.add(new Long(698676004L)); //Main spoken language Nepali (finding)
            hs.add(new Long(698670005L)); //Main spoken language Macedonian (finding)
            hs.add(new Long(315566006L)); //Main spoken language Arabic (finding)
            hs.add(new Long(698903005L)); //Main spoken language Javanese (finding)
            hs.add(new Long(698929005L)); //Main spoken language Uigur (finding)
            hs.add(new Long(312954003L)); //Language not recorded (finding)
            hs.add(new Long(407656004L)); //Main spoken language Lithuanian (finding)
            hs.add(new Long(698909009L)); //Main spoken language Latvian (finding)
            hs.add(new Long(698932008L)); //Main spoken language Wolof (finding)
            hs.add(new Long(970541000000109L)); //Main spoken language Cree (finding)
            hs.add(new Long(729041000000101L)); //Main spoken language Nyanja (finding)
            hs.add(new Long(315583002L)); //Main spoken language Somali (finding)
            hs.add(new Long(970511000000108L)); //Main spoken language Church Slavic (finding)
            hs.add(new Long(609094002L)); //Main spoken language Konkani (finding)
            hs.add(new Long(729061000000100L)); //Main spoken language Kikuyu (finding)
            hs.add(new Long(408527008L)); //Main spoken language Luganda (finding)
            hs.add(new Long(698904004L)); //Main spoken language Kalaallisut (finding)
            hs.add(new Long(511841000000102L)); //Main spoken language Hindko (finding)
            hs.add(new Long(970701000000103L)); //Main spoken language Limburgan (finding)
            hs.add(new Long(407659006L)); //Main spoken language Ukrainian (finding)
            hs.add(new Long(970491000000100L)); //Main spoken language Chechen (finding)
            hs.add(new Long(503511000000100L)); //Main spoken language Filipino (finding)
            hs.add(new Long(408524001L)); //Main spoken language Hebrew (finding)
            hs.add(new Long(408507007L)); //Main spoken language Amharic (finding)
            hs.add(new Long(698885002L)); //Main spoken language Armenian (finding)
            hs.add(new Long(698668001L)); //Main spoken language Kazakh (finding)
            hs.add(new Long(414640006L)); //Main spoken language Finnish (finding)
            hs.add(new Long(910241000000102L)); //First language not English (finding)
            hs.add(new Long(315579002L)); //Main spoken language Polish (finding)
            hs.add(new Long(698897001L)); //Main spoken language Fijian (finding)
            hs.add(new Long(698908001L)); //Main spoken language Kirgiz (finding)
            hs.add(new Long(698675000L)); //Main spoken language Mongolian (finding)
            hs.add(new Long(970471000000104L)); //Main spoken language Bosnian (finding)
            hs.add(new Long(699945003L)); //Main spoken language Bulgarian (finding)
            hs.add(new Long(970771000000106L)); //Main spoken language Navajo (finding)
            hs.add(new Long(407650005L)); //Main spoken language Croatian (finding)
            hs.add(new Long(407661002L)); //Main spoken language Vietnamese (finding)
            hs.add(new Long(357001000000106L)); //Main spoken language Faeroese (finding)
            hs.add(new Long(357001000000106L)); //Main spoken language Faeroese (finding)
            hs.add(new Long(698927007L)); //Main spoken language Tongan (finding)
            hs.add(new Long(970871000000101L)); //Main spoken language Ossetian (finding)
            hs.add(new Long(408521009L)); //Main spoken language French Créole (finding)
            hs.add(new Long(698660008L)); //Main spoken language Faroese (finding)
            hs.add(new Long(970681000000100L)); //Main spoken language Kuanyama (finding)
            hs.add(new Long(764526007L)); //Refusal by patient to provide information about spoken language (situation)
            hs.add(new Long(315576009L)); //Main spoken language Iba (finding)
            hs.add(new Long(970661000000109L)); //Main spoken language Kongo (finding)
            hs.add(new Long(698900008L)); //Main spoken language Hungarian (finding)
            hs.add(new Long(698918006L)); //Main spoken language Sango (finding)
            hs.add(new Long(970501000000106L)); //Main spoken language Chinese (finding)
            hs.add(new Long(315570003L)); //Main spoken language English (finding)
            hs.add(new Long(970831000000103L)); //Main spoken language Norwegian Nynorsk (finding)
            hs.add(new Long(315569004L)); //Main spoken language Czech (finding)
            hs.add(new Long(970921000000105L)); //Main spoken language Sanskrit (finding)
            hs.add(new Long(315578005L)); //Main spoken language Mandarin (finding)
            hs.add(new Long(970441000000105L)); //Main spoken language Avaric (finding)
            hs.add(new Long(698655009L)); //Main spoken language Belarusian (finding)
            hs.add(new Long(408532009L)); //Main spoken language Welsh (finding)
            hs.add(new Long(970641000000108L)); //Main spoken language Kanuri (finding)
            hs.add(new Long(408526004L)); //Main spoken language Lingala (finding)
            hs.add(new Long(698663005L)); //Main spoken language Icelandic (finding)
            hs.add(new Long(698680009L)); //Main spoken language Samoan (finding)
            hs.add(new Long(698674001L)); //Main spoken language Marathi (finding)
            hs.add(new Long(609093008L)); //Main spoken language Dari (finding)
            hs.add(new Long(971041000000105L)); //Main spoken language Western Frisian (finding)
            hs.add(new Long(970961000000102L)); //Main spoken language Nuosu (finding)
            hs.add(new Long(970941000000103L)); //Main spoken language Scottish Gaelic (finding)
            hs.add(new Long(408520005L)); //Main spoken language Flemish (finding)
            hs.add(new Long(970801000000109L)); //Main spoken language Northern Ndebele (finding)
            hs.add(new Long(698654008L)); //Main spoken language Basque (finding)
            hs.add(new Long(698888000L)); //Main spoken language Azerbaijani (finding)
            hs.add(new Long(315567002L)); //Main spoken language Bengali (finding)
            hs.add(new Long(698925004L)); //Main spoken language Telugu (finding)
            hs.add(new Long(698912007L)); //Main spoken language Nauruan (finding)
            hs.add(new Long(698685004L)); //Main spoken language Twi (finding)
            hs.add(new Long(698684000L)); //Main spoken language Tswana (finding)
            hs.add(new Long(970631000000104L)); //Main spoken language Ido (finding)
            hs.add(new Long(698662000L)); //Main spoken language Galician (finding)
            hs.add(new Long(407642001L)); //Main spoken language Italian (finding)
            hs.add(new Long(698911000L)); //Main spoken language Moldavian (finding)
            hs.add(new Long(698899003L)); //Main spoken language Guarani (finding)
            hs.add(new Long(970621000000101L)); //Main spoken language Hiri Motu (finding)
            hs.add(new Long(698666002L)); //Main spoken language Inupiaq (finding)
            hs.add(new Long(395108007L)); //Main spoken language Farsi (finding)
            hs.add(new Long(970521000000102L)); //Main spoken language Chuvash (finding)
            hs.add(new Long(315589003L)); //Main spoken language Yoruba (finding)
            hs.add(new Long(970741000000100L)); //Main spoken language Manx (finding)
            hs.add(new Long(698921008L)); //Main spoken language Slovenian (finding)
            hs.add(new Long(408518007L)); //Main spoken language Sinhala (finding)
            hs.add(new Long(698902000L)); //Main spoken language Occidental (finding)
            hs.add(new Long(698933003L)); //Main spoken language Xhosa (finding)
            hs.add(new Long(970691000000103L)); //Main spoken language Latin (finding)

            hsSnomedLanguageCodes = hs;
        }

        return hsSnomedLanguageCodes.contains(snomedConceptId);
    }

    private static EthnicCategory findEthnicCategory(Coding coding) throws Exception {
        String system = coding.getSystem();
        String code = coding.getCode();

        if (system.equals(FhirCodeUri.CODE_SYSTEM_READ2)) {

            //need to check both Emis and Vision for this - note I've already verified that any code present in both Emis and Vision do have the same mappings
            //so it doesn't matter what order we do these checks in
            EthnicCategory ec = null;
            try {
                ec = VisionMappingHelper.findEthnicityCode(code);
            } catch (RuntimeException ex) {
                if (ex.getMessage() == null || !ex.getMessage().contains("Unknown ethnicity code")) {
                    throw ex;
                }
            }

            //if no luck with Vision, check Emis
            if (ec == null) {
                try {
                    ec = findEmisEthnicityCode(code);
                } catch (RuntimeException ex) {
                    if (ex.getMessage() == null || !ex.getMessage().contains("Unknown ethnicity code")) {
                        throw ex;
                    }
                }
            }

            return ec;

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_EMIS_CODE)) {
            return findEmisEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_CTV3)) {
            return TppMappingHelper.findEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_TPP_CTV3)) {
            return TppMappingHelper.findEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_VISION_CODE)) {
            return VisionMappingHelper.findEthnicityCode(code);

        } else {
            LOG.error("Unexpected code system " + system);
            return null;
        }
    }

    private static EthnicCategory findEmisEthnicityCode(String code) throws Exception {

        EmisClinicalCode c = new EmisClinicalCode();
        c.setAdjustedCode(code);
        return EmisMappingHelper.findEthnicityCode(c);
    }

    private static boolean isEthnicityCode(Coding coding, Resource resource, UUID patientId) throws Exception {

        String system = coding.getSystem();
        String code = coding.getCode();

        if (system.equals(FhirCodeUri.CODE_SYSTEM_READ2)) {

            //need to check Emis and Vision for this
            return isEmisEthnicityCode(code)
                    || VisionMappingHelper.isPotentialEthnicity(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_EMIS_CODE)) {
            return isEmisEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_CTV3)) {
            return TppMappingHelper.isEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_TPP_CTV3)) {
            return TppMappingHelper.isEthnicityCode(code);

        } else if (system.equals(FhirCodeUri.CODE_SYSTEM_VISION_CODE)) {
            return VisionMappingHelper.isPotentialEthnicity(code);

        } else {
            LOG.error("Unexpected code system " + system + " on resource " + resource.getResourceType() + " " + resource.getId() + " for patient " + patientId);
            return false;
        }
    }

    private static boolean isEmisEthnicityCode(String code) throws Exception {
        try {
            EmisClinicalCode c = new EmisClinicalCode();
            c.setAdjustedCode(code);
            EmisMappingHelper.findEthnicityCode(c);
            return true;

        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Unknown ethnicity code")) {
                return false;
            } else {
                throw ex;
            }
        }

    }

    private static List<String> findNhsNumbers(String sourceFile) throws Exception {

        List<String> ret = new ArrayList<>();

        Reader reader = Files.newBufferedReader(Paths.get(sourceFile));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());

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
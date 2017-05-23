package org.endeavourhealth.transform.enterprise.transforms;

import OpenPseudonymiser.Crypto;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.core.data.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.rdbms.eds.PatientLinkHelper;
import org.endeavourhealth.core.rdbms.eds.PatientLinkPair;
import org.endeavourhealth.core.rdbms.eds.PatientSearch;
import org.endeavourhealth.core.rdbms.eds.PatientSearchHelper;
import org.endeavourhealth.core.rdbms.reference.PostcodeHelper;
import org.endeavourhealth.core.rdbms.reference.PostcodeLookup;
import org.endeavourhealth.core.rdbms.transform.EnterpriseAgeUpdater;
import org.endeavourhealth.core.rdbms.transform.EnterpriseIdHelper;
import org.endeavourhealth.core.rdbms.transform.HouseholdHelper;
import org.endeavourhealth.core.rdbms.transform.PseudoIdHelper;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.transform.enterprise.EnterpriseTransformParams;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class PatientTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(PatientTransformer.class);

    private static final String PSEUDO_KEY_NHS_NUMBER = "NHSNumber";
    private static final String PSEUDO_KEY_PATIENT_NUMBER = "PatientNumber";
    private static final String PSEUDO_KEY_DATE_OF_BIRTH = "DOB";
    //private static final String PSEUDO_SALT_RESOURCE = "Endeavour Enterprise - East London.EncryptedSalt";

    private static final int BEST_ORG_SCORE = 10;

    private static Map<String, byte[]> saltCacheMap = new HashMap<>();
    //private static byte[] saltBytes = null;
    //private static ResourceRepository resourceRepository = new ResourceRepository();

    public boolean shouldAlwaysTransform() {
        return true;
    }

    public void transform(Long enterpriseId,
                          Resource resource,
                          AbstractEnterpriseCsvWriter csvWriter,
                          EnterpriseTransformParams params) throws Exception {

        Patient fhirPatient = (Patient)resource;

        String discoveryPersonId = PatientLinkHelper.getPersonId(fhirPatient.getId());

        //when the person ID table was populated, patients who had been deleted weren't added,
        //so we'll occasionally get null for some patients. If this happens, just do what would have
        //been done originally and assign an ID
        if (Strings.isNullOrEmpty(discoveryPersonId)) {
            PatientLinkPair pair = PatientLinkHelper.updatePersonId(fhirPatient);
            discoveryPersonId = pair.getNewPersonId();
        }

        Long enterprisePersonId = EnterpriseIdHelper.findOrCreateEnterprisePersonId(discoveryPersonId, params.getEnterpriseConfigName());

        long id;
        long organizationId;
        long personId;
        int patientGenderId;
        String pseudoId = null;
        String nhsNumber = null;
        Integer ageYears = null;
        Integer ageMonths = null;
        Integer ageWeeks = null;
        Date dateOfBirth = null;
        Date dateOfDeath = null;
        String postcode = null;
        String postcodePrefix = null;
        Long householdId = null;
        String lsoaCode = null;
        String msoaCode = null;

        id = enterpriseId.longValue();
        organizationId = params.getEnterpriseOrganisationId().longValue();
        personId = enterprisePersonId.longValue();

        //Calendar cal = Calendar.getInstance();

        dateOfBirth = fhirPatient.getBirthDate();
        /*cal.setTime(dob);
        int yearOfBirth = cal.get(Calendar.YEAR);
        model.setYearOfBirth(yearOfBirth);*/

        if (fhirPatient.hasDeceasedDateTimeType()) {
            dateOfDeath = fhirPatient.getDeceasedDateTimeType().getValue();
            /*cal.setTime(dod);
            int yearOfDeath = cal.get(Calendar.YEAR);
            model.setYearOfDeath(new Integer(yearOfDeath));*/
        } else if (fhirPatient.hasDeceased()
                && fhirPatient.getDeceased() instanceof DateType) {
            //should always be a DATE TIME type, but a bug in the CSV->FHIR transform
            //means we've got data with a DATE type too
            DateType d = (DateType)fhirPatient.getDeceased();
            dateOfDeath = d.getValue();
        }

        patientGenderId = fhirPatient.getGender().ordinal();


        if (fhirPatient.hasAddress()) {
            for (Address address: fhirPatient.getAddress()) {
                if (address.getUse() != null //got Homerton data will null address use
                        && address.getUse().equals(Address.AddressUse.HOME)) {
                    postcode = address.getPostalCode();
                    postcodePrefix = findPostcodePrefix(postcode);
                    householdId = HouseholdHelper.findOrCreateHouseholdId(params.getEnterpriseConfigName(), address);
                    break;
                }
            }
        }

        //if we've found a postcode, then get the LSOA etc. for it
        if (!Strings.isNullOrEmpty(postcode)) {
            PostcodeLookup postcodeReference = PostcodeHelper.getPostcodeReference(postcode);
            if (postcodeReference != null) {
                lsoaCode = postcodeReference.getLsoaCode();
                msoaCode = postcodeReference.getMsoaCode();
                //townsendScore = postcodeReference.getTownsendScore();
            }
        }

        //check if our patient demographics also should be used as the person demographics. This is typically
        //true if our patient record is at a GP practice.
        boolean shouldWritePersonRecord = shouldWritePersonRecord(fhirPatient, discoveryPersonId, params.getProtocolId());

        org.endeavourhealth.transform.enterprise.outputModels.Patient patientWriter = (org.endeavourhealth.transform.enterprise.outputModels.Patient)csvWriter;
        org.endeavourhealth.transform.enterprise.outputModels.Person personWriter = params.getData().getPersons();

        if (patientWriter.isPseduonymised()) {

            //if pseudonymised, all non-male/non-female genders should be treated as female
            if (fhirPatient.getGender() != Enumerations.AdministrativeGender.FEMALE
                    && fhirPatient.getGender() != Enumerations.AdministrativeGender.MALE) {
                patientGenderId = Enumerations.AdministrativeGender.FEMALE.ordinal();
            }

            pseudoId = pseudonymise(fhirPatient, params);

            //only persist the pseudo ID if it's non-null
            if (!Strings.isNullOrEmpty(pseudoId)) {
                PseudoIdHelper.storePseudoId(fhirPatient.getId(), params.getEnterpriseConfigName(), pseudoId);
            }

            Integer[] ageValues = EnterpriseAgeUpdater.calculateAgeValues(id, dateOfBirth, params.getEnterpriseConfigName());
            ageYears = ageValues[EnterpriseAgeUpdater.UNIT_YEARS];
            ageMonths = ageValues[EnterpriseAgeUpdater.UNIT_MONTHS];
            ageWeeks = ageValues[EnterpriseAgeUpdater.UNIT_WEEKS];

            patientWriter.writeUpsertPseudonymised(id,
                    organizationId,
                    personId,
                    patientGenderId,
                    pseudoId,
                    ageYears,
                    ageMonths,
                    ageWeeks,
                    dateOfDeath,
                    postcodePrefix,
                    householdId,
                    lsoaCode,
                    msoaCode);

            //if our patient record is the one that should define the person record, then write that too
            if (shouldWritePersonRecord) {
                personWriter.writeUpsertPseudonymised(personId,
                        patientGenderId,
                        pseudoId,
                        ageYears,
                        ageMonths,
                        ageWeeks,
                        dateOfDeath,
                        postcodePrefix,
                        householdId,
                        lsoaCode,
                        msoaCode);
            }

        } else {

            nhsNumber = IdentifierHelper.findNhsNumber(fhirPatient);

            patientWriter.writeUpsertIdentifiable(id,
                    organizationId,
                    personId,
                    patientGenderId,
                    nhsNumber,
                    dateOfBirth,
                    dateOfDeath,
                    postcode,
                    householdId,
                    lsoaCode,
                    msoaCode);

            //if our patient record is the one that should define the person record, then write that too
            if (shouldWritePersonRecord) {
                personWriter.writeUpsertIdentifiable(personId,
                        patientGenderId,
                        nhsNumber,
                        dateOfBirth,
                        dateOfDeath,
                        postcode,
                        householdId,
                        lsoaCode,
                        msoaCode);
            }
        }
    }

    private boolean shouldWritePersonRecord(Patient fhirPatient, String discoveryPersonId, UUID protocolId) throws Exception {

        //check if OUR patient record is an active one at a GP practice, in which case it definitely should define the person record
        String patientId = fhirPatient.getId();
        PatientSearch patientSearch = PatientSearchHelper.searchByPatientId(UUID.fromString(patientId));

        if (patientSearch != null //if we get null back, then we'll have deleted the patient, so just skip the ID
                && isActive(patientSearch)
                && getPatientSearchOrgScore(patientSearch) == BEST_ORG_SCORE) {
            return true;
        }

        //if we fail the above check, we need to know what other services are in our protocol, so we can see
        //which service has the most relevant data for the person record
        LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItemUsingCache(protocolId);
        Protocol protocol = libraryItem.getProtocol();
        Set<String> serviceIdsInProtocol = new HashSet<>();

        for (ServiceContract serviceContract: protocol.getServiceContract()) {
            if (serviceContract.getType().equals(ServiceContractType.SUBSCRIBER)
                && serviceContract.getActive() == ServiceContractActive.TRUE) {

                serviceIdsInProtocol.add(serviceContract.getService().getUuid());
            }
        }

        //get the other patient IDs for our person record
        List<PatientSearch> patientSearchesInProtocol = new ArrayList<>();
        patientSearchesInProtocol.add(patientSearch);

        List<String> allPatientIds = PatientLinkHelper.getPatientIds(discoveryPersonId);
        for (String otherPatientId: allPatientIds) {

            //skip the patient ID we've already retrieved
            if (otherPatientId.equals(patientId)) {
                continue;
            }

            PatientSearch otherPatientSearch = PatientSearchHelper.searchByPatientId(UUID.fromString(otherPatientId));

            //if we get null back, then we'll have deleted the patient, so just skip the ID
            if (otherPatientSearch == null) {
                LOG.error("Failed to get patient search record for patient ID " + otherPatientId);
                continue;
            }

            //if this patient search record isn't in our protocol, skip it
            String otherPatientSearchService = otherPatientSearch.getServiceId();
            if (!serviceIdsInProtocol.contains(otherPatientSearchService)) {
                continue;
            }

            patientSearchesInProtocol.add(otherPatientSearch);
        }

        //sort the patient searches so active GP ones are first
        patientSearchesInProtocol.sort((o1, o2) -> {

            //active records always supersede inactive ones
            boolean o1Active = isActive(o1);
            boolean o2Active = isActive(o2);
            if (o1Active && !o2Active) {
                return -1;
            } else if (!o1Active && o2Active) {
                return 1;
            } else {
                //if they both have the same active state, then check the org type. We want
                //GP practices first, then hospital records, then everything else
                int o1OrgScore = getPatientSearchOrgScore(o1);
                int o2OrgScore = getPatientSearchOrgScore(o2);
                if (o1OrgScore > o2OrgScore) {
                    return -1;
                } else if (o1OrgScore < o2OrgScore) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        //check if the best patient search record matches our patient ID. If it does, then our patient
        //should be the one that is used to define the person table.
        PatientSearch bestPatientSearch = patientSearchesInProtocol.get(0);

        //if there are no patient search records (i.e. patient is deleted everywhere), just return false since there doesn't need to be a person record
        if (bestPatientSearch == null) {
            return false;
        }

        return bestPatientSearch.getPatientId().equals(patientId);
    }

    private static int getPatientSearchOrgScore(PatientSearch patientSearch) {
        String typeCode = patientSearch.getOrganisationTypeCode();
        if (Strings.isNullOrEmpty(typeCode)) {
            return BEST_ORG_SCORE-4;
        }

        if (typeCode.equals(OrganisationType.GP_PRACTICE.getCode())) {
            return BEST_ORG_SCORE;
        } else if (typeCode.equals(OrganisationType.NHS_TRUST.getCode())
                || typeCode.equals(OrganisationType.NHS_TRUST_SERVICE.getCode())) {
            return BEST_ORG_SCORE-1;
        } else {
            return BEST_ORG_SCORE-2;
        }
    }

    private static boolean isActive(PatientSearch patientSearch) {
        Date deducted = patientSearch.getRegistrationEnd();
        return deducted == null || deducted.after(new Date());
    }

    private static final String findPostcodePrefix(String postcode) {

        if (Strings.isNullOrEmpty(postcode)) {
            return null;
        }

        //if the postcode is already formatted with a space, use that
        int spaceIndex = postcode.indexOf(" ");
        if (spaceIndex > -1) {
            return postcode.substring(0, spaceIndex);
        }

        //if no space, then drop the last three chars off, which works
        //for older format postcodes (e.g. AN, ANN, AAN, AANN) and the newer London ones (e.g. ANA, AANA)
        int len = postcode.length();
        if (len <= 3) {
            return null;
        }

        return postcode.substring(0, len-3);
    }

    private static String pseudonymise(Patient fhirPatient, EnterpriseTransformParams params) throws Exception {

        String dob = null;
        if (fhirPatient.hasBirthDate()) {
            Date d = fhirPatient.getBirthDate();
            dob = new SimpleDateFormat("dd-MM-yyyy").format(d);
        }

        if (Strings.isNullOrEmpty(dob)) {
            //we always need DoB for the psuedo ID
            return null;
        }

        TreeMap<String, String> keys = new TreeMap<>();
        keys.put(PSEUDO_KEY_DATE_OF_BIRTH, dob);

        String nhsNumber = IdentifierHelper.findNhsNumber(fhirPatient);
        if (!Strings.isNullOrEmpty(nhsNumber)) {
            keys.put(PSEUDO_KEY_NHS_NUMBER, nhsNumber);

        } else {

            //if we don't have an NHS number, use the Emis patient number
            String patientNumber = null;
            if (fhirPatient.hasIdentifier()) {
                patientNumber = IdentifierHelper.findIdentifierValue(fhirPatient.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_EMIS_PATIENT_NUMBER);
            }

            if (!Strings.isNullOrEmpty(patientNumber)) {
                keys.put(PSEUDO_KEY_PATIENT_NUMBER, patientNumber);

            } else {
                //if no NHS number or patient number
                return null;
            }
        }

        Crypto crypto = new Crypto();
        crypto.SetEncryptedSalt(getEncryptedSalt(params.getEnterpriseConfigName()));
        return crypto.GetDigest(keys);
    }


    private static byte[] getEncryptedSalt(String configName) throws Exception {

        byte[] ret = saltCacheMap.get(configName);
        if (ret == null) {

            synchronized (saltCacheMap) {
                ret = saltCacheMap.get(configName);
                if (ret == null) {

                    JsonNode config = ConfigManager.getConfigurationAsJson(configName, "enterprise");
                    JsonNode saltNode = config.get("salt");
                    if (saltNode == null) {
                        throw new Exception("No 'Salt' element found in Enterprise config " + configName);
                    }
                    String base64Salt = saltNode.asText();
                    ret = Base64.getDecoder().decode(base64Salt);
                    saltCacheMap.put(configName, ret);
                }
            }
        }
        return ret;
    }
    /*private static byte[] getEncryptedSalt() throws Exception {
        if (saltBytes == null) {
            saltBytes = Resources.getResourceAsBytes(PSEUDO_SALT_RESOURCE);
        }
        return saltBytes;
    }*/

}

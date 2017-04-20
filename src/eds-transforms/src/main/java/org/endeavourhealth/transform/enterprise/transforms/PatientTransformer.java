package org.endeavourhealth.transform.enterprise.transforms;

import OpenPseudonymiser.Crypto;
import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.*;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.common.utility.Resources;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;
import org.endeavourhealth.core.rdbms.eds.PatientLinkHelper;
import org.endeavourhealth.core.rdbms.eds.PatientLinkPair;
import org.endeavourhealth.core.rdbms.reference.PostcodeHelper;
import org.endeavourhealth.core.rdbms.reference.PostcodeLookup;
import org.endeavourhealth.core.rdbms.transform.EnterpriseAgeUpdater;
import org.endeavourhealth.core.rdbms.transform.EnterpriseIdHelper;
import org.endeavourhealth.core.rdbms.transform.HouseholdHelper;
import org.endeavourhealth.core.rdbms.transform.PseudoIdHelper;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class PatientTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(PatientTransformer.class);

    private static final String PSEUDO_KEY_NHS_NUMBER = "NHSNumber";
    private static final String PSEUDO_KEY_PATIENT_NUMBER = "PatientNumber";
    private static final String PSEUDO_KEY_DATE_OF_BIRTH = "DOB";
    private static final String PSEUDO_SALT_RESOURCE = "Endeavour Enterprise - East London.EncryptedSalt";

    private static byte[] saltBytes = null;
    private static ResourceRepository resourceRepository = new ResourceRepository();

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          AbstractEnterpriseCsvWriter csvWriter,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long nullEnterprisePatientId,
                          Long nullEnterprisePersonId,
                          String configName) throws Exception {

        Long enterpriseId = mapId(resource, csvWriter, true);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            csvWriter.writeDelete(enterpriseId.longValue());

        } else {
            Resource fhir = deserialiseResouce(resource);
            transform(enterpriseId, fhir, data, csvWriter, otherResources, enterpriseOrganisationId, nullEnterprisePatientId, nullEnterprisePersonId, configName);
        }
    }

    public void transform(Long enterpriseId,
                          Resource resource,
                          OutputContainer data,
                          AbstractEnterpriseCsvWriter csvWriter,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long nullEnterprisePatientId,
                          Long nullEnterprisePersonId,
                          String configName) throws Exception {

        Patient fhirPatient = (Patient)resource;

        String discoveryPersonId = PatientLinkHelper.getPersonId(fhirPatient.getId());

        //when the person ID table was populated, patients who had been deleted weren't added,
        //so we'll occasionally get null for some patients. If this happens, just do what would have
        //been done originally and assign an ID
        if (Strings.isNullOrEmpty(discoveryPersonId)) {
            PatientLinkPair pair = PatientLinkHelper.updatePersonId(fhirPatient);
            discoveryPersonId = pair.getNewPersonId();
        }

        Long enterprisePersonId = EnterpriseIdHelper.findOrCreateEnterprisePersonId(discoveryPersonId, configName);

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
        organizationId = enterpriseOrganisationId.longValue();
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
                    householdId = HouseholdHelper.findOrCreateHouseholdId(address);
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
        boolean shouldWritePersonRecord = shouldWritePersonRecord(fhirPatient);

        org.endeavourhealth.transform.enterprise.outputModels.Patient patientWriter = (org.endeavourhealth.transform.enterprise.outputModels.Patient)csvWriter;
        org.endeavourhealth.transform.enterprise.outputModels.Person personWriter = data.getPersons();

        if (patientWriter.isPseduonymised()) {

            //if pseudonymised, all non-male/non-female genders should be treated as female
            if (fhirPatient.getGender() != Enumerations.AdministrativeGender.FEMALE
                    && fhirPatient.getGender() != Enumerations.AdministrativeGender.MALE) {
                patientGenderId = Enumerations.AdministrativeGender.FEMALE.ordinal();
            }

            pseudoId = pseudonomise(fhirPatient);

            //only persist the pseudo ID if it's non-null
            if (!Strings.isNullOrEmpty(pseudoId)) {
                PseudoIdHelper.storePseudoId(fhirPatient.getId(), configName, pseudoId);
            }

            Integer[] ageValues = EnterpriseAgeUpdater.calculateAgeValues(id, dateOfBirth, configName);
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

    private boolean shouldWritePersonRecord(Patient fhirPatient) throws Exception {

        //TODO - remove this
        if (true) {
            return false;
        }

        //if our FHIR record is for an active patient at a GP practice, then it should be the defining demographics for the Person record
        UUID patientUuid = UUID.fromString(fhirPatient.getId());
        ResourceHistory patientResourceHistory = resourceRepository.getCurrentVersion(ResourceType.Patient.toString(), patientUuid);
        UUID serviceUuid = patientResourceHistory.getServiceId();
        UUID systemUuid = patientResourceHistory.getSystemId();

        boolean activePatient = false;


        List<ResourceByPatient> episodeResourceWrappers = resourceRepository.getResourcesByPatient(serviceUuid, systemUuid, patientUuid, ResourceType.EpisodeOfCare.toString());
        for (ResourceByPatient resourceByPatient: episodeResourceWrappers) {
            EpisodeOfCare episodeOfCare = (EpisodeOfCare)deserialiseResouce(resourceByPatient.getResourceData());

            //if the FHIR episode doesn't have an organisation, there's no point checking it
            if (!episodeOfCare.hasManagingOrganization()) {
                continue;
            }

            boolean active = false;

            //episode is active if we have no end date or a future-dated end date
            if (episodeOfCare.hasPeriod()) {
                Period period = episodeOfCare.getPeriod();
                if (!period.hasEnd()
                    || (period.hasEnd() && period.getEnd().after(new Date()))) {
                    active = true;
                }
            }

            //episode is active if we're explicitly told it is
            if (episodeOfCare.hasStatus()
                    && episodeOfCare.getStatus() == EpisodeOfCare.EpisodeOfCareStatus.ACTIVE) {
                active = true;
            }

            //if the episode is active, we check the managing organisation to see if it's a GP practice
            if (active) {
                Reference orgReference = episodeOfCare.getManagingOrganization();
                ReferenceComponents comps = ReferenceHelper.getReferenceComponents(orgReference);
                Organization fhirOrganization = (Organization)resourceRepository.getCurrentVersionAsResource(comps.getResourceType(), comps.getId());
                if (fhirOrganization != null
                        && fhirOrganization.hasType()) {

                    CodeableConcept codeableConcept = fhirOrganization.getType();
                    String orgTypeCode = CodeableConceptHelper.findCodingCode(codeableConcept, FhirValueSetUri.VALUE_SET_ORGANISATION_TYPE);
                    if (!Strings.isNullOrEmpty(orgTypeCode)
                            && orgTypeCode.equals(OrganisationType.GP_PRACTICE.getCode())) {
                        return true;
                    }
                }
            }
        }

        //TODO - check just if is active and NOT gp practice elsewhere
        String personId = PatientLinkHelper.getPersonId(patientUuid.toString());
        List<String> otherPatientIds = PatientLinkHelper.getPatientIds(personId);

        //TODO - how to work out what other services are in the protocol????


        return false;
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

    private static String pseudonomise(Patient fhirPatient) throws Exception {

        String dob = null;
        if (fhirPatient.hasBirthDate()) {
            Date d = fhirPatient.getBirthDate();
            dob = new SimpleDateFormat("dd-MM-yyyy").format(d);
        }

        if (Strings.isNullOrEmpty(dob)) {
            //we always need DoB for the psuedo ID
            return null;
        }

        TreeMap keys = new TreeMap();
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
        crypto.SetEncryptedSalt(getEncryptedSalt());
        return crypto.GetDigest(keys);
    }

    private static byte[] getEncryptedSalt() throws Exception {
        if (saltBytes == null) {
            saltBytes = Resources.getResourceAsBytes(PSEUDO_SALT_RESOURCE);
        }
        return saltBytes;
    }

}

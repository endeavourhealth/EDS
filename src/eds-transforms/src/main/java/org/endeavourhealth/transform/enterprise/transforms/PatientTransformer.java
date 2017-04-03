package org.endeavourhealth.transform.enterprise.transforms;

import OpenPseudonymiser.Crypto;
import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.endeavourhealth.common.utility.Resources;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.rdbms.eds.PatientLinkHelper;
import org.endeavourhealth.core.rdbms.eds.PatientLinkPair;
import org.endeavourhealth.core.rdbms.reference.PostcodeHelper;
import org.endeavourhealth.core.rdbms.reference.PostcodeReference;
import org.endeavourhealth.core.rdbms.transform.EnterpriseAgeUpdater;
import org.endeavourhealth.core.rdbms.transform.EnterpriseIdHelper;
import org.endeavourhealth.core.rdbms.transform.HouseholdHelper;
import org.endeavourhealth.core.rdbms.transform.PseudoIdHelper;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.DateType;
import org.hl7.fhir.instance.model.Enumerations;
import org.hl7.fhir.instance.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class PatientTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(PatientTransformer.class);

    private static final String PSEUDO_KEY_NHS_NUMBER = "NHSNumber";
    private static final String PSEUDO_KEY_PATIENT_NUMBER = "PatientNumber";
    private static final String PSEUDO_KEY_DATE_OF_BIRTH = "DOB";
    private static final String PSEUDO_SALT_RESOURCE = "Endeavour Enterprise - East London.EncryptedSalt";

    private static byte[] saltBytes = null;

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long nullEnterprisePatientId,
                          Long nullEnterprisePersonId,
                          String configName) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.Patient model = data.getPatients();

        Long enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.longValue());

        } else {
            Patient fhirPatient = (Patient)deserialiseResouce(resource);

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
            BigDecimal townsendScore = null;

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
                    if (address.getUse().equals(Address.AddressUse.HOME)) {
                        postcode = address.getPostalCode();
                        postcodePrefix = findPostcodePrefix(postcode);
                        householdId = HouseholdHelper.findOrCreateHouseholdId(address);
                    }
                }
            }

            //if we've found a postcode, then get the LSOA etc. for it
            if (!Strings.isNullOrEmpty(postcode)) {
                PostcodeReference postcodeReference = PostcodeHelper.getPostcodeReference(postcode);
                if (postcodeReference != null) {
                    lsoaCode = postcodeReference.getLsoaCode();
                    msoaCode = postcodeReference.getMsoaCode();
                    townsendScore = postcodeReference.getTownsendScore();
                }
            }


            if (model.isPseduonymised()) {

                //if pseudonymised, all non-male/non-female genders should be treated as female
                if (fhirPatient.getGender() != Enumerations.AdministrativeGender.FEMALE
                        && fhirPatient.getGender() != Enumerations.AdministrativeGender.MALE) {
                    patientGenderId = Enumerations.AdministrativeGender.FEMALE.ordinal();
                }

                pseudoId = pseudonomise(fhirPatient);

                //only persist the pseudo ID if it's non-null
                if (!Strings.isNullOrEmpty(pseudoId)) {
                    PseudoIdHelper.storePseudoId(fhirPatient.getId(), pseudoId);
                }

                Integer[] ageValues = EnterpriseAgeUpdater.calculateAgeValues(id, dateOfBirth, configName);
                ageYears = ageValues[EnterpriseAgeUpdater.UNIT_YEARS];
                ageMonths = ageValues[EnterpriseAgeUpdater.UNIT_MONTHS];
                ageWeeks = ageValues[EnterpriseAgeUpdater.UNIT_WEEKS];

                model.writeUpsertPseudonymised(id,
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
                        msoaCode,
                        townsendScore);

            } else {

                nhsNumber = IdentifierHelper.findNhsNumber(fhirPatient);

                model.writeUpsertIdentifiable(id,
                        organizationId,
                        personId,
                        patientGenderId,
                        nhsNumber,
                        dateOfBirth,
                        dateOfDeath,
                        postcode,
                        householdId,
                        lsoaCode,
                        msoaCode,
                        townsendScore);

            }
        }
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

    /**
     * We've had a bug that resulted in deleted patient resources where the dependent resources we're also deleted
     * This is now fixed in the inbound Emis transform, but the existing data is in a state that the need to handle below
     * Update 21/02/2017 - data in AIMES has been fixed, so this isn't required any more
     */
    /*private void deleteAllDependentEntities(OutputContainer data, ResourceByExchangeBatch resourceBatchEntry) throws Exception {

        //retrieve all past versions, to find the EDS patient ID
        ResourceRepository resourceRepository = new ResourceRepository();
        UUID patientResourceId = resourceBatchEntry.getResourceId();
        String patientResourceType = resourceBatchEntry.getResourceType();
        //LOG.trace("Deleting patient " + patientResourceId);

        ResourceHistory resourceHistory = resourceRepository.getCurrentVersion(patientResourceType, patientResourceId);
        UUID serviceId = resourceHistory.getServiceId();
        UUID systemId = resourceHistory.getSystemId();

        //retrieve all non-deleted resources
        List<ResourceByPatient> resourceByPatients = resourceRepository.getResourcesByPatient(serviceId, systemId, patientResourceId);
        //LOG.trace("Found " + resourceByPatients.size() + " resources for service " + serviceId + " system " + systemId + " and patient " + patientResourceId);
        for (ResourceByPatient resourceByPatient: resourceByPatients) {

            String resourceTypeStr = resourceByPatient.getResourceType();
            UUID resourceId = resourceByPatient.getResourceId();
            ResourceType resourceType = ResourceType.valueOf(resourceTypeStr);

            AbstractEnterpriseCsvWriter csvWriter = null;
            if (resourceType == ResourceType.Organization) {
                csvWriter = data.getOrganisations();
            } else if (resourceType == ResourceType.Practitioner) {
                csvWriter = data.getPractitioners();
            } else if (resourceType == ResourceType.Schedule) {
                csvWriter = data.getSchedules();
            } else if (resourceType == ResourceType.Patient) {
                csvWriter = data.getPatients();
            } else if (resourceType == ResourceType.EpisodeOfCare) {
                csvWriter = data.getEpisodesOfCare();
            } else if (resourceType == ResourceType.Appointment) {
                csvWriter = data.getAppointments();
            } else if (resourceType == ResourceType.Encounter) {
                csvWriter = data.getEncounters();
            } else if (resourceType == ResourceType.ReferralRequest) {
                csvWriter = data.getReferralRequests();
            } else if (resourceType == ResourceType.ProcedureRequest) {
                csvWriter = data.getProcedureRequests();
            } else if (resourceType == ResourceType.Observation) {
                csvWriter = data.getObservations();
            } else if (resourceType == ResourceType.MedicationStatement) {
                csvWriter = data.getMedicationStatements();
            } else if (resourceType == ResourceType.MedicationOrder) {
                csvWriter = data.getMedicationOrders();
            } else if (resourceType == ResourceType.AllergyIntolerance) {
                csvWriter = data.getAllergyIntolerances();
            } else if (resourceType == ResourceType.Condition) {
                csvWriter = data.getObservations();
            } else if (resourceType == ResourceType.Procedure) {
                csvWriter = data.getObservations();
            } else if (resourceType == ResourceType.Immunization) {
                csvWriter = data.getObservations();
            } else if (resourceType == ResourceType.FamilyMemberHistory) {
                csvWriter = data.getObservations();
            } else if (resourceType == ResourceType.DiagnosticOrder) {
                csvWriter = data.getObservations();
            } else if (resourceType == ResourceType.DiagnosticReport) {
                csvWriter = data.getObservations();
            } else if (resourceType == ResourceType.Specimen) {
                csvWriter = data.getObservations();
            } else if (resourceType == ResourceType.Slot) {
                //these aren't sent to Enterprise
                continue;
            } else if (resourceType == ResourceType.Location) {
                //these aren't sent to Enterprise
                continue;
            } else {
                throw new Exception("Unhandlded resource type " + resourceType);
            }

            Integer enterpriseId = findEnterpriseId(csvWriter, resourceTypeStr, resourceId);
            //LOG.trace("Writing delete for " + csvWriter.getClass().getSimpleName() + " " + enterpriseId);
            if (enterpriseId != null) {
                csvWriter.writeDelete(enterpriseId.intValue());
            }
        }
    }*/

    /*public void transform(ResourceByExchangeBatch resource,
                          EnterpriseData data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Patient model = new org.endeavourhealth.core.xml.enterprise.Patient();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            Patient fhirPatient = (Patient)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            //Calendar cal = Calendar.getInstance();

            Date dob = fhirPatient.getBirthDate();
            model.setDateOfBirth(convertDate(dob));
            *//*cal.setTime(dob);
            int yearOfBirth = cal.get(Calendar.YEAR);
            model.setYearOfBirth(yearOfBirth);*//*

            if (fhirPatient.hasDeceasedDateTimeType()) {
                Date dod = fhirPatient.getDeceasedDateTimeType().getValue();
                model.setDateOfDeath(convertDate(dod));
                *//*cal.setTime(dod);
                int yearOfDeath = cal.get(Calendar.YEAR);
                model.setYearOfDeath(new Integer(yearOfDeath));*//*
            }

            model.setPatientGenderId(fhirPatient.getGender().ordinal());

            if (fhirPatient.hasAddress()) {
                for (Address address: fhirPatient.getAddress()) {
                    if (address.getUse().equals(Address.AddressUse.HOME)) {
                        String postcode = address.getPostalCode();
                        model.setPostcode(postcode);
                    }
                }
            }

            model.setPseudoId(pseudonomise(fhirPatient));

            //adding NHS number to allow data checking
            String nhsNumber = findNhsNumber(fhirPatient);
            model.setNhsNumber(nhsNumber);
        }

        data.getPatient().add(model);
    }*/

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

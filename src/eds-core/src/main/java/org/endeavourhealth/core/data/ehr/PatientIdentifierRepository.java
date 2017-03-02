package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PatientIdentifierAccessor;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByLocalId;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByNhsNumber;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByPatientId;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class PatientIdentifierRepository extends Repository {

    private final static String IDENTIFIER_SYSTEM_NHSNUMBER = "http://fhir.nhs.net/Id/nhs-number";

    public void savePatientIdentity(Patient fhirPatient,
                                    UUID serviceId,
                                    UUID systemId) {

        //for each identifier, other than NHS number, save the patient identity. NHS number is handled
        //slightly differently, with its own materialised view
        for (Identifier fhirIdentifier: fhirPatient.getIdentifier()) {

            if (!fhirIdentifier.getSystem().equalsIgnoreCase(IDENTIFIER_SYSTEM_NHSNUMBER)) {
                savePatientIdentity(fhirPatient, serviceId, systemId, fhirIdentifier.getSystem(), fhirIdentifier.getValue());
            }
        }
    }
    private void savePatientIdentity(Patient fhirPatient,
                                     UUID serviceId,
                                     UUID systemId,
                                     String localIdSystem,
                                     String localId) {

        UUID patientId = UUID.fromString(fhirPatient.getId());
        String nhsNumber = findNhsNumber(fhirPatient);
        String forenames = findForenames(fhirPatient);
        String surname = findSurname(fhirPatient);
        String postcode = findPostcode(fhirPatient);
        Enumerations.AdministrativeGender gender = fhirPatient.getGender();

        PatientIdentifierByLocalId newPatientIdentifier = new PatientIdentifierByLocalId();
        newPatientIdentifier.setServiceId(serviceId);
        newPatientIdentifier.setSystemId(systemId);
        newPatientIdentifier.setLocalId(localId);
        newPatientIdentifier.setLocalIdSystem(localIdSystem);
        newPatientIdentifier.setPatientId(patientId); //always re-set the patient ID, just in case it's changed
        newPatientIdentifier.setForenames(forenames);
        newPatientIdentifier.setSurname(surname);
        newPatientIdentifier.setNhsNumber(nhsNumber);
        newPatientIdentifier.setDateOfBirth(fhirPatient.getBirthDate());
        newPatientIdentifier.setPostcode(postcode);
        newPatientIdentifier.setGender(gender);
        newPatientIdentifier.setTimestamp(new Date());
        newPatientIdentifier.setVersion(UUIDs.timeBased());

        //compare against the original, in case we're saving the patient for a different demographic change we don't track here
        PatientIdentifierByLocalId latestPatientIdentifier = getMostRecentByLocalId(serviceId, systemId, localId, localIdSystem);
        if (latestPatientIdentifier != null) {
            boolean allSame = true;

            if (!nullEquals(newPatientIdentifier.getPatientId(), latestPatientIdentifier.getPatientId())) {
                allSame = false;
            }

            if (allSame
                    && !nullEquals(newPatientIdentifier.getForenames(), latestPatientIdentifier.getForenames())) {
                allSame = false;
            }

            if (allSame
                    && !nullEquals(newPatientIdentifier.getSurname(), latestPatientIdentifier.getSurname())) {
                allSame = false;
            }

            if (allSame
                    && !nullEquals(newPatientIdentifier.getNhsNumber(), latestPatientIdentifier.getNhsNumber())) {
                allSame = false;
            }

            if (allSame
                    && !nullEquals(newPatientIdentifier.getDateOfBirth(), latestPatientIdentifier.getDateOfBirth())) {
                allSame = false;
            }

            if (allSame
                    && !nullEquals(newPatientIdentifier.getPostcode(), latestPatientIdentifier.getPostcode())) {
                allSame = false;
            }

            if (allSame
                    && !nullEquals(newPatientIdentifier.getGender(), latestPatientIdentifier.getGender())) {
                allSame = false;
            }

            if (allSame) {
                return;
            }
        }

        save(newPatientIdentifier);
    }

    private static boolean nullEquals(Object one, Object two) {
        if (one == null && two == null) {
            return true;

        } else if (one == null || two == null) {
            return false;

        } else {
            return one.equals(two);
        }
    }



    /*private void savePatientIdentity(Patient fhirPatient,
                                    UUID serviceId,
                                    UUID systemId,
                                    String localIdSystem,
                                    String localId) {

        UUID patientId = UUID.fromString(fhirPatient.getId());

        PatientIdentifierByLocalId patientIdentifier = getMostRecentByLocalId(serviceId, systemId, localId, localIdSystem);

        //if we've never encountered this patient before, create a new personIdentifier record
        if (patientIdentifier == null) {
            patientIdentifier = new PatientIdentifierByLocalId();
            patientIdentifier.setServiceId(serviceId);
            patientIdentifier.setSystemId(systemId);
            patientIdentifier.setLocalId(localId);
            patientIdentifier.setLocalIdSystem(localIdSystem);
            patientIdentifier.setPatientId(patientId); //moved lower down
        }

        //whether we've encountered this patient before or not, refresh the record with the latest demographics
        String nhsNumber = findNhsNumber(fhirPatient);
        String forenames = findForenames(fhirPatient);
        String surname = findSurname(fhirPatient);
        String postcode = findPostcode(fhirPatient);
        Enumerations.AdministrativeGender gender = fhirPatient.getGender();

        patientIdentifier.setForenames(forenames);
        patientIdentifier.setSurname(surname);
        patientIdentifier.setNhsNumber(nhsNumber);
        patientIdentifier.setDateOfBirth(fhirPatient.getBirthDate());
        patientIdentifier.setPostcode(postcode);
        patientIdentifier.setGender(gender);
        patientIdentifier.setTimestamp(new Date());
        patientIdentifier.setVersion(UUIDs.timeBased());

        save(patientIdentifier);
    }*/


    private static String findForenames(Patient fhirPatient) {
        List<String> forenames = new ArrayList<>();

        for (HumanName fhirName: fhirPatient.getName()) {
            if (fhirName.getUse() != HumanName.NameUse.OFFICIAL) {
                continue;
            }

            for (StringType given: fhirName.getGiven()) {
                forenames.add(given.getValue());
            }
        }
        return String.join(" ", forenames);
    }

    private static String findSurname(Patient fhirPatient) {
        List<String> surnames = new ArrayList<>();

        for (HumanName fhirName: fhirPatient.getName()) {
            if (fhirName.getUse() != HumanName.NameUse.OFFICIAL) {
                continue;
            }

            for (StringType family: fhirName.getFamily()) {
                surnames.add(family.getValue());
            }
        }
        return String.join(" ", surnames);
    }

    private static String findPostcode(Patient fhirPatient) {

        for (Address fhirAddress: fhirPatient.getAddress()) {
            if (fhirAddress.getUse() != Address.AddressUse.HOME) {
                continue;
            }
            return fhirAddress.getPostalCode();
        }
        return null;
    }

    private static String findNhsNumber(Patient fhirPatient) {

        for (Identifier fhirIdentifier: fhirPatient.getIdentifier()) {
            if (fhirIdentifier.getSystem().equals(IDENTIFIER_SYSTEM_NHSNUMBER)) {
                return fhirIdentifier.getValue();
            }
        }
        return null;
    }

    public void save(PatientIdentifierByLocalId patientIdentifier) {
        if (patientIdentifier == null) {
            throw new IllegalArgumentException("personIdentifier is null");
        }

        Mapper<PatientIdentifierByLocalId> mapper = getMappingManager().mapper(PatientIdentifierByLocalId.class);
        mapper.save(patientIdentifier);
    }

    public PatientIdentifierByLocalId getMostRecentByLocalId(UUID serviceId, UUID systemId, String localId, String localIdSystem) {

        PatientIdentifierAccessor accessor = getMappingManager().createAccessor(PatientIdentifierAccessor.class);
        Iterator<PatientIdentifierByLocalId> iterator = accessor.getMostRecentForLocalId(serviceId, systemId, localId, localIdSystem).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<PatientIdentifierByLocalId> getForLocalId(UUID serviceId, UUID systemId, String localId) {

        PatientIdentifierAccessor accessor = getMappingManager().createAccessor(PatientIdentifierAccessor.class);
        return Lists.newArrayList(accessor.getForLocalId(serviceId, systemId, localId));
    }


    public List<PatientIdentifierByNhsNumber> getForNhsNumber(String nhsNumber) {

        PatientIdentifierAccessor accessor = getMappingManager().createAccessor(PatientIdentifierAccessor.class);
        return Lists.newArrayList(accessor.getForNhsNumber(nhsNumber));
    }

    public PatientIdentifierByPatientId getMostRecentByPatientId(UUID patientId) {

        PatientIdentifierAccessor accessor = getMappingManager().createAccessor(PatientIdentifierAccessor.class);
        Iterator<PatientIdentifierByPatientId> iterator = accessor.getMostRecentForPatientId(patientId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    // temporary call while we build a proper patient find index
    public List<PatientIdentifierByLocalId> getForNhsNumberTemporary(UUID serviceId, UUID systemId, String nhsNumber) {
        PatientIdentifierAccessor accessor = getMappingManager().createAccessor(PatientIdentifierAccessor.class);
        return Lists.newArrayList(accessor.getForNhsNumberTemporary(serviceId, systemId, nhsNumber));
    }

    // temporary call while we build a proper patient find index
    public List<PatientIdentifierByLocalId> getForSurnameTemporary(UUID serviceId, UUID systemId, String surname) {
        PatientIdentifierAccessor accessor = getMappingManager().createAccessor(PatientIdentifierAccessor.class);
        return Lists.newArrayList(accessor.getForSurnameTemporary(serviceId, systemId, surname));
    }

    // temporary call while we build a proper patient find index
    public List<PatientIdentifierByLocalId> getForForenamesTemporary(UUID serviceId, UUID systemId, String forenames) {
        PatientIdentifierAccessor accessor = getMappingManager().createAccessor(PatientIdentifierAccessor.class);
        return Lists.newArrayList(accessor.getForForenamesTemporary(serviceId, systemId, forenames));
    }

    /**
     * deletes all rows for a given service and system
     */
    public void hardDeleteForService(UUID serviceId, UUID systemId) {
        PatientIdentifierAccessor accessor = getMappingManager().createAccessor(PatientIdentifierAccessor.class);
        accessor.hardDeleteForService(serviceId, systemId);
    }
}

package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
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

        PatientIdentifierByLocalId patientIdentifier = getMostRecentByLocalId(serviceId, systemId, localId, localIdSystem);

        //if we've never encountered this patient before, create a new personIdentifier record
        if (patientIdentifier == null) {
            patientIdentifier = new PatientIdentifierByLocalId();
            patientIdentifier.setServiceId(serviceId);
            patientIdentifier.setSystemId(systemId);
            patientIdentifier.setLocalId(localId);
            patientIdentifier.setLocalIdSystem(localIdSystem);
            patientIdentifier.setPatientId(patientId);
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
    }

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
}

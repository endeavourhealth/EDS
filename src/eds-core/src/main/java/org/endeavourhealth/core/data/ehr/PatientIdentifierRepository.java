package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PatientIdentifierAccessor;
import org.endeavourhealth.core.data.ehr.accessors.PatientIdentifierByNhsNumberAccessor;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifier;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByNhsNumber;
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
    public void savePatientIdentity(Patient fhirPatient,
                                    UUID serviceId,
                                    UUID systemId,
                                    String localIdSystem,
                                    String localId) {

        UUID patientId = UUID.fromString(fhirPatient.getId());

        PatientIdentifier patientIdentifier = getMostRecent(serviceId, systemId, patientId, localIdSystem);

        //if we've never encountered this patient before, create a new personIdentifier record
        if (patientIdentifier == null) {
            patientIdentifier = new PatientIdentifier();
            patientIdentifier.setServiceId(serviceId);
            patientIdentifier.setSystemId(systemId);
            patientIdentifier.setPatientId(patientId);
            patientIdentifier.setLocalIdSystem(localIdSystem);
        }

        //whether we've encountered this patient before or not, refresh the record with the latest demographics
        String nhsNumber = findNhsNumber(fhirPatient);
        String forenames = findForenames(fhirPatient);
        String surname = findSurname(fhirPatient);
        String postcode = findPostcode(fhirPatient);
        Enumerations.AdministrativeGender gender = fhirPatient.getGender();

        patientIdentifier.setLocalId(localId);
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

    private String findForenames(Patient fhirPatient) {
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

    private String findSurname(Patient fhirPatient) {
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

    private String findPostcode(Patient fhirPatient) {

        for (Address fhirAddress: fhirPatient.getAddress()) {
            if (fhirAddress.getUse() != Address.AddressUse.HOME) {
                continue;
            }
            return fhirAddress.getPostalCode();
        }
        return null;
    }

    private String findNhsNumber(Patient fhirPatient) {

        for (Identifier fhirIdentifier: fhirPatient.getIdentifier()) {
            if (fhirIdentifier.getSystem().equals(IDENTIFIER_SYSTEM_NHSNUMBER)) {
                return fhirIdentifier.getValue();
            }
        }
        return null;
    }

    /*private String findLocalIdentifier(Patient fhirPatient) {

        for (Identifier fhirIdentifier: fhirPatient.getIdentifier()) {
            if (fhirIdentifier.getSystem() == null
                || !fhirIdentifier.getSystem().equals(IDENTIFIER_SYSTEM_NHSNUMBER)) {
                return fhirIdentifier.getValue();
            }
        }
        return null;
    }*/

    public void save(PatientIdentifier patientIdentifier) {
        if (patientIdentifier == null) {
            throw new IllegalArgumentException("personIdentifier is null");
        }

        Mapper<PatientIdentifier> mapper = getMappingManager().mapper(PatientIdentifier.class);
        mapper.save(patientIdentifier);
    }

    public PatientIdentifier getMostRecent(UUID serviceId, UUID systemId, UUID patientId, String localIdSystem) {

        PatientIdentifierAccessor accessor = getMappingManager().createAccessor(PatientIdentifierAccessor.class);
        Iterator<PatientIdentifier> iterator = accessor.getMostRecent(serviceId, systemId, patientId, localIdSystem).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }


    public List<PatientIdentifierByNhsNumber> getForNhsNumber(String nhsNumber) {

        PatientIdentifierByNhsNumberAccessor accessor = getMappingManager().createAccessor(PatientIdentifierByNhsNumberAccessor.class);
        return Lists.newArrayList(accessor.getForNhsNumber(nhsNumber));
    }

}

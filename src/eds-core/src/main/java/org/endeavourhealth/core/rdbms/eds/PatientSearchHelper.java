package org.endeavourhealth.core.rdbms.eds;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.*;
import org.endeavourhealth.core.data.ehr.ResourceNotFoundException;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.fhirStorage.metadata.ReferenceHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

public class PatientSearchHelper {
    private static final Logger LOG = LoggerFactory.getLogger(PatientSearchHelper.class);

    public static void update(UUID serviceId, UUID systemId, Patient fhirPatient) throws Exception {
        update(serviceId, systemId, fhirPatient, null);
    }

    public static void update(UUID serviceId, UUID systemId, EpisodeOfCare fhirEpisode) throws Exception {
        update(serviceId, systemId, null, fhirEpisode);
    }

    private static void update(UUID serviceId, UUID systemId, Patient fhirPatient, EpisodeOfCare fhirEpisode) throws Exception {

        EntityManager entityManager = EdsConnection.getEntityManager();

        try {
            performUpdateInTransaction(serviceId, systemId, fhirPatient, fhirEpisode, entityManager);

        } catch (Exception ex) {
            //if we get an exception during the above, it's probably because another thread has inserted for our
            //patient at the same time (since we file patient and episode resources in parallel), so we should rollback and just try again
            entityManager.getTransaction().rollback();

            try {
                performUpdateInTransaction(serviceId, systemId, fhirPatient, fhirEpisode, entityManager);

            } catch (Exception ex2) {
                //if we get an exception the second time around, we should rollback and throw the FIRST exception
                entityManager.getTransaction().rollback();
                throw ex;
            }

        } finally {
            entityManager.close();
        }
    }

    private static void performUpdateInTransaction(UUID serviceId, UUID systemId, Patient fhirPatient, EpisodeOfCare fhirEpisode, EntityManager entityManager) throws Exception {

        entityManager.getTransaction().begin();

        PatientSearch patientSearch = createOrUpdatePatientSearch(serviceId, systemId, fhirPatient, fhirEpisode, entityManager);
        entityManager.persist(patientSearch);

        //only if we have a patient resource do we need to update the local identifiers
        if (fhirPatient != null) {
            List<PatientSearchLocalIdentifier> localIdentifiers = createOrUpdateLocalIdentifiers(serviceId, systemId, fhirPatient, entityManager);
            for (PatientSearchLocalIdentifier localIdentifier: localIdentifiers) {

                //adding try/catch to investigate a problem that has happened once but can't be replicated
                //entityManager.persist(localIdentifier);
                try {
                    entityManager.persist(localIdentifier);

                } catch (Exception ex) {
                    String msg = ex.getMessage();
                    if (msg.indexOf("A different object with the same identifier value was already associated with the session") > -1) {

                        LOG.error("Failed to persist PatientSearchLocalIdentifier for service " + localIdentifier.getServiceId()
                                + " system " + localIdentifier.getSystemId()
                                + " patient " + localIdentifier.getPatientId()
                                + " ID system " + localIdentifier.getLocalIdSystem()
                                + " ID value " + localIdentifier.getLocalId()
                                + " date " + localIdentifier.getLastUpdated().getTime());

                        LOG.error("Entity being persisted is in entity cache = " + entityManager.contains(localIdentifier));
                    }
                    throw ex;
                }
            }
        }

        entityManager.getTransaction().commit();
    }


    private static List<PatientSearchLocalIdentifier> createOrUpdateLocalIdentifiers(UUID serviceId, UUID systemId, Patient fhirPatient, EntityManager entityManager) {
        String patientId = findPatientId(fhirPatient, null);

        String sql = "select c"
                + " from "
                + " PatientSearchLocalIdentifier c"
                + " where c.serviceId = :service_id"
                + " and c.systemId = :system_id"
                + " and c.patientId = :patient_id";

        Query query = entityManager.createQuery(sql, PatientSearchLocalIdentifier.class)
                .setParameter("service_id", serviceId.toString())
                .setParameter("system_id", systemId.toString())
                .setParameter("patient_id", patientId);

        List<PatientSearchLocalIdentifier> list = query.getResultList();

        Map<String, PatientSearchLocalIdentifier> existingMap = new HashMap<>();
        for (PatientSearchLocalIdentifier localIdentifier: list) {
            String system = localIdentifier.getLocalIdSystem();
            existingMap.put(system, localIdentifier);
        }

        if (fhirPatient.hasIdentifier()) {
            for (Identifier fhirIdentifier : fhirPatient.getIdentifier()) {

                if (!fhirIdentifier.getSystem().equalsIgnoreCase(FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER)) {
                    String system = fhirIdentifier.getSystem();
                    String value = fhirIdentifier.getValue();

                    PatientSearchLocalIdentifier localIdentifier = existingMap.get(system);
                    if (localIdentifier == null) {
                        localIdentifier = new PatientSearchLocalIdentifier();
                        localIdentifier.setServiceId(serviceId.toString());
                        localIdentifier.setSystemId(systemId.toString());
                        localIdentifier.setPatientId(patientId);
                        localIdentifier.setLocalIdSystem(system);

                        list.add(localIdentifier);
                    }

                    localIdentifier.setLocalId(value);
                    localIdentifier.setLastUpdated(new Date());
                }
            }
        }

        return list;
    }

    private static PatientSearch createOrUpdatePatientSearch(UUID serviceId, UUID systemId, Patient fhirPatient, EpisodeOfCare fhirEpisode, EntityManager entityManager) throws Exception {
        String patientId = findPatientId(fhirPatient, fhirEpisode);

        String sql = "select c"
                + " from"
                + " PatientSearch c"
                + " where c.serviceId = :serviceId"
                + " and c.systemId = :systemId"
                + " and c.patientId = :patientId";

        Query query = entityManager.createQuery(sql, PatientSearch.class)
                .setParameter("serviceId", serviceId.toString())
                .setParameter("systemId", systemId.toString())
                .setParameter("patientId", patientId);

        PatientSearch patientSearch = null;
        try {
            patientSearch = (PatientSearch)query.getSingleResult();

        } catch (NoResultException ex) {
            patientSearch = new PatientSearch();
            patientSearch.setServiceId(serviceId.toString());
            patientSearch.setSystemId(systemId.toString());
            patientSearch.setPatientId(patientId);
        }

        if (fhirPatient != null) {

            String nhsNumber = IdentifierHelper.findNhsNumberTrueNhsNumber(fhirPatient);
            String forenames = findForenames(fhirPatient);
            String surname = findSurname(fhirPatient);
            String postcode = findPostcode(fhirPatient);
            String gender = findGender(fhirPatient);
            Date dob = fhirPatient.getBirthDate();
            Date dod = findDateOfDeath(fhirPatient);

            patientSearch.setNhsNumber(nhsNumber);
            patientSearch.setForenames(forenames);
            patientSearch.setSurname(surname);
            patientSearch.setPostcode(postcode);
            patientSearch.setGender(gender);
            patientSearch.setDateOfBirth(dob);
            patientSearch.setDateOfDeath(dod);
        }

        if (fhirEpisode != null) {

            Date regStart = null;
            Date regEnd = null;
            String orgTypeCode = null;

            if (fhirEpisode.hasPeriod()) {
                Period period = fhirEpisode.getPeriod();
                if (period.hasStart()) {
                    regStart = period.getStart();
                }
                if (period.hasEnd()) {
                    regEnd = period.getEnd();
                }
            }

            if (fhirEpisode.hasManagingOrganization()) {
                Reference orgReference = fhirEpisode.getManagingOrganization();
                ReferenceComponents comps = org.endeavourhealth.common.fhir.ReferenceHelper.getReferenceComponents(orgReference);
                ResourceType type = comps.getResourceType();
                String id = comps.getId();
                try {
                    Organization org = (Organization)new ResourceRepository().getCurrentVersionAsResource(type, id);
                    if (org != null) {
                        CodeableConcept concept = org.getType();
                        orgTypeCode = CodeableConceptHelper.findCodingCode(concept, FhirValueSetUri.VALUE_SET_ORGANISATION_TYPE);
                    }
                } catch (ResourceNotFoundException ex) {
                    //if the resource doesn't exist, then just leave the field blank
                }
            }

            patientSearch.setRegistrationStart(regStart);
            patientSearch.setRegistrationEnd(regEnd);
            patientSearch.setOrganisationTypeCode(orgTypeCode);
        }

        patientSearch.setLastUpdated(new Date());

        return patientSearch;
    }

    private static String findGender(Patient fhirPatient) {
        if (fhirPatient.hasGender()) {
            return fhirPatient.getGender().getDisplay();
        } else {
            return null;
        }
    }

    private static Date findDateOfDeath(Patient fhirPatient) throws Exception {
        if (fhirPatient.hasDeceasedDateTimeType()) {
            return fhirPatient.getDeceasedDateTimeType().getValue();
        } else {
            return null;
        }
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

            //Homerton seem to sometimes enter extra information in the postcode
            //field, making it longer than the 8 chars the field allows. So
            //simply truncate down
            String s = fhirAddress.getPostalCode();
            if (!Strings.isNullOrEmpty(s)
                    && s.length() > 8) {
                s = s.substring(0, 8);
            }
            return s;
            //return fhirAddress.getPostalCode();
        }
        return null;
    }

    private static String findPatientId(Patient fhirPatient, EpisodeOfCare fhirEpisode) {
        if (fhirPatient != null) {
            return fhirPatient.getId();

        } else {
            Reference reference = fhirEpisode.getPatient();
            return ReferenceHelper.getReferenceId(reference);
        }
    }



    public static void delete(UUID serviceId, UUID systemId) throws Exception {

        EntityManager entityManager = EdsConnection.getEntityManager();
        entityManager.getTransaction().begin();

        String sql = "delete"
                + " from"
                + " PatientSearchLocalIdentifier c"
                + " where c.serviceId = :serviceId"
                + " and c.systemId = :systemId";

        Query query = entityManager.createQuery(sql)
                .setParameter("serviceId", serviceId.toString())
                .setParameter("systemId", systemId.toString());
        query.executeUpdate();

        sql = "delete"
                + " from"
                + " PatientSearch c"
                + " where c.serviceId = :serviceId"
                + " and c.systemId = :systemId";

        query = entityManager.createQuery(sql)
                .setParameter("serviceId", serviceId.toString())
                .setParameter("systemId", systemId.toString());
        query.executeUpdate();

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public static List<PatientSearch> searchByNhsNumber(String nhsNumber) throws Exception {
        EntityManager entityManager = EdsConnection.getEntityManager();

        String sql = "select c"
                + " from"
                + " PatientSearch c"
                + " where c.nhsNumber = :nhs_number";

        Query query = entityManager.createQuery(sql, PatientSearch.class)
                .setParameter("nhs_number", nhsNumber);

        List<PatientSearch> results = query.getResultList();
        entityManager.close();
        return results;
    }

    public static List<PatientSearch> searchByLocalId(UUID serviceId, UUID systemId, String localId) throws Exception {
        EntityManager entityManager = EdsConnection.getEntityManager();

        String sql = "select c"
            + " from"
            + " PatientSearch c"
            + " inner join PatientSearchLocalIdentifier l"
            + " on c.serviceId = l.serviceId"
            + " and c.systemId = l.systemId"
            + " and c.patientId = l.patientId"
            + " where l.localId = :localId"
            + " and l.serviceId = :serviceId"
            + " and l.systemId = :systemId";

        Query query = entityManager.createQuery(sql, PatientSearch.class)
            .setParameter("localId", localId)
            .setParameter("serviceId", serviceId.toString())
            .setParameter("systemId", systemId.toString());

        List<PatientSearch> results = query.getResultList();
        entityManager.close();
        return results;
    }

    public static List<PatientSearch> searchByLocalId(Set<String> serviceIds, String localId) throws Exception {
        EntityManager entityManager = EdsConnection.getEntityManager();

        String sql = "select c"
            + " from"
            + " PatientSearch c"
            + " inner join PatientSearchLocalIdentifier l"
            + " on c.serviceId = l.serviceId"
            + " and c.systemId = l.systemId"
            + " and c.patientId = l.patientId"
            + " where l.localId = :localId"
            + " and l.serviceId IN :serviceIds";

        Query query = entityManager.createQuery(sql, PatientSearch.class)
            .setParameter("localId", localId)
            .setParameter("serviceIds", serviceIds);

        List<PatientSearch> results = query.getResultList();
        entityManager.close();
        return results;
    }

    public static List<PatientSearch> searchByDateOfBirth(UUID serviceId, UUID systemId, Date dateOfBirth) throws Exception {
        EntityManager entityManager = EdsConnection.getEntityManager();

        String sql = "select c"
                + " from"
                + " PatientSearch c"
                + " where c.dateOfBirth = :dateOfBirth"
                + " and c.serviceId = :serviceId"
                + " and c.systemId = :systemId";

        Query query = entityManager.createQuery(sql, PatientSearch.class)
                .setParameter("dateOfBirth", dateOfBirth)
                .setParameter("serviceId", serviceId.toString())
                .setParameter("systemId", systemId.toString());

        List<PatientSearch> results = query.getResultList();
        entityManager.close();
        return results;
    }

    public static List<PatientSearch> searchByDateOfBirth(Set<String> serviceIds, Date dateOfBirth) throws Exception {
        EntityManager entityManager = EdsConnection.getEntityManager();

        String sql = "select c"
            + " from"
            + " PatientSearch c"
            + " where c.dateOfBirth = :dateOfBirth"
            + " and c.serviceId IN :serviceIds";

        Query query = entityManager.createQuery(sql, PatientSearch.class)
            .setParameter("dateOfBirth", dateOfBirth)
            .setParameter("serviceIds", serviceIds);

        List<PatientSearch> results = query.getResultList();
        entityManager.close();
        return results;
    }

    public static List<PatientSearch> searchByNhsNumber(UUID serviceId, UUID systemId, String nhsNumber) throws Exception {
        EntityManager entityManager = EdsConnection.getEntityManager();

        String sql = "select c"
                + " from"
                + " PatientSearch c"
                + " where c.nhsNumber = :nhs_number"
                + " and c.serviceId = :serviceId"
                + " and c.systemId = :systemId";

        Query query = entityManager.createQuery(sql, PatientSearch.class)
                .setParameter("nhs_number", nhsNumber)
                .setParameter("serviceId", serviceId.toString())
                .setParameter("systemId", systemId.toString());

        List<PatientSearch> results = query.getResultList();
        entityManager.close();
        return results;
    }

    public static List<PatientSearch> searchByNhsNumber(Set<String> serviceIds, String nhsNumber) throws Exception {
        EntityManager entityManager = EdsConnection.getEntityManager();

        String sql = "select c"
            + " from"
            + " PatientSearch c"
            + " where c.nhsNumber = :nhs_number"
            + " and c.serviceId in :serviceIds";

        Query query = entityManager.createQuery(sql, PatientSearch.class)
            .setParameter("nhs_number", nhsNumber)
            .setParameter("serviceIds", serviceIds);

        List<PatientSearch> results = query.getResultList();
        entityManager.close();
        return results;
    }

    public static List<PatientSearch> searchByNames(UUID serviceId, UUID systemId, List<String> names) throws Exception {

        if (names.isEmpty()) {
            throw new IllegalArgumentException("Names cannot be empty");
        }

        EntityManager entityManager = EdsConnection.getEntityManager();

        List<PatientSearch> results = null;

        //if just one name, then treat as a surname
        if (names.size() == 1) {

            String surname = names.get(0) + "%";

            String sql = "select c"
                    + " from"
                    + " PatientSearch c"
                    + " where lower(c.surname) LIKE lower(:surname)"
                    + " and c.serviceId = :serviceId"
                    + " and c.systemId = :systemId";

            Query query = entityManager.createQuery(sql, PatientSearch.class)
                    .setParameter("surname", surname)
                    .setParameter("serviceId", serviceId.toString())
                    .setParameter("systemId", systemId.toString());

            results = query.getResultList();

        } else {

            //if multiple tokens, then treat all but the last as forenames
            names = new ArrayList(names);
            String surname = names.remove(names.size()-1) + "%";
            String forenames = String.join("% ", names) + "%";

            String sql = "select c"
                    + " from"
                    + " PatientSearch c"
                    + " where lower(c.surname) LIKE lower(:surname)"
                    + " and lower(c.forenames) LIKE lower(:forenames)"
                    + " and c.serviceId = :serviceId"
                    + " and c.systemId = :systemId";

            Query query = entityManager.createQuery(sql, PatientSearch.class)
                    .setParameter("surname", surname)
                    .setParameter("forenames", forenames)
                    .setParameter("serviceId", serviceId.toString())
                    .setParameter("systemId", systemId.toString());

            results = query.getResultList();
        }

        entityManager.close();
        return results;
    }


    public static List<PatientSearch> searchByNames(Set<String> serviceIds, List<String> names) throws Exception {

        if (names.isEmpty()) {
            throw new IllegalArgumentException("Names cannot be empty");
        }

        EntityManager entityManager = EdsConnection.getEntityManager();

        List<PatientSearch> results = null;

        //if just one name, then treat as a surname
        if (names.size() == 1) {

            String surname = names.get(0) + "%";

            String sql = "select c"
                + " from"
                + " PatientSearch c"
                + " where lower(c.surname) LIKE lower(:surname)"
                + " and c.serviceId IN :serviceIds";

            Query query = entityManager.createQuery(sql, PatientSearch.class)
                .setParameter("surname", surname)
                .setParameter("serviceIds", serviceIds);

            results = query.getResultList();

        } else {

            //if multiple tokens, then treat all but the last as forenames
            names = new ArrayList(names);
            String surname = names.remove(names.size()-1) + "%";
            String forenames = String.join("% ", names) + "%";

            String sql = "select c"
                + " from"
                + " PatientSearch c"
                + " where lower(c.surname) LIKE lower(:surname)"
                + " and lower(c.forenames) LIKE lower(:forenames)"
                + " and c.serviceId IN :serviceIds";

            Query query = entityManager.createQuery(sql, PatientSearch.class)
                .setParameter("surname", surname)
                .setParameter("forenames", forenames)
                .setParameter("serviceIds", serviceIds);

            results = query.getResultList();
        }

        entityManager.close();
        return results;
    }

    public static PatientSearch searchByPatientId(UUID patientId) throws Exception {
        EntityManager entityManager = EdsConnection.getEntityManager();

        String sql = "select c"
                + " from"
                + " PatientSearch c"
                + " where c.patientId = :patientId";

        Query query = entityManager.createQuery(sql, PatientSearch.class)
                .setParameter("patientId", patientId.toString());

        try {
            return (PatientSearch)query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        } finally {
            entityManager.close();
        }
    }


}

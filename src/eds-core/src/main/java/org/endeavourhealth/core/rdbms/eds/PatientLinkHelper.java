package org.endeavourhealth.core.rdbms.eds;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.hl7.fhir.instance.model.Patient;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

public class PatientLinkHelper {

    public static PatientLinkPair updatePersonId(Patient fhirPatient) throws Exception {

        String patientId = fhirPatient.getId();
        String newPersonId = null;
        String previousPersonId = null;

        EntityManager entityManager = EdsConnection.getEntityManager();

        //get the current person ID for the patient
        PatientLink patientLink = getPatientLink(patientId, entityManager);
        if (patientLink != null) {
            previousPersonId = patientLink.getPersonId();
        }

        //work out what the person ID should be
        String nhsNumber = IdentifierHelper.findNhsNumberTrueNhsNumber(fhirPatient);
        if (!Strings.isNullOrEmpty(nhsNumber)) {
            String sql = "select c"
                    + " from"
                    + " PatientLinkPerson c"
                    + " where c.nhsNumber = :nhsNumber";

            Query query = entityManager.createQuery(sql, PatientLinkPerson.class)
                    .setParameter("nhsNumber", nhsNumber);

            PatientLinkPerson person = null;
            try {
                person = (PatientLinkPerson)query.getSingleResult();

            } catch (NoResultException ex) {
                //if we haven't got a person ID for this NHS number, then generate one now
                person = new PatientLinkPerson();
                person.setNhsNumber(nhsNumber);
                person.setPersonId(UUID.randomUUID().toString());

                entityManager.getTransaction().begin();
                entityManager.persist(person);
                entityManager.getTransaction().commit();
            }

            String matchingPersonId = person.getPersonId();
            if (previousPersonId == null
                    || !previousPersonId.equals(matchingPersonId)) {
                newPersonId = matchingPersonId;
            }

        } else {
            //if we don't have an NHS number, then just assign a new random person ID
            if (previousPersonId == null) {
                newPersonId = UUID.randomUUID().toString();
            }
        }

        //if we've assigned a new person ID, then record this in the history table and update the main table
        if (!Strings.isNullOrEmpty(newPersonId)) {

            PatientLinkHistory history = new PatientLinkHistory();
            history.setPatientId(patientId);
            history.setNewPersonId(newPersonId);
            history.setPreviousPersonId(previousPersonId);
            history.setUpdated(new Date());

            if (patientLink == null) {
                patientLink = new PatientLink();
                patientLink.setPatientId(patientId);
            }
            patientLink.setPersonId(newPersonId);

            entityManager.getTransaction().begin();
            entityManager.persist(history);
            entityManager.persist(patientLink);
            entityManager.getTransaction().commit();
        }

        return new PatientLinkPair(patientId, newPersonId, previousPersonId);
    }

    private static PatientLink getPatientLink(String patientId, EntityManager entityManager) {
        String sql = "select c"
                + " from"
                + " PatientLink c"
                + " where c.patientId = :patientId";

        Query query = entityManager.createQuery(sql, PatientLink.class)
                .setParameter("patientId", patientId);

        try {
            return (PatientLink)query.getSingleResult();

        } catch (NoResultException ex) {
            return null;

        }
    }

    public static String getPersonId(String patientId) throws Exception {

        EntityManager entityManager = EdsConnection.getEntityManager();

        PatientLink patientLink = getPatientLink(patientId, entityManager);
        entityManager.close();

        if (patientLink != null) {
            return patientLink.getPersonId();
        } else {
            return null;
        }
    }

    public static List<String> getPatientIds(String personId) throws Exception {
        EntityManager entityManager = EdsConnection.getEntityManager();

        String sql = "select c"
                + " from"
                + " PatientLink c"
                + " where c.personId = :personId";

        Query query = entityManager.createQuery(sql, PatientLink.class)
                .setParameter("personId", personId);

        List<String> ret = new ArrayList<>();

        List<PatientLink> links = query.getResultList();
        for (PatientLink link: links) {
            ret.add(link.getPatientId());
        }

        entityManager.close();

        return ret;
    }

    public static List<PatientLinkPair> getChangesSince(Date timestamp) throws Exception {
        EntityManager entityManager = EdsConnection.getEntityManager();

        String sql = "select c"
                + " from"
                + " PatientLinkHistory c"
                + " where c.updated >= :timestamp";

        Query query = entityManager.createQuery(sql, PatientLinkHistory.class)
                .setParameter("timestamp", timestamp);

        List<PatientLinkHistory> links = query.getResultList();

        entityManager.close();

        //sort the links by date, since we need them for the filtering
        links.sort((a, b) -> a.getUpdated().compareTo(b.getUpdated()));
        //TODO - ensure this sorting is correct

        Map<String, List<PatientLinkHistory>> updatesByPatient = new HashMap<>();

        for (PatientLinkHistory link: links) {
            String patientId = link.getPatientId();
            List<PatientLinkHistory> list = updatesByPatient.get(patientId);
            if (list == null) {
                list = new ArrayList<>();
                updatesByPatient.put(patientId, list);
            }
            list.add(link);
        }

        List<PatientLinkPair> ret = new ArrayList<>();

        //if a patient was matched to different persons MULTIPLE times since the timestamp, our
        //results will have two records, for the patient A->B and B->C. To make it easier for consumers,
        //so they don't have to follow that chain, we sanitise the results, so it shows A->C and B->C
        for (String patientId: updatesByPatient.keySet()) {

            List<PatientLinkHistory> updates = updatesByPatient.get(patientId);

            PatientLinkHistory last = updates.get(updates.size()-1);
            String latestPersonId = last.getNewPersonId();

            HashSet<String> oldPersonIds = new HashSet<>();
            for (PatientLinkHistory update: updates) {
                String oldPersonId = update.getPreviousPersonId(); //note: this may be null

                //sometimes the person ID changes back and forth, so if the old person ID is the same as the latest person ID, then skip it
                if (oldPersonId != null && oldPersonId.equals(latestPersonId)) {
                    continue;
                }

                oldPersonIds.add(oldPersonId);
            }

            for (String oldPersonId: oldPersonIds) {
                ret.add(new PatientLinkPair(patientId, latestPersonId, oldPersonId));
            }
        }

        return ret;
    }



}

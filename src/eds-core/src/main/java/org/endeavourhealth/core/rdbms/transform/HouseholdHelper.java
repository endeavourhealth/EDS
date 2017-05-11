package org.endeavourhealth.core.rdbms.transform;

import com.google.common.base.Strings;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.StringType;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class HouseholdHelper {

    public static Long findOrCreateHouseholdId(String enterpriseConfigName, Address address) throws Exception {

        //the postcode is enough to get us to a small area,
        //so we don't need to match on town or city, but since
        //postcodes can span several streets, we need to factor in
        //the road as well as number/name.
        String postcode = address.getPostalCode();
        String line1 = null;
        String line2 = null;

        if (address.hasLine()) {
            List<StringType> lines = address.getLine();
            if (lines.size() >= 1) {
                line1 = lines.get(0).getValue();
            }
            if (lines.size() >= 2) {
                line2 = lines.get(1).getValue();
            }
        }

        postcode = sanitisePostcode(postcode);
        line1 = sanitise(line1);
        line2 = sanitise(line2);

        if (Strings.isNullOrEmpty(postcode)
                || postcode.length() < 5) { //in case not a full postcode
            return null;
        }

        //we at least need one line of the address to create a household ID
        if (Strings.isNullOrEmpty(line1)) {
            return null;
        }

        //we use line 2 as part of the primary key, so make sure it's non-null
        if (line2 == null) {
            line2 = "";
        }


        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);

        Long ret = findHouseholdId(postcode, line1, line2, entityManager);
        if (ret != null) {
            entityManager.close();
            return ret;
        }

        try {
            return createHouseholdId(postcode, line1, line2, entityManager);

        } catch (Exception ex) {
            //if another thread has beat us to it, we'll get an exception, so try the find again
            ret = findHouseholdId(postcode, line1, line2, entityManager);
            if (ret != null) {
                return ret;
            }

            throw ex;
        } finally {
            entityManager.close();
        }
    }



    private static Long createHouseholdId(String postcode, String line1, String line2, EntityManager entityManager) {

        HouseholdIdMap mapping = new HouseholdIdMap();
        mapping.setPostcode(postcode);
        mapping.setLine1(line1);
        mapping.setLine2(line2);

        entityManager.getTransaction().begin();
        entityManager.persist(mapping);
        entityManager.getTransaction().commit();

        return mapping.getHouseholdId();
    }

    private static Long findHouseholdId(String postcode, String line1, String line2, EntityManager entityManager) {

        String sql = "select c"
                + " from"
                + " HouseholdIdMap c"
                + " where c.postcode = :postcode"
                + " and c.line1 = :line1"
                + " and c.line2 = :line2";


        Query query = entityManager.createQuery(sql, HouseholdIdMap.class)
                .setParameter("postcode", postcode)
                .setParameter("line1", line1)
                .setParameter("line2", line2);

        try {
            HouseholdIdMap result = (HouseholdIdMap)query.getSingleResult();
            return result.getHouseholdId();

        } catch (NoResultException ex) {
            return null;
        }
    }


    private static String sanitisePostcode(String postcode) {

        if (Strings.isNullOrEmpty(postcode)) {
            return postcode;
        }

        //force uppercase, so we don't need to convert in SQL
        postcode = postcode.toUpperCase();

        //remove spaces, so inconsistent spacing isn't a problem
        postcode = postcode.replace(" ", "");

        return postcode;
    }

    private static String sanitise(String line) {
        if (Strings.isNullOrEmpty(line)) {
            return null;
        }

        //always force to upper case, so we don't need to convert when running queries
        line = line.toUpperCase();

        //the only quick bit of sanitisation I can think of is to
        //remove any use of the word "flat" since it's redundant
        if (line.startsWith("FLAT ")) {
            line = line.substring(5);
        }
        if (line.startsWith("FLT ")) {
            line = line.substring(4);
        }
        if (line.startsWith("FT ")) {
            line = line.substring(3);
        }

        return line;
    }
}

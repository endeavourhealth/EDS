package org.endeavourhealth.core.rdbms.reference;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class PostcodeHelper {
    private static final Logger LOG = LoggerFactory.getLogger(PostcodeHelper.class);




    public static PostcodeLookup getPostcodeReference(String postcode) throws Exception {

        EntityManager entityManager = ReferenceConnection.getEntityManager();
        try {
            return getPostcodeReference(postcode, entityManager);
        } finally {
            entityManager.close();
        }
    }

    public static PostcodeLookup getPostcodeReference(String postcode, EntityManager entityManager) throws Exception {

        //if called with an empty postcode, just return null
        if (Strings.isNullOrEmpty(postcode)) {
            return null;
        }

        //we force everything to upper case when creating the table, so do that now
        postcode = postcode.toUpperCase();

        //because we've got no guarantee how/where the raw postcodes are spaced, we use the string without spaces as our primary key
        postcode = postcode.replaceAll(" ", "");

        String sql = "select r"
                   + " from PostcodeLookup r"
                   + " where r.postcodeNoSpace = :postcodeNoSpace";

        Query query = entityManager
                .createQuery(sql, PostcodeLookup.class)
                .setParameter("postcodeNoSpace", postcode);

        try {
            return (PostcodeLookup)query.getSingleResult();

        } catch (NoResultException e) {
            return null;

        }
    }


}

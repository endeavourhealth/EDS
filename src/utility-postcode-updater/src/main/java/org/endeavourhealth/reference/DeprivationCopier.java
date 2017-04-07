package org.endeavourhealth.reference;

import com.google.common.collect.Lists;
import org.endeavourhealth.core.enterprise.EnterpriseConnector;
import org.endeavourhealth.core.rdbms.reference.DeprivationLookup;
import org.endeavourhealth.core.rdbms.reference.ReferenceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DeprivationCopier {
    private static final Logger LOG = LoggerFactory.getLogger(DeprivationCopier.class);

    private static final int BATCH_SIZE = 5000;

    /**
     * copies the lsoa_lookup table in the reference DB to an Enterprise/Data Checking DB
     *
     * Usage
     * =================================================================================
     * Then run this utility as:
     *      Main copy_deprivation <enterprise config name>
     */
    public static void copyDeprivation(String[] args) throws Exception {
        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: copy_deprivation <enterprise config name>");
            return;
        }

        String enterpriseConfigName = args[1];
        LOG.info("Deprivation Copy to " + enterpriseConfigName + " Starting");

        Connection enterpriseConnection = EnterpriseConnector.openConnection(enterpriseConfigName);
        EntityManager entityManager = ReferenceConnection.getEntityManager();

        try {

            PreparedStatement update = createUpdatePreparedStatement(enterpriseConnection);

            int batch = 0;
            while (copyBatch(entityManager, batch, enterpriseConnection, update)) {
                batch ++;
                LOG.info("Done " + (batch * BATCH_SIZE));
            }

        } finally {
            enterpriseConnection.close();
            entityManager.close();
        }

        LOG.info("Finished Deprivation Copying");
    }

    private static boolean copyBatch(EntityManager entityManager, int batch, Connection enterpriseConnection, PreparedStatement update) throws Exception {

        String sql = "select c"
                + " from DeprivationLookup c";

        Query query = entityManager.createQuery(sql, DeprivationLookup.class)
                .setFirstResult(batch * BATCH_SIZE)
                .setMaxResults(BATCH_SIZE);

        List<DeprivationLookup> results = query.getResultList();
        copyBatch(results, enterpriseConnection, update);

        return results.size() == BATCH_SIZE;
    }

    private static void copyBatch(List<DeprivationLookup> lookups, Connection enterpriseConnection, PreparedStatement update) throws Exception {

        for (DeprivationLookup lookup: lookups) {
            String code = lookup.getLsoaCode();
            Integer rank = lookup.getImdRank();
            Integer decile = lookup.getImdDecile();

            update.setInt(1, rank.intValue());
            update.setInt(2, decile.intValue());
            update.setString(3, code);
            update.addBatch();
        }

        int rows = update.executeUpdate();
        if (rows != lookups.size()) {

            //if the update didn't affect as many rows as we expected, roll back and try the updates one by one
            //until we find out the row that's at fault
            enterpriseConnection.rollback();

            if (lookups.size() == 1) {
                DeprivationLookup lookup = lookups.get(0);
                throw new Exception("Failed to update lsoa_lookup record for lsoa_code " + lookup.getLsoaCode());

            } else {
                for (DeprivationLookup lookup: lookups) {
                    copyBatch(Lists.newArrayList(lookup), enterpriseConnection, update);
                }
            }

        } else {
            enterpriseConnection.commit();
        }
    }

    private static PreparedStatement createUpdatePreparedStatement(Connection connection) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE lsoa_lookup SET ");
        sb.append("imd_rank = ?, ");
        sb.append("imd_decile = ? ");
        sb.append("WHERE lsoa_code = ?");

        return connection.prepareStatement(sb.toString());
    }


}

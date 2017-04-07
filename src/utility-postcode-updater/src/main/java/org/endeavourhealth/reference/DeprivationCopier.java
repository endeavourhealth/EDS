package org.endeavourhealth.reference;

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

            Integer incomeRank = lookup.getIncomeRank();
            Integer incomeDecile = lookup.getIncomeDecile();
            Integer employmentRank = lookup.getEmploymentRank();
            Integer employmentDecile = lookup.getEmploymentDecile();
            Integer educationRank = lookup.getEducationRank();
            Integer educationDecile = lookup.getEducationDecile();
            Integer healthRank = lookup.getHealthRank();
            Integer healthDecile = lookup.getHealthDecile();
            Integer crimeRank = lookup.getCrimeRank();
            Integer crimeDecile = lookup.getCrimeDecile();
            Integer housingAndServicesBarriersRank = lookup.getHousingAndServicesBarriersRank();
            Integer housingAndServicesBarriersDecile = lookup.getHousingAndServicesBarriersDecile();
            Integer livingEnvironmentRank = lookup.getLivingEnvironmentRank();
            Integer livingEnvironmentDecile = lookup.getLivingEnvironmentDecile();

            update.setInt(1, rank.intValue());
            update.setInt(2, decile.intValue());
            update.setInt(3, incomeRank.intValue());
            update.setInt(4, incomeDecile.intValue());
            update.setInt(5, employmentRank.intValue());
            update.setInt(6, employmentDecile.intValue());
            update.setInt(7, educationRank.intValue());
            update.setInt(8, educationDecile.intValue());
            update.setInt(9, healthRank.intValue());
            update.setInt(10, healthDecile.intValue());
            update.setInt(11, crimeRank.intValue());
            update.setInt(12, crimeDecile.intValue());
            update.setInt(13, housingAndServicesBarriersRank.intValue());
            update.setInt(14, housingAndServicesBarriersDecile.intValue());
            update.setInt(15, livingEnvironmentRank.intValue());
            update.setInt(16, livingEnvironmentDecile.intValue());
            update.setString(17, code);
            update.addBatch();
        }

        int[] rows = update.executeBatch();
        if (rows.length != lookups.size()) {
            throw new Exception("Mismatch in number of batches " + lookups.size() + " and number of results " + rows.length);
        }

        //check the results to see if there were any batches that updated zero rows
        for (int i=0; i<rows.length; i++) {
            if (rows[0] == 0) {
                DeprivationLookup lookup = lookups.get(i);
                throw new Exception("Failed to update lsoa_lookup record for lsoa_code " + lookup.getLsoaCode());
            }
        }

        enterpriseConnection.commit();
    }

    private static PreparedStatement createUpdatePreparedStatement(Connection connection) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE lsoa_lookup SET ");
        sb.append("imd_rank = ?, ");
        sb.append("imd_decile = ?, ");
        sb.append("income_rank = ?, ");
        sb.append("income_decile = ?, ");
        sb.append("employment_rank = ?, ");
        sb.append("employment_decile = ?, ");
        sb.append("education_rank = ?, ");
        sb.append("education_decile = ?, ");
        sb.append("health_rank = ?, ");
        sb.append("health_decile = ?, ");
        sb.append("crime_rank = ?, ");
        sb.append("crime_decile = ?, ");
        sb.append("housing_and_services_barriers_rank = ?, ");
        sb.append("housing_and_services_barriers_decile = ?, ");
        sb.append("living_environment_rank = ?, ");
        sb.append("living_environment_decile = ? ");
        sb.append("WHERE lsoa_code = ?");

        return connection.prepareStatement(sb.toString());
    }


}

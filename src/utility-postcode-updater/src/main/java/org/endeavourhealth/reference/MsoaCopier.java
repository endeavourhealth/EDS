package org.endeavourhealth.reference;

import org.endeavourhealth.core.enterprise.EnterpriseConnector;
import org.endeavourhealth.core.rdbms.reference.MsoaLookup;
import org.endeavourhealth.core.rdbms.reference.ReferenceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MsoaCopier {
    private static final Logger LOG = LoggerFactory.getLogger(MsoaCopier.class);

    private static final int BATCH_SIZE = 5000;

    /**
     * copies the msoa_lookup table in the reference DB to an Enterprise/Data Checking DB
     *
     * Usage
     * =================================================================================
     * Then run this utility as:
     *      Main copy_msoa <enterprise config name>
     */
    public static void copyMsoas(String[] args) throws Exception {
        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: copy_msoa <enterprise config name>");
            return;
        }

        String enterpriseConfigName = args[1];
        LOG.info("MSOA Copy to " + enterpriseConfigName + " Starting");

        Connection enterpriseConnection = EnterpriseConnector.openConnection(enterpriseConfigName);
        EntityManager entityManager = ReferenceConnection.getEntityManager();

        PreparedStatement update = null;
        PreparedStatement insert = null;

        try {
            update = createUpdatePreparedStatement(enterpriseConnection);
            insert = createInsertPreparedStatement(enterpriseConnection);

            int batch = 0;
            while (copyBatch(entityManager, batch, enterpriseConnection, update, insert)) {
                batch ++;
                LOG.info("Done " + (batch * BATCH_SIZE));
            }

        } finally {
            if (update != null) {
                update.close();
            }
            if (insert != null) {
                insert.close();
            }
            enterpriseConnection.close();
            entityManager.close();
        }

        LOG.info("Finished MSOA copying");
    }

    private static boolean copyBatch(EntityManager entityManager, int batch, Connection enterpriseConnection, PreparedStatement update, PreparedStatement insert) throws Exception {

        String sql = "select c"
                + " from MsoaLookup c";

        Query query = entityManager.createQuery(sql, MsoaLookup.class)
                .setFirstResult(batch * BATCH_SIZE)
                .setMaxResults(BATCH_SIZE);

        List<MsoaLookup> results = query.getResultList();

        for (MsoaLookup lookup: results) {
            String code = lookup.getMsoaCode();
            String name = lookup.getMsoaName();

            //attempt an update first, and check if it affected any rows
            update.setString(1, name);
            update.setString(2, code);
            update.addBatch();

            int[] rows = update.executeBatch();
            int rowsUpdated = rows[0];
            if (rowsUpdated == 0) {
                //if the update didn't affect any rows, add it to the insert
                insert.setString(1, code);
                insert.setString(2, name);
                insert.addBatch();
            }
        }

        insert.executeBatch();
        enterpriseConnection.commit();

        return results.size() == BATCH_SIZE;
    }

    private static PreparedStatement createUpdatePreparedStatement(Connection connection) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE msoa_lookup SET ");
        sb.append("msoa_name = ? ");
        sb.append("WHERE msoa_code = ?");

        return connection.prepareStatement(sb.toString());
    }

    private static PreparedStatement createInsertPreparedStatement(Connection connection) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO msoa_lookup (msoa_code, msoa_name) VALUES (?, ?)");

        return connection.prepareStatement(sb.toString());
    }
}


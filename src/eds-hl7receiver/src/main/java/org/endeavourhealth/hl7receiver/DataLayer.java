package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.hl7receiver.model.db.DbConfiguration;
import org.endeavourhealth.utilities.postgres.PgStoredProc;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;

import javax.sql.DataSource;

public class DataLayer
{
    private DataSource dataSource;

    public DataLayer(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public DbConfiguration getConfiguration(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("hl7receiver.get_configuration")
                .addParameter("_instance_id", instanceId);

        return pgStoredProc.executeSingleRow((resultSet) ->
                new DbConfiguration()
                        .setInstanceId(resultSet.getString("instance_id"))
                        .setInstanceDescription(resultSet.getString("instance_description")));
    }
}

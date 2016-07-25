package org.endeavourhealth.sftpreader;

import javax.sql.DataSource;

public class DataLayer
{
    private DataSource dataSource;

    public DataLayer(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }


}

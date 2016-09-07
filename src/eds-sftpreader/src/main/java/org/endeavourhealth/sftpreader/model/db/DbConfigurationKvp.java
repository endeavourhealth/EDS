package org.endeavourhealth.sftpreader.model.db;

public class DbConfigurationKvp
{
    private String key;
    private String value;

    public String getKey()
    {
        return key;
    }

    public DbConfigurationKvp setKey(String key)
    {
        this.key = key;
        return this;
    }

    public String getValue()
    {
        return value;
    }

    public DbConfigurationKvp setValue(String value)
    {
        this.value = value;
        return this;
    }
}

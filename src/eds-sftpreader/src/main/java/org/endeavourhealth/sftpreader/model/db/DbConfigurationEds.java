package org.endeavourhealth.sftpreader.model.db;

public class DbConfigurationEds
{
    private String edsUrl;
    private String edsServiceIdentifier;
    private String softwareName;
    private String softwareVersion;
    private String envelopeContentType;

    public String getEdsUrl()
    {
        return edsUrl;
    }

    public DbConfigurationEds setEdsUrl(String edsUrl)
    {
        this.edsUrl = edsUrl;
        return this;
    }

    public String getEdsServiceIdentifier()
    {
        return edsServiceIdentifier;
    }

    public DbConfigurationEds setEdsServiceIdentifier(String edsServiceIdentifier)
    {
        this.edsServiceIdentifier = edsServiceIdentifier;
        return this;
    }

    public String getSoftwareName()
    {
        return softwareName;
    }

    public DbConfigurationEds setSoftwareName(String softwareName)
    {
        this.softwareName = softwareName;
        return this;
    }

    public String getSoftwareVersion()
    {
        return softwareVersion;
    }

    public DbConfigurationEds setSoftwareVersion(String softwareVersion)
    {
        this.softwareVersion = softwareVersion;
        return this;
    }

    public String getEnvelopeContentType()
    {
        return envelopeContentType;
    }

    public DbConfigurationEds setEnvelopeContentType(String envelopeContentType)
    {
        this.envelopeContentType = envelopeContentType;
        return this;
    }
}

package org.endeavourhealth.sftpreader.model.db;

public class DbConfigurationPgp
{
    private String pgpFileExtensionFilter;
    private String pgpSenderPublicKey;
    private String pgpRecipientPublicKey;
    private String pgpRecipientPrivateKey;
    private String pgpRecipientPrivateKeyPassword;

    public String getPgpFileExtensionFilter()
    {
        return pgpFileExtensionFilter;
    }

    public DbConfigurationPgp setPgpFileExtensionFilter(String pgpFileExtensionFilter)
    {
        this.pgpFileExtensionFilter = pgpFileExtensionFilter;
        return this;
    }

    public String getPgpSenderPublicKey()
    {
        return pgpSenderPublicKey;
    }

    public DbConfigurationPgp setPgpSenderPublicKey(String pgpSenderPublicKey)
    {
        this.pgpSenderPublicKey = pgpSenderPublicKey;
        return this;
    }

    public String getPgpRecipientPublicKey()
    {
        return pgpRecipientPublicKey;
    }

    public DbConfigurationPgp setPgpRecipientPublicKey(String pgpRecipientPublicKey)
    {
        this.pgpRecipientPublicKey = pgpRecipientPublicKey;
        return this;
    }

    public String getPgpRecipientPrivateKey()
    {
        return pgpRecipientPrivateKey;
    }

    public DbConfigurationPgp setPgpRecipientPrivateKey(String pgpRecipientPrivateKey)
    {
        this.pgpRecipientPrivateKey = pgpRecipientPrivateKey;
        return this;
    }

    public String getPgpRecipientPrivateKeyPassword()
    {
        return pgpRecipientPrivateKeyPassword;
    }

    public DbConfigurationPgp setPgpRecipientPrivateKeyPassword(String pgpRecipientPrivateKeyPassword)
    {
        this.pgpRecipientPrivateKeyPassword = pgpRecipientPrivateKeyPassword;
        return this;
    }
}

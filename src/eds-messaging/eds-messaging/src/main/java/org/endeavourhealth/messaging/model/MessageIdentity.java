package org.endeavourhealth.messaging.model;

public class MessageIdentity
{
    private String messageName;
    private String version;
    private String sender;
    private String recipient;
    private String clientCertificateThumbprint;

    public String getMessageName()
    {
        return messageName;
    }

    public void setMessageName(String messageName)
    {
        this.messageName = messageName;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getSender()
    {
        return sender;
    }

    public void setSender(String sender)
    {
        this.sender = sender;
    }

    public String getRecipient()
    {
        return recipient;
    }

    public void setRecipient(String recipient)
    {
        this.recipient = recipient;
    }

    public String getClientCertificateThumbprint()
    {
        return clientCertificateThumbprint;
    }

    public void setClientCertificateThumbprint(String certCName)
    {
        this.clientCertificateThumbprint = certCName;
    }
}

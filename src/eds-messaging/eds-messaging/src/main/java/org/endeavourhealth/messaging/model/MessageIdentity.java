package org.endeavourhealth.messaging.model;

public class MessageIdentity
{
    private String messageId;
    private String conversationId;
    private String senderId;
    private String senderCertificateThumbprint;
    private String recipientId;
    private String messageTypeId;

    public String getMessageId()
    {
        return messageId;
    }

    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    public String getConversationId()
    {
        return conversationId;
    }

    public void setConversationId(String conversationId)
    {
        this.conversationId = conversationId;
    }

    public String getSenderId()
    {
        return senderId;
    }

    public void setSenderId(String senderId)
    {
        this.senderId = senderId;
    }

    public String getSenderCertificateThumbprint()
    {
        return senderCertificateThumbprint;
    }

    public void setSenderCertificateThumbprint(String senderCertificateThumbprint)
    {
        this.senderCertificateThumbprint = senderCertificateThumbprint;
    }

    public String getRecipientId()
    {
        return recipientId;
    }

    public void setRecipientId(String recipientId)
    {
        this.recipientId = recipientId;
    }

    public String getMessageTypeId()
    {
        return messageTypeId;
    }

    public void setMessageTypeId(String messageTypeId)
    {
        this.messageTypeId = messageTypeId;
    }
}

package org.endeavourhealth.sftpreader.model.db;

import java.time.LocalDateTime;
import java.util.UUID;

public class Envelope
{
    private UUID messageId;
    private LocalDateTime timestamp;
    private String sourceName;
    private String sourceSoftware;
    private String softwareVersion;
    private String sourceEndpoint;
    private String payloadType;
    private String payload;

    public UUID getMessageId()
    {
        return messageId;
    }

    public Envelope setMessageId(UUID messageId)
    {
        this.messageId = messageId;
        return this;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public Envelope setTimestamp(LocalDateTime timestamp)
    {
        this.timestamp = timestamp;
        return this;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public Envelope setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
        return this;
    }

    public String getSoftwareVersion()
    {
        return softwareVersion;
    }

    public Envelope setSoftwareVersion(String softwareVersion)
    {
        this.softwareVersion = softwareVersion;
        return this;
    }

    public String getSourceEndpoint()
    {
        return sourceEndpoint;
    }

    public Envelope setSourceEndpoint(String sourceEndpoint)
    {
        this.sourceEndpoint = sourceEndpoint;
        return this;
    }

    public String getPayloadType()
    {
        return payloadType;
    }

    public Envelope setPayloadType(String payloadType)
    {
        this.payloadType = payloadType;
        return this;
    }

    public String getPayload()
    {
        return payload;
    }

    public Envelope setPayload(String payload)
    {
        this.payload = payload;
        return this;
    }
}

package org.endeavourhealth.sftpreader;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EdsEnvelope
{
    private static final String EDS_ENVELOPE_TEMPLATE_FILENAME = "EdsEnvelopeTemplate.xml";

    private String buildEnvelope() throws IOException
    {
        String edsEnvelope = Resources.toString(Resources.getResource(EDS_ENVELOPE_TEMPLATE_FILENAME), Charsets.UTF_8);

        Map<String, String> test = new HashMap<String, String>()
        {
            {
                put("{{message-id}}", messageId.toString());
                put("{{timestamp}}", timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                put("{{source-name}}", sourceName);
                put("{{source-software}}", sourceSoftware);
                put("{{source-endpoint}}", sourceEndpoint);
                put("{{payload-id}}", UUID.randomUUID().toString());
                put("{{payload-type}}", payloadType);
                put("{{payload-base64}}", Base64.getEncoder().encodeToString(payload.getBytes()));
            }
        };

        for (String replacement : test.keySet())
            edsEnvelope = edsEnvelope.replaceAll(replacement, test.get(replacement));

        return edsEnvelope;
    }

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

    public EdsEnvelope setMessageId(UUID messageId)
    {
        this.messageId = messageId;
        return this;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public EdsEnvelope setTimestamp(LocalDateTime timestamp)
    {
        this.timestamp = timestamp;
        return this;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public EdsEnvelope setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
        return this;
    }

    public String getSoftwareVersion()
    {
        return softwareVersion;
    }

    public EdsEnvelope setSoftwareVersion(String softwareVersion)
    {
        this.softwareVersion = softwareVersion;
        return this;
    }

    public String getSourceEndpoint()
    {
        return sourceEndpoint;
    }

    public EdsEnvelope setSourceEndpoint(String sourceEndpoint)
    {
        this.sourceEndpoint = sourceEndpoint;
        return this;
    }

    public String getPayloadType()
    {
        return payloadType;
    }

    public EdsEnvelope setPayloadType(String payloadType)
    {
        this.payloadType = payloadType;
        return this;
    }

    public String getPayload()
    {
        return payload;
    }

    public EdsEnvelope setPayload(String payload)
    {
        this.payload = payload;
        return this;
    }
}

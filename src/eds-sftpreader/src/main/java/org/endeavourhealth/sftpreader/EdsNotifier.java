package org.endeavourhealth.sftpreader;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.endeavourhealth.sftpreader.model.db.DbConfigurationEds;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EdsNotifier
{
    private static final String EDS_ENVELOPE_TEMPLATE_FILENAME = "EdsEnvelopeTemplate.xml";

    private DbConfigurationEds dbConfigurationEds;
    private String payload;
    private UUID messageId;
    private LocalDateTime timestamp;
    private String outboundMessage;
    private String inboundMessage;

    public EdsNotifier(DbConfigurationEds dbConfigurationEds, String payload)
    {
        Validate.notNull(dbConfigurationEds, "dbConfigurationEds is null");
        Validate.notBlank(payload, "payload is blank");

        this.dbConfigurationEds = dbConfigurationEds;
        this.payload = payload;
        this.messageId = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
    }

    public void notifyEds() throws IOException
    {
        outboundMessage = buildEnvelope();

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(dbConfigurationEds.getEdsUrl());
        httpPost.setEntity(new ByteArrayEntity(outboundMessage.getBytes()));

        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();

        if (entity != null)
        {
            InputStream instream = entity.getContent();

            try
            {
                // do something useful
            }
            finally
            {
                instream.close();
            }
        }
    }

    private String buildEnvelope() throws IOException
    {
        Validate.notNull(dbConfigurationEds, "dbConfigurationEds is null");
        Validate.notBlank(dbConfigurationEds.getEdsServiceIdentifier(), "dbConfigurationEds.edsServiceIdentifier is blank");
        Validate.notBlank(dbConfigurationEds.getEdsUrl(), "dbConfigurationEds.edsUrl is blank");
        Validate.notBlank(dbConfigurationEds.getSoftwareName(), "dbConfigurationEds.softwareName is blank");
        Validate.notBlank(dbConfigurationEds.getSoftwareVersion(), "dbConfigurationEds.softwareVersion is blank");

        String edsEnvelope = Resources.toString(Resources.getResource(EDS_ENVELOPE_TEMPLATE_FILENAME), Charsets.UTF_8);

        Map<String, String> test = new HashMap<String, String>()
        {
            {
                put("{{message-id}}", messageId.toString());
                put("{{timestamp}}", timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                put("{{source-name}}", dbConfigurationEds.getEdsServiceIdentifier());
                put("{{source-software}}", dbConfigurationEds.getSoftwareName());
                put("{{source-version}}", dbConfigurationEds.getSoftwareVersion());
                put("{{source-endpoint}}", "");
                put("{{payload-id}}", UUID.randomUUID().toString());
                put("{{payload-type}}", dbConfigurationEds.getEnvelopeContentType());
                put("{{payload-base64}}", Base64.getEncoder().encodeToString(payload.getBytes()));
            }
        };

        for (String replacement : test.keySet())
            edsEnvelope = edsEnvelope.replace(replacement, test.get(replacement));

        return edsEnvelope;
    }

    public UUID getMessageId()
    {
        return this.messageId;
    }

    public String getOutboundMessage()
    {
        return this.outboundMessage;
    }

    public String getInboundMessage()
    {
        return this.inboundMessage;
    }
}

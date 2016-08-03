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
import org.endeavourhealth.sftpreader.model.db.Envelope;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EdsNotifier
{
    private static final String EDS_ENVELOPE_TEMPLATE_FILENAME = "EdsEnvelopeTemplate.xml";

    public void notifyEds(String url, String message) throws IOException
    {
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new ByteArrayEntity(message.getBytes()));

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

    private String buildEnvelope(Envelope envelope) throws IOException
    {
        Validate.notNull(envelope, "envelope is null");

        String edsEnvelope = Resources.toString(Resources.getResource(EDS_ENVELOPE_TEMPLATE_FILENAME), Charsets.UTF_8);

        Map<String, String> test = new HashMap<String, String>()
        {
            {
                put("{{message-id}}", envelope.getMessageId().toString());
                put("{{timestamp}}", envelope.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                put("{{source-name}}", envelope.getSourceName());
                put("{{source-software}}", envelope.getSourceName());
                put("{{source-endpoint}}", envelope.getSourceEndpoint());
                put("{{payload-id}}", UUID.randomUUID().toString());
                put("{{payload-type}}", envelope.getPayloadType());
                put("{{payload-base64}}", Base64.getEncoder().encodeToString(envelope.getPayload().getBytes()));
            }
        };

        for (String replacement : test.keySet())
            edsEnvelope = edsEnvelope.replaceAll(replacement, test.get(replacement));

        return edsEnvelope;
    }
}

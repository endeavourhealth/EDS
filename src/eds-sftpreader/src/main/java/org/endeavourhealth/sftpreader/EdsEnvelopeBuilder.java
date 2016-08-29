package org.endeavourhealth.sftpreader;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.sftpreader.model.db.DbConfigurationEds;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EdsEnvelopeBuilder
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EdsEnvelopeBuilder.class);
    private static final String EDS_ENVELOPE_TEMPLATE_FILENAME = "EdsEnvelopeTemplate.xml";

    private DbConfigurationEds dbConfigurationEds;

    public EdsEnvelopeBuilder(DbConfigurationEds dbConfigurationEds)
    {
        Validate.notNull(dbConfigurationEds, "dbConfigurationEds is null");

        this.dbConfigurationEds = dbConfigurationEds;
    }

    public String buildEnvelope(UUID messageId, String messagePayload, String organisationId) throws IOException
    {
        Validate.notBlank(messagePayload, "messagePayload is blank");
        Validate.notNull(dbConfigurationEds, "dbConfigurationEds is null");
        Validate.notBlank(dbConfigurationEds.getEdsServiceIdentifier(), "dbConfigurationEds.edsServiceIdentifier is blank");
        Validate.notBlank(dbConfigurationEds.getEdsUrl(), "dbConfigurationEds.edsUrl is blank");
        Validate.notBlank(dbConfigurationEds.getSoftwareName(), "dbConfigurationEds.softwareName is blank");
        Validate.notBlank(dbConfigurationEds.getSoftwareVersion(), "dbConfigurationEds.softwareVersion is blank");

        String sourceName = dbConfigurationEds.getEdsServiceIdentifier() + "//" + organisationId;

        String edsEnvelope = Resources.toString(Resources.getResource(EDS_ENVELOPE_TEMPLATE_FILENAME), Charsets.UTF_8);

        Map<String, String> test = new HashMap<String, String>()
        {
            {
                put("{{message-id}}", messageId.toString());
                put("{{timestamp}}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                put("{{source-name}}", sourceName);
                put("{{source-software}}", dbConfigurationEds.getSoftwareName());
                put("{{source-version}}", dbConfigurationEds.getSoftwareVersion());
                put("{{source-endpoint}}", "");
                put("{{payload-id}}", UUID.randomUUID().toString());
                put("{{payload-type}}", dbConfigurationEds.getEnvelopeContentType());
                put("{{payload-base64}}", Base64.getEncoder().encodeToString(messagePayload.getBytes()));
            }
        };

        for (String replacement : test.keySet())
            edsEnvelope = edsEnvelope.replace(replacement, test.get(replacement));

        return edsEnvelope;
    }
}

package org.endeavourhealth.sftpreader.implementations.emis;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.sftpreader.implementations.SftpFilenameParser;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.model.db.DbConfigurationKvp;
import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class EmisSftpFilenameParser extends SftpFilenameParser
{
                                                                        // same as ISO pattern but switch : for . so can be used as filename
    private static final DateTimeFormatter BATCH_IDENTIFIER_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH'.'mm'.'ss");
    private static final String SHARING_AGREEMENT_UUID_KEY = "SharingAgreementGuid";

    private ProcessingIdSet processingIds;
    private String schemaName;
    private String tableName;
    private LocalDateTime extractDateTime;
    private UUID sharingAgreementUuid;

    public EmisSftpFilenameParser(String filename, DbConfiguration dbConfiguration, String fileExtension)
    {
        super(filename, dbConfiguration, fileExtension);
    }

    public EmisSftpFilenameParser(String filename, DbConfiguration dbConfiguration)
    {
        super(filename, dbConfiguration);
    }

    @Override
    public String generateBatchIdentifier()
    {
        return this.extractDateTime.format(BATCH_IDENTIFIER_FORMAT);
    }

    public static LocalDateTime parseBatchIdentifier(String batchIdentifier)
    {
        return LocalDateTime.parse(batchIdentifier, BATCH_IDENTIFIER_FORMAT);
    }

    @Override
    public String generateFileTypeIdentifier()
    {
        return schemaName + "_" + tableName;
    }

    @Override
    protected void parseFilename(String filename, String pgpFileExtensionFilter) throws SftpFilenameParseException
    {
        String[] parts = filename.split("_");

        if (parts.length != 5)
            throw new SftpFilenameParseException("Emis batch filename could not be parsed");

        String processingIdPart = parts[0];
        String schemaNamePart = parts[1];
        String tableNamePart = parts[2];
        String extractDateTimePart = parts[3];
        String sharingAgreementGuidWithFileExtensionPart = parts[4];

        this.processingIds = ProcessingIdSet.parseBatchIdentifier(processingIdPart);

        if (StringUtils.isEmpty(schemaNamePart))
            throw new SftpFilenameParseException("Schema name is empty");

        this.schemaName = schemaNamePart;

        if (StringUtils.isEmpty(tableNamePart))
            throw new SftpFilenameParseException("Table name is empty");

        this.tableName = tableNamePart;

        if (StringUtils.isEmpty(extractDateTimePart))
            throw new SftpFilenameParseException("Extract date/time is empty");

        this.extractDateTime = LocalDateTime.parse(extractDateTimePart, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        if (!StringUtils.endsWith(sharingAgreementGuidWithFileExtensionPart, pgpFileExtensionFilter))
            throw new SftpFilenameParseException("File does not end with " + pgpFileExtensionFilter);

        String[] sharingAgreementParts = sharingAgreementGuidWithFileExtensionPart.split("[.]");
        String sharingAgreementGuid = sharingAgreementParts[0];

        if (StringUtils.isEmpty(sharingAgreementGuid))
            throw new SftpFilenameParseException("Sharing agreement UUID is empty");

        this.sharingAgreementUuid = UUID.fromString(sharingAgreementGuid);

        if (!sharingAgreementUuid.equals(getSharingAgreementUuidFromConfiguration()))
            throw new SftpFilenameParseException("Sharing agreement UUID does not match that in configuration key value pair");
    }

    private UUID getSharingAgreementUuidFromConfiguration() throws SftpFilenameParseException
    {
        for (DbConfigurationKvp dbConfigurationKvp : dbConfiguration.getDbConfigurationKvp())
            if (dbConfigurationKvp.getKey().equals(SHARING_AGREEMENT_UUID_KEY))
                return UUID.fromString(dbConfigurationKvp.getValue());

        throw new SftpFilenameParseException(SHARING_AGREEMENT_UUID_KEY + " has not been configured in configuration key value pair");
    }

    public ProcessingIdSet getProcessingIds()
    {
        return this.processingIds;
    }

    public LocalDateTime getExtractDateTime()
    {
        return extractDateTime;
    }

    public UUID getSharingAgreementUuid()
    {
        return sharingAgreementUuid;
    }
}

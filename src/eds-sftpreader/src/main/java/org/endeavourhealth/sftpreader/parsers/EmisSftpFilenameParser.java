package org.endeavourhealth.sftpreader.parsers;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class EmisSftpFilenameParser extends SftpFilenameParser
{
    private int processingIdStart;
    private int processingIdEnd;
    private String schemaName;
    private String tableName;
    private LocalDateTime extractDateTime;
    private UUID sharingAgreementUuid;

    public EmisSftpFilenameParser(String filename, String pgpFileExtensionFilter, List<String> validFileTypeIdentifiers)
    {
        super(filename, pgpFileExtensionFilter, validFileTypeIdentifiers);
    }

    @Override
    public String generateBatchIdentifier()
    {
        return Integer.toString(processingIdStart) + "-" + Integer.toString(processingIdEnd);
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

        if (StringUtils.isBlank(processingIdPart))
            throw new SftpFilenameParseException("No processing ids present");

        String[] processingIdParts = processingIdPart.split("-");

        this.processingIdStart = Integer.parseInt(processingIdParts[0]);

        this.processingIdEnd = this.processingIdStart;

        if (processingIdParts.length == 2)
            this.processingIdEnd = Integer.parseInt(processingIdParts[1]);
        else if (processingIdParts.length > 2)
            throw new SftpFilenameParseException("Too many processing ids");

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
    }
}

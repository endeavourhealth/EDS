package org.endeavourhealth.sftpreader.batchFileImplementations;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class EmisBatchFile extends BatchFile
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EmisBatchFile.class);

    private boolean isFilenameValid = false;
    private int processingIdStart;
    private int processingIdEnd;
    private String schemaName;
    private String tableName;
    private LocalDateTime extractDateTime;
    private UUID sharingAgreementUuid;

    EmisBatchFile(SftpRemoteFile sftpRemoteFile, String localRootPath, String pgpFileExtensionFilter, List<String> validFileTypeIdentifiers)
    {
        super(sftpRemoteFile, localRootPath, pgpFileExtensionFilter, validFileTypeIdentifiers);

        Validate.notNull(sftpRemoteFile, "sftpRemoteFile is null");
        Validate.notBlank(localRootPath, "localRootPath is blank");
        Validate.notBlank(pgpFileExtensionFilter, "pgpFileExtensionFilter is blank");
        Validate.notNull(validFileTypeIdentifiers, "validFileTypeIdentifiers is null");

        parseFilename();
    }

    @Override
    public boolean isFilenameValid()
    {
        return (isFilenameValid && validFileTypeIdentifiers.contains(getFileTypeIdentifier()));
    }

    @Override
    public String getBatchIdentifier()
    {
        if (!isFilenameValid)
            return "UNKNOWN";

        return Integer.toString(processingIdStart) + "-" + Integer.toString(processingIdEnd);
    }

    @Override
    public String getFileTypeIdentifier()
    {
        if (!isFilenameValid)
            return "UNKNOWN";

        return schemaName + "_" + tableName;
    }

    private void parseFilename()
    {
        try
        {
            String[] parts = super.getFilename().split("_");

            if (parts.length != 5)
                throw new BatchFilenameParseException("Emis batch filename could not be parsed");

            String processingIdPart = parts[0];
            String schemaNamePart = parts[1];
            String tableNamePart = parts[2];
            String extractDateTimePart = parts[3];
            String sharingAgreementGuidWithFileExtensionPart = parts[4];

            if (StringUtils.isBlank(processingIdPart))
                throw new BatchFilenameParseException("No processing ids present");

            String[] processingIdParts = processingIdPart.split("-");

            this.processingIdStart = Integer.parseInt(processingIdParts[0]);

            this.processingIdEnd = this.processingIdStart;

            if (processingIdParts.length == 2)
                this.processingIdEnd = Integer.parseInt(processingIdParts[1]);
            else if (processingIdParts.length > 2)
                throw new BatchFilenameParseException("Too many processing ids");

            if (StringUtils.isEmpty(schemaNamePart))
                throw new BatchFilenameParseException("Schema name is empty");

            this.schemaName = schemaNamePart;

            if (StringUtils.isEmpty(tableNamePart))
                throw new BatchFilenameParseException("Table name is empty");

            this.tableName = tableNamePart;

            if (StringUtils.isEmpty(extractDateTimePart))
                throw new BatchFilenameParseException("Extract date/time is empty");

            this.extractDateTime = LocalDateTime.parse(extractDateTimePart, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            if (!StringUtils.endsWith(sharingAgreementGuidWithFileExtensionPart, this.pgpFileExtensionFilter))
                throw new BatchFilenameParseException("File does not end with " + this.pgpFileExtensionFilter);

            String[] sharingAgreementParts = sharingAgreementGuidWithFileExtensionPart.split("[.]");
            String sharingAgreementGuid = sharingAgreementParts[0];

            if (StringUtils.isEmpty(sharingAgreementGuid))
                throw new BatchFilenameParseException("Sharing agreement UUID is empty");

            this.sharingAgreementUuid = UUID.fromString(sharingAgreementGuid);

            this.isFilenameValid = true;
        }
        catch (Exception e)
        {
            LOG.error("Could not parse EMIS filename", e);
        }
    }
}

package org.endeavourhealth.sftpreader.implementations;

import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class SftpFilenameParser
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SftpFilenameParser.class);

    private boolean isFilenameValid = false;
    private String pgpFileExtensionFilter;

    public SftpFilenameParser(String filename, String pgpFileExtensionFilter, List<String> validFileTypeIdentifiers)
    {
        this.pgpFileExtensionFilter = pgpFileExtensionFilter;

        try
        {
            parseFilename(filename, pgpFileExtensionFilter);

            if (!validFileTypeIdentifiers.contains(generateFileTypeIdentifier()))
                throw new SftpFilenameParseException("File type " + generateFileTypeIdentifier() + " not recognised");

            isFilenameValid = true;
        }
        catch (Exception e)
        {
            isFilenameValid = false;
            LOG.error("Error parsing filename " + filename, e);
        }
    }

    protected abstract void parseFilename(String filename, String pgpFileExtensionFilter) throws SftpFilenameParseException;
    protected abstract String generateBatchIdentifier();
    protected abstract String generateFileTypeIdentifier();

    public boolean isFilenameValid()
    {
        return this.isFilenameValid;
    }

    public String getBatchIdentifier()
    {
        if (!isFilenameValid)
            return "UNKNOWN";

        return generateBatchIdentifier();
    }

    public String getFileTypeIdentifier()
    {
        if (!isFilenameValid)
            return "UNKNOWN";

        return generateFileTypeIdentifier();
    }

    public String getPgpFileExtensionFilter()
    {
        return this.pgpFileExtensionFilter;
    }
}

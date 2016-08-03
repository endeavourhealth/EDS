package org.endeavourhealth.sftpreader.model.exceptions;

public class SftpFilenameParseException extends SftpReaderException
{
    public SftpFilenameParseException() {
        super();
    }
    public SftpFilenameParseException(String message) {
        super(message);
    }
    public SftpFilenameParseException(String message, Throwable cause) {
        super(message, cause);
    }
    public SftpFilenameParseException(Throwable cause) {
        super(cause);
    }
}

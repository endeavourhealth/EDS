package org.endeavourhealth.sftpreader.batchFileImplementations;

public class BatchFilenameParseException extends Exception
{
    public BatchFilenameParseException() {
        super();
    }
    public BatchFilenameParseException(String message) {
        super(message);
    }
    public BatchFilenameParseException(String message, Throwable cause) {
        super(message, cause);
    }
    public BatchFilenameParseException(Throwable cause) {
        super(cause);
    }
}

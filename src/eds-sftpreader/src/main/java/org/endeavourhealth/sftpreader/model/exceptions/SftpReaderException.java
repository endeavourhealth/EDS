package org.endeavourhealth.sftpreader.model.exceptions;

public class SftpReaderException extends Exception
{
    public SftpReaderException() {
        super();
    }
    public SftpReaderException(String message) {
        super(message);
    }
    public SftpReaderException(String message, Throwable cause) {
        super(message, cause);
    }
    public SftpReaderException(Throwable cause) {
        super(cause);
    }
}

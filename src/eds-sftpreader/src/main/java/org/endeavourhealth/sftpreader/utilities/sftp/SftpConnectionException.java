package org.endeavourhealth.sftpreader.utilities.sftp;

public class SftpConnectionException extends Exception
{
    public SftpConnectionException() {
        super();
    }
    public SftpConnectionException(String message) {
        super(message);
    }
    public SftpConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
    public SftpConnectionException(Throwable cause) {
        super(cause);
    }
}

package org.endeavourhealth.sftpreader.utilities.postgres;

public class PgStoredProcException extends Exception
{
    public PgStoredProcException() {
        super();
    }
    public PgStoredProcException(String message) {
        super(message);
    }
    public PgStoredProcException(String message, Throwable cause) {
        super(message, cause);
    }
    public PgStoredProcException(Throwable cause) {
        super(cause);
    }
}

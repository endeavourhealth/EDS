package org.endeavourhealth.utilities.configuration;

public class LocalConfigurationException extends Exception {
    public LocalConfigurationException() {
        super();
    }
    public LocalConfigurationException(String message) {
        super(message);
    }
    public LocalConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
    public LocalConfigurationException(Throwable cause) {
        super(cause);
    }
}

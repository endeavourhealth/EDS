package org.endeavourhealth.hl7receiver.model.exceptions;

public class LogbackConfigurationException extends ConfigurationException {
    public LogbackConfigurationException() {
        super();
    }
    public LogbackConfigurationException(String message) {
        super(message);
    }
    public LogbackConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
    public LogbackConfigurationException(Throwable cause) {
        super(cause);
    }
}

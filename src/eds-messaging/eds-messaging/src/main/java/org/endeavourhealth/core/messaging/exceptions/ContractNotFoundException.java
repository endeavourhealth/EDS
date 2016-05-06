package org.endeavourhealth.core.messaging.exceptions;

public class ContractNotFoundException extends ResolutionException
{
    public ContractNotFoundException() {
        super();
    }
    public ContractNotFoundException(String message) { super(message); }
    public ContractNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public ContractNotFoundException(Throwable cause) {
        super(cause);
    }
}

package org.endeavourhealth.core.messaging.model;

import org.endeavourhealth.core.messaging.exceptions.EndpointException;

public interface IReceiver
{
    MessageIdentity identifyMessage(Message message) throws EndpointException;
    void handleError(Exception e);
}

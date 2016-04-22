package org.endeavourhealth.messaging.model;

import org.endeavourhealth.messaging.exceptions.EndpointException;

public interface IReceiver
{
    MessageIdentity identifyMessage(Message message) throws EndpointException;
    void handleError(Exception e);
}

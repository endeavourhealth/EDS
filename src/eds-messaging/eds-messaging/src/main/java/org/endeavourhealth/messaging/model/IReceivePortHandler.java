package org.endeavourhealth.messaging.model;

import org.endeavourhealth.messaging.exceptions.EndpointException;

public interface IReceivePortHandler
{
    MessageIdentity identifyMessage(Message message) throws EndpointException;
    void handleError(Exception e);
}

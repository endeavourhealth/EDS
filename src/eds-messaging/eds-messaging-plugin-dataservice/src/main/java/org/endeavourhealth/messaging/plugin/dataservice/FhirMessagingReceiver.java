package org.endeavourhealth.messaging.plugin.dataservice;

import org.endeavourhealth.messaging.model.Message;
import org.endeavourhealth.messaging.model.IReceiver;
import org.endeavourhealth.messaging.model.MessageIdentity;

public class FhirMessagingReceiver implements IReceiver
{
    @Override
    public MessageIdentity identifyMessage(Message message)
    {
        // TODO
        return new MessageIdentity();
    }

    public void handleError(Exception e)
    {

    }
}

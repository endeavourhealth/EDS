package org.endeavourhealth.messaging;

import org.endeavourhealth.messaging.model.Message;

public class MessagePipeline
{
    public void Process(Message message) throws Exception
    {
        // load message processor by on message identity
        //MessageRoute messageRoute = configuration.getRoute(messageIdentity);

        // log message


        // check certificate

        // check data sharing protocols

        // queue or deliver message

        // debugging
    }
}

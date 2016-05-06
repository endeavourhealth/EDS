package org.endeavourhealth.core.messaging;

import org.endeavourhealth.core.messaging.configuration.Configuration;
import org.endeavourhealth.core.messaging.model.IMessageProcessor;
import org.endeavourhealth.core.messaging.model.Message;

public class MessagePipeline
{
    public void Process(Message message) throws Exception
    {
        Configuration configuration = Configuration.getInstance();

        IMessageProcessor messageProcessor = configuration.getMessageProcessor(message.getMessageIdentity());

        // log message


        // check certificate

        // check data sharing protocols

        // queue or deliver message

        // debugging
    }
}

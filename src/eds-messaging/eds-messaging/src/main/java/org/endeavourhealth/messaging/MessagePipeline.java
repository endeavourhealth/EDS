package org.endeavourhealth.messaging;

import org.endeavourhealth.messaging.configuration.Configuration;
import org.endeavourhealth.messaging.model.IMessageProcessor;
import org.endeavourhealth.messaging.model.Message;

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

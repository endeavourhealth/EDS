package org.endeavourhealth.core.messaging.plugin.dataservice;

import org.endeavourhealth.core.messaging.exceptions.EndpointException;
import org.endeavourhealth.core.messaging.model.IReceiver;
import org.endeavourhealth.core.messaging.model.Message;
import org.endeavourhealth.core.messaging.model.MessageIdentity;
import org.endeavourhealth.core.utilities.XmlHelper;
import org.w3c.dom.Document;

public class EndeavourEnvelopeReceiver implements IReceiver
{
    @Override
    public MessageIdentity identifyMessage(Message message) throws EndpointException
    {
        try
        {
            Document document = XmlHelper.documentFromString(message.getBody());

            MessageIdentity identifier = new MessageIdentity();

            identifier.setMessageId(XmlHelper.getXPathString(document, "/EndeavourEnvelope1-0/Header/MessageId"));
            identifier.setConversationId(XmlHelper.getXPathString(document, "/EndeavourEnvelope1-0/Header/ConversationId"));
            identifier.setSenderId(XmlHelper.getXPathString(document, "/EndeavourEnvelope1-0/Header/SenderId"));
            identifier.setSenderCertificateThumbprint(message.getHeaderValue("SSL-Client-Fingerprint"));
            identifier.setRecipientId(XmlHelper.getXPathString(document, "/EndeavourEnvelope1-0/Header/RecipientId"));
            identifier.setMessageTypeId(XmlHelper.getXPathString(document, "/EndeavourEnvelope1-0/Header/MessageTypeId"));

            return identifier;
        }
        catch (Exception e)
        {
            throw new EndpointException(e);
        }
    }

    public void handleError(Exception e)
    {

    }
}

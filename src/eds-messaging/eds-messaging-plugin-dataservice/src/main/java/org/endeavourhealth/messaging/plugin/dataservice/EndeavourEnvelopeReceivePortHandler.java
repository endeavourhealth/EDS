package org.endeavourhealth.messaging.plugin.dataservice;

import org.endeavourhealth.messaging.exceptions.EndpointException;
import org.endeavourhealth.messaging.model.IReceivePortHandler;
import org.endeavourhealth.messaging.model.Message;
import org.endeavourhealth.messaging.model.MessageIdentity;
import org.endeavourhealth.messaging.utilities.XmlHelper;
import org.w3c.dom.Document;

public class EndeavourEnvelopeReceivePortHandler implements IReceivePortHandler
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

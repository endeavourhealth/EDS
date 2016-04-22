package org.endeavourhealth.messaging.plugin.gpcim;

import org.endeavourhealth.messaging.model.Message;
import org.endeavourhealth.messaging.exceptions.EndpointException;
import org.endeavourhealth.messaging.model.MessageIdentity;
import org.endeavourhealth.messaging.model.IReceiver;
import org.endeavourhealth.messaging.utilities.XmlHelper;
import org.w3c.dom.Document;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

public class MigSoapReceiver implements IReceiver
{
    @Override
    public MessageIdentity identifyMessage(Message message) throws EndpointException
    {
        try
        {
            Document document = XmlHelper.documentFromString(message.getBody());

            MessageIdentity identifier = new MessageIdentity();

            identifier.setMessageName(XmlHelper.getXPathString(document, "/Envelope/Body/migRequest/serviceDefinition/name"));
            identifier.setVersion(XmlHelper.getXPathString(document, "/Envelope/Body/migRequest/serviceDefinition/version"));
            identifier.setSender(XmlHelper.getXPathString(document, "/Envelope/Body/migRequest/serviceHeader/source/identifier"));
            identifier.setRecipient(XmlHelper.getXPathString(document, "/Envelope/Body/migRequest/serviceHeader/target/nationalCode"));
            identifier.setClientCertificateThumbprint(message.getHeaderValue("SSL-Client-Fingerprint"));

            return identifier;
        }
        catch (Exception e)
        {
            throw new EndpointException(e);
        }
    }

    public void handleError(Exception e)
    {
        try
        {
            MessageFactory factory = MessageFactory.newInstance();

            SOAPMessage message = factory.createMessage();
            message.getSOAPPart();

        }
        catch (Exception ex)
        {
            // ?
        }
    }
}

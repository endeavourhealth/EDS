package org.endeavourhealth.messaging.utilities;

import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

public class XmlHelper
{
    public static <T> T deserialize(String xml, Class<T> objectClass) throws SerializationException
    {
        try
        {
            StringReader reader = new StringReader(xml);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);

            JAXBContext jaxbContent = JAXBContext.newInstance(objectClass);
            Unmarshaller unmarshaller = jaxbContent.createUnmarshaller();

            return (T)unmarshaller.unmarshal(xmlReader, objectClass).getValue();
        }
        catch (Exception e)
        {
            throw new SerializationException(String.format("Error deserialising %s", objectClass.getTypeName()), e);
        }
    }

    public static Document documentFromString(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public static String getXPathString(Document document, String xpath) throws XPathExpressionException
    {
        return XPathFactory.newInstance().newXPath().evaluate(xpath, document);
    }
}

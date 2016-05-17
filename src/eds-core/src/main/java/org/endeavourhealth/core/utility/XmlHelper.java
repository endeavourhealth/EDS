package org.endeavourhealth.core.utility;

import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;

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

    public static <T> String serialize(JAXBElement<T> object) throws SerializationException
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Marshaller m = jaxbContext.createMarshaller();
            StringWriter stringWriter = new StringWriter();
            m.marshal(object, stringWriter);
            return stringWriter.toString();
        }
        catch (JAXBException e)
        {
            throw new SerializationException(String.format("Error serialising %s", object.getClass().getTypeName()), e);
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

    public static void validate(String xml, String xsd) throws Exception
    {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new StreamSource(new StringReader(xsd)));
        Source xmlFile = new StreamSource(new StringReader(xml));

        Validator validator = schema.newValidator();

        try
        {
            validator.validate(xmlFile);
        }
        catch (SAXException e)
        {
            throw e;
        }
    }
}

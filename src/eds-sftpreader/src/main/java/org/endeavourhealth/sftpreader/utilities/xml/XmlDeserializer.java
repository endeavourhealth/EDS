package org.endeavourhealth.sftpreader.utilities.xml;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class XmlDeserializer
{
    public static <T> T deserializeFromResource(Class cls, String xmlResourceName, String xsdName) throws ParserConfigurationException, JAXBException, IOException, SAXException
    {
        String xml = Resources.toString(Resources.getResource(xmlResourceName), Charsets.UTF_8);

        InputStream is = new ByteArrayInputStream(removeXmlStringNoNamespace(xml).getBytes());
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = docBuilder.parse(is);

        return deserializeFromXmlDocument(cls, document, xsdName);
    }

    private static <T> T deserializeFromXmlDocument(Class cls, Document doc, String xsdName) throws ParserConfigurationException, JAXBException, IOException, SAXException
    {
        JAXBContext context = JAXBContext.newInstance(cls);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        if (xsdName != null)
        {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL url = Resources.getResource(xsdName);
            Schema schema = sf.newSchema(url);
            unmarshaller.setSchema(schema);
        }

        @SuppressWarnings("unchecked")
        JAXBElement<T> loader = unmarshaller.unmarshal(doc, cls);
        return loader.getValue();
    }

    private static String removeXmlStringNoNamespace(String xmlString)
    {
        return xmlString.replaceAll("xsi:noNamespaceSchemaLocation=.*?(\"|\').*?(\"|\')", ""); /* remove xmlns declaration */
    }
}
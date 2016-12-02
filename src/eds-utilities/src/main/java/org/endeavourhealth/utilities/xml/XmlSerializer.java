package org.endeavourhealth.utilities.xml;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class XmlSerializer
{
    public static <T> T deserializeFromString(Class cls, String xml, String xsdName) throws ParserConfigurationException, JAXBException, IOException, SAXException
    {
        String newXml = removeXmlStringNoNamespace(xml);

        InputStream is = new ByteArrayInputStream(newXml.getBytes());
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = docBuilder.parse(is);

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
        JAXBElement<T> loader = unmarshaller.unmarshal(document, cls);
        return loader.getValue();
    }

    public static <T> T deserializeFromResource(Class cls, String xmlResourceName, String xsdName) throws ParserConfigurationException, JAXBException, IOException, SAXException
    {
        URL urlItem = Resources.getResource(xmlResourceName);
        String xml = com.google.common.io.Resources.toString(urlItem, Charsets.UTF_8);
        return deserializeFromString(cls, xml, xsdName);
    }

    public static <T> T deserializeFromFile(Class cls, String path, String xsdName) throws ParserConfigurationException, JAXBException, IOException, SAXException
    {
        String xml = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        return deserializeFromString(cls, xml, xsdName);
    }

    private static String removeXmlStringNoNamespace(String xmlString)
    {
        return xmlString.replaceAll("xsi:noNamespaceSchemaLocation=.*?(\"|\').*?(\"|\')", ""); /* remove xmlns declaration */
    }
}

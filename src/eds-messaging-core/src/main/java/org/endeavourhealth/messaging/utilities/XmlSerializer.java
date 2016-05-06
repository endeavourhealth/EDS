package org.endeavourhealth.messaging.utilities;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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

public abstract class XmlSerializer {

    public static <T> T deserializeFromString(Class cls, String xml, String xsdName) throws ParserConfigurationException, JAXBException, IOException, SAXException {

        //I can't figure out how to get the namespace set in the XML but overridden in Java.
        String newXml = removeXmlStringNoNamespace(xml);

        //parse XML string into DOM
        InputStream is = new ByteArrayInputStream(newXml.getBytes());
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = docBuilder.parse(is);

        return deserializeFromXmlDocument(cls, document, xsdName);
    }

    private static String removeXmlStringNoNamespace(String xmlString) {
        return xmlString.replaceAll("xsi:noNamespaceSchemaLocation=.*?(\"|\').*?(\"|\')", ""); /* remove xmlns declaration */
    }


    private static <T> T deserializeFromXmlDocument(Class cls, Document doc, String xsdName) throws ParserConfigurationException, JAXBException, IOException, SAXException {

        JAXBContext context = JAXBContext.newInstance(cls);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        //if a schema was provided, set it in the unmarshaller
        if (xsdName != null) {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL url = Resources.getResourceAsURLObject(xsdName);
            Schema schema = sf.newSchema(url);
            unmarshaller.setSchema(schema);
        }

        @SuppressWarnings("unchecked")
        JAXBElement<T> loader = unmarshaller.unmarshal(doc, cls);
        return loader.getValue();
    }

}

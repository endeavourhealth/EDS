package org.endeavourhealth.messaging.utilities;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;

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

    public static <T> T deserializeFromResource(Class cls, String xmlResourceName, String xsdName) throws ParserConfigurationException, JAXBException, IOException, SAXException {
        String xml = Resources.getResourceAsString(xmlResourceName);
        return deserializeFromString(cls, xml, xsdName);
    }

    private static String removeXmlStringNoNamespace(String xmlString) {
        return xmlString.replaceAll("xsi:noNamespaceSchemaLocation=.*?(\"|\').*?(\"|\')", ""); /* remove xmlns declaration */
    }

    public static <T> T deserializeFromFile(Class cls, Path file, String xsdName) throws Exception {
        String xml = FileHelper.loadStringFile(file);
        return deserializeFromString(cls, xml, xsdName);
    }

//    public static <T> T deserializeFromString(Class cls, String xml, String xsdName) throws ParserConfigurationException, JAXBException, IOException, SAXException {
//
//        //parse XML string into DOM
//        InputStream is = new ByteArrayInputStream(xml.getBytes());
//        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//        Document document = docBuilder.parse(is);
//
//        return deserializeFromXmlDocument(cls, document, xsdName);
//    }
//
//    public static <T> T deserializeFromResource(Class cls, String xmlResourceName, String xsdName) throws ParserConfigurationException, JAXBException, IOException, SAXException {
//
//        URL url = Resources.getResourceAsURLObject(xmlResourceName);
//        FileInputStream is = new FileInputStream(URLDecoder.decode( url.getFile(), "UTF-8" ));
//        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//        Document document = docBuilder.parse(is);
//
//        return deserializeFromXmlDocument(cls, document, xsdName);
//    }

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

    public static String serializeToString(JAXBElement element, String xsdName) {
        StringWriter sw = new StringWriter();
        Class cls = element.getValue().getClass();

        try {
            JAXBContext context = JAXBContext.newInstance(cls);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); //just makes output easier to read

            if (xsdName != null) {
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                URL url = Resources.getResourceAsURLObject(xsdName);
                Schema schema = sf.newSchema(url);
                marshaller.setSchema(schema);
            }

            marshaller.marshal(element, sw);

        } catch (JAXBException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        return sw.toString();
    }
}

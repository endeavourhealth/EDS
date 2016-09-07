package org.endeavourhealth.core.utility;

import org.endeavourhealth.core.cache.MarshallerPool;
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
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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


    public static <T> T deserializeFromFile(Class cls, String filename, String xsdName) throws IOException, JAXBException, ParserConfigurationException, SAXException {
        Path path = Paths.get(filename);
        byte[] encoded = Files.readAllBytes(path);
        String configXml = new String(encoded, "UTF-8");
        T configuration = deserializeFromString(cls, configXml, xsdName);
        return configuration;
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
        Schema schema = null;
        if (xsdName != null) {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL url = Resources.getResourceAsURLObject(xsdName);
            schema = sf.newSchema(url);
        }

        @SuppressWarnings("unchecked")
        JAXBElement<T> loader = MarshallerPool.getInstance().unmarshal(cls, doc, schema);
        return loader.getValue();
    }

    public static String serializeToString(JAXBElement element, String xsdName) {
        StringWriter sw = new StringWriter();
        Class cls = element.getValue().getClass();

        try {
            Schema schema = null;
            if (xsdName != null) {
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                URL url = Resources.getResourceAsURLObject(xsdName);
                schema = sf.newSchema(url);
            }

            MarshallerPool.getInstance().marshal(cls, element, schema, sw);

        } catch (JAXBException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        return sw.toString();
    }


}

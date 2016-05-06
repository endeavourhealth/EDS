package org.endeavourhealth.core.utilities;

import org.apache.commons.io.IOUtils;
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

	public static <T> T deserializeFromFile(Class cls, String filename, String xsdName) throws IOException, JAXBException, ParserConfigurationException, SAXException {
		Path path = Paths.get(filename);
		byte[] encoded = Files.readAllBytes(path);
		String configXml = new String(encoded, "UTF-8");
		T configuration = deserializeFromString(cls, configXml, xsdName);
		return configuration;
	}

	public static <T> T deserializeFromResource(Class cls, String resource, String xsdName) throws IOException, JAXBException, ParserConfigurationException, SAXException {
		InputStream inputStream =
				cls.getClassLoader().getResourceAsStream(resource);
		String configXml = IOUtils.toString(inputStream);
		T configuration = deserializeFromString(cls, configXml, xsdName);
		return configuration;
	}
}
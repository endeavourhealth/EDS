package org.endeavourhealth.sftpreader.configuration;

import org.endeavourhealth.messaging.utilities.XmlSerializer;
import org.endeavourhealth.sftpreader.configuration.model.SftpReaderConfiguration;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
	public static SftpReaderConfiguration readFromFile(String filename) throws IOException, JAXBException, ParserConfigurationException, SAXException {
		Path path = Paths.get(filename);
		byte[] encoded = Files.readAllBytes(path);
		String configXml = new String(encoded, "UTF-8");
		SftpReaderConfiguration configuration = XmlSerializer.deserializeFromString(SftpReaderConfiguration.class, configXml, "SftpReaderConfiguration.xsd");
		return configuration;
	}
}

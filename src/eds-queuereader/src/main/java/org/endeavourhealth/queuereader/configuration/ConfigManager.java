package org.endeavourhealth.queuereader.configuration;

import org.endeavourhealth.queuereader.configuration.model.QueueReaderConfiguration;
import org.endeavourhealth.messaging.utilities.XmlSerializer;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
	public static QueueReaderConfiguration readFromFile(String filename) throws IOException, JAXBException, ParserConfigurationException, SAXException {
		Path path = Paths.get(filename);
		byte[] encoded = Files.readAllBytes(path);
		String configXml = new String(encoded, "UTF-8");
		QueueReaderConfiguration configuration = XmlSerializer.deserializeFromString(QueueReaderConfiguration.class, configXml, "QueueReaderConfiguration.xsd");
		return configuration;
	}
}

package org.endeavourhealth.core.xml;

import org.endeavourhealth.core.utility.XmlSerializer;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.ObjectFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public abstract class EnterpriseSerializer {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static final String XSD = "EnterpriseSchema.xsd";

    public static EnterpriseData readFromXml(String xml) throws ParserConfigurationException, JAXBException, IOException, SAXException {
        return XmlSerializer.deserializeFromString(EnterpriseData.class, xml, XSD);
    }

    public static String writeToXml(EnterpriseData enterpriseData) {
        JAXBElement element = OBJECT_FACTORY.createEnterpriseData(enterpriseData);
        return XmlSerializer.serializeToString(element, XSD);
    }
}

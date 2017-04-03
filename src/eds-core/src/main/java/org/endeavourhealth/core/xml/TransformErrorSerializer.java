package org.endeavourhealth.core.xml;

import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.core.xml.transformError.ObjectFactory;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public abstract class TransformErrorSerializer {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static final String XSD = "TransformError.xsd";

    public static TransformError readFromXml(String xml) throws ParserConfigurationException, JAXBException, IOException, SAXException {
        return XmlSerializer.deserializeFromString(TransformError.class, xml, XSD);
    }

    public static String writeToXml(TransformError transformError) {
        JAXBElement element = OBJECT_FACTORY.createTransformError(transformError);
        return XmlSerializer.serializeToString(element, XSD);
    }

}

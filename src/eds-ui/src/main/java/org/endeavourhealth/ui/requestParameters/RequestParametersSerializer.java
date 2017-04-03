package org.endeavourhealth.ui.requestParameters;

import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.ui.requestParameters.models.ObjectFactory;
import org.endeavourhealth.ui.requestParameters.models.RequestParameters;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public abstract class RequestParametersSerializer {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static final String XSD = "RequestParameters.xsd";

    public static RequestParameters readFromXml(String xml) throws ParserConfigurationException, JAXBException, IOException, SAXException {
        return XmlSerializer.deserializeFromString(RequestParameters.class, xml, XSD);
    }

    public static String writeToXml(RequestParameters r) {
        JAXBElement element = OBJECT_FACTORY.createRequestParameters(r);
        return XmlSerializer.serializeToString(element, XSD);
    }



}

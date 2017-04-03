package org.endeavourhealth.core.xml;

import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.ObjectFactory;
import org.endeavourhealth.core.xml.QueryDocument.QueryDocument;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public abstract class QueryDocumentSerializer {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static final String XSD = "QueryDocument.xsd";

    public static LibraryItem readLibraryItemFromXml(String xml) throws ParserConfigurationException, JAXBException, IOException, SAXException {
        return XmlSerializer.deserializeFromString(LibraryItem.class, xml, XSD);
    }
    public static QueryDocument readQueryDocumentFromXml(String xml) throws ParserConfigurationException, JAXBException, IOException, SAXException {
        return XmlSerializer.deserializeFromString(QueryDocument.class, xml, XSD);
    }


    public static String writeToXml(QueryDocument q) {
        if (q.getFolder().isEmpty()
                && q.getLibraryItem().size() == 1)
        {

            LibraryItem libraryItem = q.getLibraryItem().get(0);
            JAXBElement element = OBJECT_FACTORY.createLibraryItem(libraryItem);
            return XmlSerializer.serializeToString(element, XSD);
        }
        else
        {
            JAXBElement element = OBJECT_FACTORY.createQueryDocument(q);
            return XmlSerializer.serializeToString(element, XSD);
        }
    }



}

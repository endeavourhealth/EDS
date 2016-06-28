
package org.endeavourhealth.ui.requestParameters.models;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.ui.requestParameters.models package.
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _RequestParameters_QNAME = new QName("", "requestParameters");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.ui.requestParameters.models
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RequestParameters }
     * 
     */
    public RequestParameters createRequestParameters() {
        return new RequestParameters();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestParameters }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "requestParameters")
    public JAXBElement<RequestParameters> createRequestParameters(RequestParameters value) {
        return new JAXBElement<RequestParameters>(_RequestParameters_QNAME, RequestParameters.class, null, value);
    }

}

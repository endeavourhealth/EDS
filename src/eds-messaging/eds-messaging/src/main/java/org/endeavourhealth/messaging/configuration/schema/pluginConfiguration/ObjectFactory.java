
package org.endeavourhealth.messaging.configuration.schema.pluginConfiguration;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavour.resolution.configuration.schema.pluginConfiguration package.
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

    private final static QName _Protocol_QNAME = new QName("", "Protocol");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavour.resolution.configuration.schema.pluginConfiguration
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Receivers }
     * 
     */
    public Receivers createEndpoints() {
        return new Receivers();
    }

    /**
     * Create an instance of {@link Receiver }
     * 
     */
    public Receiver createEndpoint() {
        return new Receiver();
    }

    /**
     * Create an instance of {@link Messages }
     * 
     */
    public Messages createMessages() {
        return new Messages();
    }

    /**
     * Create an instance of {@link Message }
     * 
     */
    public Message createMessage() {
        return new Message();
    }

    /**
     * Create an instance of {@link Listener }
     * 
     */
    public Listener createListener() {
        return new Listener();
    }

    /**
     * Create an instance of {@link PluginConfiguration }
     * 
     */
    public PluginConfiguration createPluginConfiguration() {
        return new PluginConfiguration();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Protocol")
    public JAXBElement<String> createProtocol(String value) {
        return new JAXBElement<String>(_Protocol_QNAME, String.class, null, value);
    }

}

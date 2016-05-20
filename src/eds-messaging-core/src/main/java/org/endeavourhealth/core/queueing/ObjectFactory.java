
package org.endeavourhealth.core.queueing;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.core.queueing package. 
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

    private final static QName _Config_QNAME = new QName("", "Config");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.core.queueing
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RMQConfig }
     * 
     */
    public RMQConfig createRMQConfig() {
        return new RMQConfig();
    }

    /**
     * Create an instance of {@link ExchangeProperties }
     * 
     */
    public ExchangeProperties createExchangeProperties() {
        return new ExchangeProperties();
    }

    /**
     * Create an instance of {@link ComponentConfig }
     * 
     */
    public ComponentConfig createComponentConfig() {
        return new ComponentConfig();
    }

    /**
     * Create an instance of {@link RMQExchange }
     * 
     */
    public RMQExchange createRMQExchange() {
        return new RMQExchange();
    }

    /**
     * Create an instance of {@link ExchangeProperty }
     * 
     */
    public ExchangeProperty createExchangeProperty() {
        return new ExchangeProperty();
    }

    /**
     * Create an instance of {@link Credentials }
     * 
     */
    public Credentials createCredentials() {
        return new Credentials();
    }

    /**
     * Create an instance of {@link RMQQueue }
     * 
     */
    public RMQQueue createRMQQueue() {
        return new RMQQueue();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RMQConfig }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Config")
    public JAXBElement<RMQConfig> createConfig(RMQConfig value) {
        return new JAXBElement<RMQConfig>(_Config_QNAME, RMQConfig.class, null, value);
    }

}

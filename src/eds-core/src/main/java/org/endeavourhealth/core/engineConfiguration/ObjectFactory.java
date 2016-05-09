
package org.endeavourhealth.core.engineConfiguration;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.core.engineConfiguration package. 
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

    private final static QName _EngineConfiguration_QNAME = new QName("", "engineConfiguration");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.core.engineConfiguration
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EngineConfiguration }
     * 
     */
    public EngineConfiguration createEngineConfiguration() {
        return new EngineConfiguration();
    }

    /**
     * Create an instance of {@link Cassandra }
     * 
     */
    public Cassandra createCassandra() {
        return new Cassandra();
    }

    /**
     * Create an instance of {@link Audit }
     * 
     */
    public Audit createAudit() {
        return new Audit();
    }

    /**
     * Create an instance of {@link Logging }
     * 
     */
    public Logging createLogging() {
        return new Logging();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EngineConfiguration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "engineConfiguration")
    public JAXBElement<EngineConfiguration> createEngineConfiguration(EngineConfiguration value) {
        return new JAXBElement<EngineConfiguration>(_EngineConfiguration_QNAME, EngineConfiguration.class, null, value);
    }

}

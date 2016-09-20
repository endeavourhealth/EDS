
package org.endeavourhealth.coreui.framework.config.models;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.endeavourhealth.ui.framework.config.models package.
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

    private final static QName _Config_QNAME = new QName("", "config");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.ui.framework.config.models
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Config }
     *
     */
    public Config createConfig() {
        return new Config();
    }

    /**
     * Create an instance of {@link Template }
     *
     */
    public Template createTemplate() {
        return new Template();
    }

    /**
     * Create an instance of {@link Database }
     *
     */
    public Database createDatabase() {
        return new Database();
    }

    /**
     * Create an instance of {@link MessagingQueue }
     *
     */
    public MessagingQueue createMessagingQueue() {
        return new MessagingQueue();
    }

    /**
     * Create an instance of {@link WebServer }
     *
     */
    public WebServer createWebServer() {
        return new WebServer();
    }

    /**
     * Create an instance of {@link Email }
     *
     */
    public Email createEmail() {
        return new Email();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Config }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "config")
    public JAXBElement<Config> createConfig(Config value) {
        return new JAXBElement<Config>(_Config_QNAME, Config.class, null, value);
    }

}


package org.endeavourhealth.messaging.configuration.schema.pluginContracts;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavour.resolution.configuration.schema.pluginContracts package.
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavour.resolution.configuration.schema.pluginContracts
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PluginContracts }
     * 
     */
    public PluginContracts createPluginContracts() {
        return new PluginContracts();
    }

    /**
     * Create an instance of {@link Contracts }
     * 
     */
    public Contracts createContracts() {
        return new Contracts();
    }

    /**
     * Create an instance of {@link Contract }
     * 
     */
    public Contract createContract() {
        return new Contract();
    }

}

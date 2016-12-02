
package org.endeavourhealth.hl7receiver.model.xml;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.sftpreader.model.xml package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.sftpreader.model.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Hl7ReceiverConfiguration }
     * 
     */
    public Hl7ReceiverConfiguration createHl7ReceiverConfiguration() {
        return new Hl7ReceiverConfiguration();
    }

    /**
     * Create an instance of {@link Hl7ReceiverConfiguration.DatabaseConnections }
     * 
     */
    public Hl7ReceiverConfiguration.DatabaseConnections createHl7ReceiverConfigurationDatabaseConnections() {
        return new Hl7ReceiverConfiguration.DatabaseConnections();
    }

    /**
     * Create an instance of {@link DatabaseConnection }
     * 
     */
    public DatabaseConnection createDatabaseConnection() {
        return new DatabaseConnection();
    }

}

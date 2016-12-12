
package org.endeavourhealth.utilities.configuration.model;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.utilities.configuration.model package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.utilities.configuration.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LocalConfiguration }
     * 
     */
    public LocalConfiguration createLocalConfiguration() {
        return new LocalConfiguration();
    }

    /**
     * Create an instance of {@link LocalConfiguration.DatabaseConnections }
     * 
     */
    public LocalConfiguration.DatabaseConnections createLocalConfigurationDatabaseConnections() {
        return new LocalConfiguration.DatabaseConnections();
    }

    /**
     * Create an instance of {@link DatabaseConnection }
     * 
     */
    public DatabaseConnection createDatabaseConnection() {
        return new DatabaseConnection();
    }

}

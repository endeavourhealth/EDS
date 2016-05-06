
package org.endeavourhealth.core.configuration;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.core.configuration package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.core.configuration
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PostMessageToQueueConfig }
     * 
     */
    public PostMessageToQueueConfig createPostMessageToQueueConfig() {
        return new PostMessageToQueueConfig();
    }

    /**
     * Create an instance of {@link ValidateMessageTypeConfig }
     * 
     */
    public ValidateMessageTypeConfig createValidateMessageTypeConfig() {
        return new ValidateMessageTypeConfig();
    }

    /**
     * Create an instance of {@link ValidateSenderConfig }
     * 
     */
    public ValidateSenderConfig createValidateSenderConfig() {
        return new ValidateSenderConfig();
    }

    /**
     * Create an instance of {@link Credentials }
     * 
     */
    public Credentials createCredentials() {
        return new Credentials();
    }

    /**
     * Create an instance of {@link Pipeline }
     * 
     */
    public Pipeline createPipeline() {
        return new Pipeline();
    }

}

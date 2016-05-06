
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
     * Create an instance of {@link PostMessageToQueue }
     * 
     */
    public PostMessageToQueue createPostMessageToQueue() {
        return new PostMessageToQueue();
    }

    /**
     * Create an instance of {@link ValidateSender }
     * 
     */
    public ValidateSender createValidateSender() {
        return new ValidateSender();
    }

    /**
     * Create an instance of {@link Credentials }
     * 
     */
    public Credentials createCredentials() {
        return new Credentials();
    }

    /**
     * Create an instance of {@link ValidateMessageType }
     * 
     */
    public ValidateMessageType createValidateMessageType() {
        return new ValidateMessageType();
    }

    /**
     * Create an instance of {@link Pipeline }
     * 
     */
    public Pipeline createPipeline() {
        return new Pipeline();
    }

}

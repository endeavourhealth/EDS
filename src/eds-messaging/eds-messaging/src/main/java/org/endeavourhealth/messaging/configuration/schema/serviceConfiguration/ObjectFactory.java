
package org.endeavourhealth.messaging.configuration.schema.serviceConfiguration;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.messaging.configuration.schema.serviceConfiguration package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.messaging.configuration.schema.serviceConfiguration
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RabbitListener }
     * 
     */
    public RabbitListener createRabbitListener() {
        return new RabbitListener();
    }

    /**
     * Create an instance of {@link RabbitListener.Nodes }
     * 
     */
    public RabbitListener.Nodes createRabbitListenerNodes() {
        return new RabbitListener.Nodes();
    }

    /**
     * Create an instance of {@link Service }
     * 
     */
    public Service createService() {
        return new Service();
    }

    /**
     * Create an instance of {@link ServiceConfiguration }
     * 
     */
    public ServiceConfiguration createServiceConfiguration() {
        return new ServiceConfiguration();
    }

    /**
     * Create an instance of {@link HttpReceiver }
     * 
     */
    public HttpReceiver createHttpReceiver() {
        return new HttpReceiver();
    }

    /**
     * Create an instance of {@link HttpListener }
     * 
     */
    public HttpListener createHttpListener() {
        return new HttpListener();
    }

    /**
     * Create an instance of {@link RabbitReceiver }
     * 
     */
    public RabbitReceiver createRabbitReceiver() {
        return new RabbitReceiver();
    }

    /**
     * Create an instance of {@link MessageType }
     * 
     */
    public MessageType createMessageType() {
        return new MessageType();
    }

    /**
     * Create an instance of {@link RabbitListener.Credentials }
     * 
     */
    public RabbitListener.Credentials createRabbitListenerCredentials() {
        return new RabbitListener.Credentials();
    }

    /**
     * Create an instance of {@link RabbitListener.Nodes.NodeHostName }
     * 
     */
    public RabbitListener.Nodes.NodeHostName createRabbitListenerNodesNodeHostName() {
        return new RabbitListener.Nodes.NodeHostName();
    }

    /**
     * Create an instance of {@link Service.Listeners }
     * 
     */
    public Service.Listeners createServiceListeners() {
        return new Service.Listeners();
    }

    /**
     * Create an instance of {@link Service.MessageTypes }
     * 
     */
    public Service.MessageTypes createServiceMessageTypes() {
        return new Service.MessageTypes();
    }

}

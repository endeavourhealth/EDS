
package org.endeavourhealth.messaging.configuration.schema.pluginConfiguration;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.messaging.configuration.schema.pluginConfiguration package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.messaging.configuration.schema.pluginConfiguration
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Route }
     * 
     */
    public Route createRoute() {
        return new Route();
    }

    /**
     * Create an instance of {@link ReceivePort }
     * 
     */
    public ReceivePort createReceivePort() {
        return new ReceivePort();
    }

    /**
     * Create an instance of {@link Service }
     * 
     */
    public Service createService() {
        return new Service();
    }

    /**
     * Create an instance of {@link PluginConfiguration }
     * 
     */
    public PluginConfiguration createRouteConfiguration() {
        return new PluginConfiguration();
    }

    /**
     * Create an instance of {@link SendPortType }
     * 
     */
    public SendPortType createSendPortType() {
        return new SendPortType();
    }

    /**
     * Create an instance of {@link MessageType }
     * 
     */
    public MessageType createMessageType() {
        return new MessageType();
    }

    /**
     * Create an instance of {@link Route.MessageTypes }
     * 
     */
    public Route.MessageTypes createRouteMessageTypes() {
        return new Route.MessageTypes();
    }

    /**
     * Create an instance of {@link ReceivePort.Http }
     * 
     */
    public ReceivePort.Http createReceivePortHttp() {
        return new ReceivePort.Http();
    }

    /**
     * Create an instance of {@link ReceivePort.Sftp }
     * 
     */
    public ReceivePort.Sftp createReceivePortSftp() {
        return new ReceivePort.Sftp();
    }

    /**
     * Create an instance of {@link ReceivePort.RabbitMQ }
     * 
     */
    public ReceivePort.RabbitMQ createReceivePortRabbitMQ() {
        return new ReceivePort.RabbitMQ();
    }

    /**
     * Create an instance of {@link Service.ReceivePorts }
     * 
     */
    public Service.ReceivePorts createServiceReceivePorts() {
        return new Service.ReceivePorts();
    }

    /**
     * Create an instance of {@link Service.MessageTypes }
     * 
     */
    public Service.MessageTypes createServiceMessageTypes() {
        return new Service.MessageTypes();
    }

    /**
     * Create an instance of {@link Service.SendPorts }
     * 
     */
    public Service.SendPorts createServiceSendPorts() {
        return new Service.SendPorts();
    }

    /**
     * Create an instance of {@link Service.Routes }
     * 
     */
    public Service.Routes createServiceRoutes() {
        return new Service.Routes();
    }

}

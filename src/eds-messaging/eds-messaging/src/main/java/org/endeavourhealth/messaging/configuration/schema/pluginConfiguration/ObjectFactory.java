
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
     * Create an instance of {@link PluginConfiguration }
     * 
     */
    public PluginConfiguration createPluginConfiguration() {
        return new PluginConfiguration();
    }

    /**
     * Create an instance of {@link ReceivePortType }
     * 
     */
    public ReceivePortType createReceivePortType() {
        return new ReceivePortType();
    }

    /**
     * Create an instance of {@link ServiceType }
     * 
     */
    public ServiceType createServiceType() {
        return new ServiceType();
    }

    /**
     * Create an instance of {@link RouteType }
     * 
     */
    public RouteType createRouteType() {
        return new RouteType();
    }

    /**
     * Create an instance of {@link PluginConfiguration.Services }
     * 
     */
    public PluginConfiguration.Services createPluginConfigurationServices() {
        return new PluginConfiguration.Services();
    }

    /**
     * Create an instance of {@link SendPortType }
     * 
     */
    public SendPortType createSendPortType() {
        return new SendPortType();
    }

    /**
     * Create an instance of {@link MessageTypeType }
     * 
     */
    public MessageTypeType createMessageTypeType() {
        return new MessageTypeType();
    }

    /**
     * Create an instance of {@link ReceivePortType.Http }
     * 
     */
    public ReceivePortType.Http createReceivePortTypeHttp() {
        return new ReceivePortType.Http();
    }

    /**
     * Create an instance of {@link ReceivePortType.Sftp }
     * 
     */
    public ReceivePortType.Sftp createReceivePortTypeSftp() {
        return new ReceivePortType.Sftp();
    }

    /**
     * Create an instance of {@link ReceivePortType.RabbitMQ }
     * 
     */
    public ReceivePortType.RabbitMQ createReceivePortTypeRabbitMQ() {
        return new ReceivePortType.RabbitMQ();
    }

    /**
     * Create an instance of {@link ServiceType.ReceivePorts }
     * 
     */
    public ServiceType.ReceivePorts createServiceTypeReceivePorts() {
        return new ServiceType.ReceivePorts();
    }

    /**
     * Create an instance of {@link ServiceType.MessageTypes }
     * 
     */
    public ServiceType.MessageTypes createServiceTypeMessageTypes() {
        return new ServiceType.MessageTypes();
    }

    /**
     * Create an instance of {@link ServiceType.SendPorts }
     * 
     */
    public ServiceType.SendPorts createServiceTypeSendPorts() {
        return new ServiceType.SendPorts();
    }

    /**
     * Create an instance of {@link ServiceType.Routes }
     * 
     */
    public ServiceType.Routes createServiceTypeRoutes() {
        return new ServiceType.Routes();
    }

    /**
     * Create an instance of {@link RouteType.MessageTypes }
     * 
     */
    public RouteType.MessageTypes createRouteTypeMessageTypes() {
        return new RouteType.MessageTypes();
    }

}


package org.endeavourhealth.messaging.configuration.schema.pluginConfiguration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ServiceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ReceivePorts">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *                   &lt;element name="ReceivePort" type="{}ReceivePortType"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="MessageTypes">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *                   &lt;element name="MessageType" type="{}MessageTypeType"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="SendPorts">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *                   &lt;element name="SendPort" type="{}SendPortType"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Routes">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Route" type="{}RouteType"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceType", propOrder = {
    "id",
    "receivePorts",
    "messageTypes",
    "sendPorts",
    "routes"
})
public class ServiceType {

    @XmlElement(name = "Id", required = true)
    protected String id;
    @XmlElement(name = "ReceivePorts", required = true)
    protected ServiceType.ReceivePorts receivePorts;
    @XmlElement(name = "MessageTypes", required = true)
    protected ServiceType.MessageTypes messageTypes;
    @XmlElement(name = "SendPorts", required = true)
    protected ServiceType.SendPorts sendPorts;
    @XmlElement(name = "Routes", required = true)
    protected ServiceType.Routes routes;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the receivePorts property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceType.ReceivePorts }
     *     
     */
    public ServiceType.ReceivePorts getReceivePorts() {
        return receivePorts;
    }

    /**
     * Sets the value of the receivePorts property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceType.ReceivePorts }
     *     
     */
    public void setReceivePorts(ServiceType.ReceivePorts value) {
        this.receivePorts = value;
    }

    /**
     * Gets the value of the messageTypes property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceType.MessageTypes }
     *     
     */
    public ServiceType.MessageTypes getMessageTypes() {
        return messageTypes;
    }

    /**
     * Sets the value of the messageTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceType.MessageTypes }
     *     
     */
    public void setMessageTypes(ServiceType.MessageTypes value) {
        this.messageTypes = value;
    }

    /**
     * Gets the value of the sendPorts property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceType.SendPorts }
     *     
     */
    public ServiceType.SendPorts getSendPorts() {
        return sendPorts;
    }

    /**
     * Sets the value of the sendPorts property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceType.SendPorts }
     *     
     */
    public void setSendPorts(ServiceType.SendPorts value) {
        this.sendPorts = value;
    }

    /**
     * Gets the value of the routes property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceType.Routes }
     *     
     */
    public ServiceType.Routes getRoutes() {
        return routes;
    }

    /**
     * Sets the value of the routes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceType.Routes }
     *     
     */
    public void setRoutes(ServiceType.Routes value) {
        this.routes = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
     *         &lt;element name="MessageType" type="{}MessageTypeType"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "messageType"
    })
    public static class MessageTypes {

        @XmlElement(name = "MessageType")
        protected List<MessageTypeType> messageType;

        /**
         * Gets the value of the messageType property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the messageType property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMessageType().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MessageTypeType }
         * 
         * 
         */
        public List<MessageTypeType> getMessageType() {
            if (messageType == null) {
                messageType = new ArrayList<MessageTypeType>();
            }
            return this.messageType;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
     *         &lt;element name="ReceivePort" type="{}ReceivePortType"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "receivePort"
    })
    public static class ReceivePorts {

        @XmlElement(name = "ReceivePort")
        protected List<ReceivePortType> receivePort;

        /**
         * Gets the value of the receivePort property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the receivePort property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getReceivePort().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ReceivePortType }
         * 
         * 
         */
        public List<ReceivePortType> getReceivePort() {
            if (receivePort == null) {
                receivePort = new ArrayList<ReceivePortType>();
            }
            return this.receivePort;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="Route" type="{}RouteType"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "route"
    })
    public static class Routes {

        @XmlElement(name = "Route", required = true)
        protected RouteType route;

        /**
         * Gets the value of the route property.
         * 
         * @return
         *     possible object is
         *     {@link RouteType }
         *     
         */
        public RouteType getRoute() {
            return route;
        }

        /**
         * Sets the value of the route property.
         * 
         * @param value
         *     allowed object is
         *     {@link RouteType }
         *     
         */
        public void setRoute(RouteType value) {
            this.route = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
     *         &lt;element name="SendPort" type="{}SendPortType"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "sendPort"
    })
    public static class SendPorts {

        @XmlElement(name = "SendPort")
        protected List<SendPortType> sendPort;

        /**
         * Gets the value of the sendPort property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the sendPort property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSendPort().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SendPortType }
         * 
         * 
         */
        public List<SendPortType> getSendPort() {
            if (sendPort == null) {
                sendPort = new ArrayList<SendPortType>();
            }
            return this.sendPort;
        }

    }

}

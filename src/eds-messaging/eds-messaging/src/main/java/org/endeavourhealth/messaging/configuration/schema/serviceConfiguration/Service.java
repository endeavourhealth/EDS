
package org.endeavourhealth.messaging.configuration.schema.serviceConfiguration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Service complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Service">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{}nonEmptyString"/>
 *         &lt;element name="Listeners">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="HttpListener" type="{}HttpListener" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="RabbitListener" type="{}RabbitListener" maxOccurs="unbounded" minOccurs="0"/>
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
 *                   &lt;element name="MessageType" type="{}MessageType"/>
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
@XmlType(name = "Service", propOrder = {
    "id",
    "listeners",
    "messageTypes"
})
public class Service {

    @XmlElement(name = "Id", required = true)
    protected String id;
    @XmlElement(name = "Listeners", required = true)
    protected Service.Listeners listeners;
    @XmlElement(name = "MessageTypes", required = true)
    protected Service.MessageTypes messageTypes;

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
     * Gets the value of the listeners property.
     * 
     * @return
     *     possible object is
     *     {@link Service.Listeners }
     *     
     */
    public Service.Listeners getListeners() {
        return listeners;
    }

    /**
     * Sets the value of the listeners property.
     * 
     * @param value
     *     allowed object is
     *     {@link Service.Listeners }
     *     
     */
    public void setListeners(Service.Listeners value) {
        this.listeners = value;
    }

    /**
     * Gets the value of the messageTypes property.
     * 
     * @return
     *     possible object is
     *     {@link Service.MessageTypes }
     *     
     */
    public Service.MessageTypes getMessageTypes() {
        return messageTypes;
    }

    /**
     * Sets the value of the messageTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Service.MessageTypes }
     *     
     */
    public void setMessageTypes(Service.MessageTypes value) {
        this.messageTypes = value;
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
     *         &lt;element name="HttpListener" type="{}HttpListener" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="RabbitListener" type="{}RabbitListener" maxOccurs="unbounded" minOccurs="0"/>
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
        "httpListener",
        "rabbitListener"
    })
    public static class Listeners {

        @XmlElement(name = "HttpListener")
        protected List<HttpListener> httpListener;
        @XmlElement(name = "RabbitListener")
        protected List<RabbitListener> rabbitListener;

        /**
         * Gets the value of the httpListener property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the httpListener property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getHttpListener().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link HttpListener }
         * 
         * 
         */
        public List<HttpListener> getHttpListener() {
            if (httpListener == null) {
                httpListener = new ArrayList<HttpListener>();
            }
            return this.httpListener;
        }

        /**
         * Gets the value of the rabbitListener property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the rabbitListener property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRabbitListener().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link RabbitListener }
         * 
         * 
         */
        public List<RabbitListener> getRabbitListener() {
            if (rabbitListener == null) {
                rabbitListener = new ArrayList<RabbitListener>();
            }
            return this.rabbitListener;
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
     *         &lt;element name="MessageType" type="{}MessageType"/>
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
        protected List<MessageType> messageType;

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
         * {@link MessageType }
         * 
         * 
         */
        public List<MessageType> getMessageType() {
            if (messageType == null) {
                messageType = new ArrayList<MessageType>();
            }
            return this.messageType;
        }

    }

}


package org.endeavourhealth.messaging.configuration.schema.pluginConfiguration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Route complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Route">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="SendPortId" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="ReceivePortId" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="MessageTypes">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *                   &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
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
@XmlType(name = "Route", propOrder = {
    "id",
    "sendPortId",
    "receivePortId",
    "messageTypes"
})
public class Route {

    @XmlElement(name = "Id", required = true)
    protected Object id;
    @XmlElement(name = "SendPortId", required = true)
    protected Object sendPortId;
    @XmlElement(name = "ReceivePortId", required = true)
    protected Object receivePortId;
    @XmlElement(name = "MessageTypes", required = true)
    protected Route.MessageTypes messageTypes;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setId(Object value) {
        this.id = value;
    }

    /**
     * Gets the value of the sendPortId property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getSendPortId() {
        return sendPortId;
    }

    /**
     * Sets the value of the sendPortId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setSendPortId(Object value) {
        this.sendPortId = value;
    }

    /**
     * Gets the value of the receivePortId property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getReceivePortId() {
        return receivePortId;
    }

    /**
     * Sets the value of the receivePortId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setReceivePortId(Object value) {
        this.receivePortId = value;
    }

    /**
     * Gets the value of the messageTypes property.
     * 
     * @return
     *     possible object is
     *     {@link Route.MessageTypes }
     *     
     */
    public Route.MessageTypes getMessageTypes() {
        return messageTypes;
    }

    /**
     * Sets the value of the messageTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Route.MessageTypes }
     *     
     */
    public void setMessageTypes(Route.MessageTypes value) {
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
     *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
     *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
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
        "id"
    })
    public static class MessageTypes {

        @XmlElement(name = "Id")
        protected List<Object> id;

        /**
         * Gets the value of the id property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the id property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getId().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Object }
         * 
         * 
         */
        public List<Object> getId() {
            if (id == null) {
                id = new ArrayList<Object>();
            }
            return this.id;
        }

    }

}


package org.endeavourhealth.queuereader.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="Credentials">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Username" type="{}nonEmptyString" minOccurs="0"/>
 *                   &lt;element name="Password" type="{}nonEmptyString" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Nodes" type="{}nonEmptyString" minOccurs="0"/>
 *         &lt;element name="Exchange" type="{}nonEmptyString" minOccurs="0"/>
 *         &lt;element name="Queue" type="{}nonEmptyString" minOccurs="0"/>
 *         &lt;element name="RoutingKeys" type="{}nonEmptyString" minOccurs="0"/>
 *         &lt;element name="MessagePipelineClass" type="{}nonEmptyString" minOccurs="0"/>
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
    "credentials",
    "nodes",
    "exchange",
    "queue",
    "routingKeys",
    "messagePipelineClass"
})
@XmlRootElement(name = "QueueReaderConfiguration")
public class QueueReaderConfiguration {

    @XmlElement(name = "Credentials", required = true)
    protected QueueReaderConfiguration.Credentials credentials;
    @XmlElement(name = "Nodes")
    protected String nodes;
    @XmlElement(name = "Exchange")
    protected String exchange;
    @XmlElement(name = "Queue")
    protected String queue;
    @XmlElement(name = "RoutingKeys")
    protected String routingKeys;
    @XmlElement(name = "MessagePipelineClass")
    protected String messagePipelineClass;

    /**
     * Gets the value of the credentials property.
     * 
     * @return
     *     possible object is
     *     {@link QueueReaderConfiguration.Credentials }
     *     
     */
    public QueueReaderConfiguration.Credentials getCredentials() {
        return credentials;
    }

    /**
     * Sets the value of the credentials property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueueReaderConfiguration.Credentials }
     *     
     */
    public void setCredentials(QueueReaderConfiguration.Credentials value) {
        this.credentials = value;
    }

    /**
     * Gets the value of the nodes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNodes() {
        return nodes;
    }

    /**
     * Sets the value of the nodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNodes(String value) {
        this.nodes = value;
    }

    /**
     * Gets the value of the exchange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExchange() {
        return exchange;
    }

    /**
     * Sets the value of the exchange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExchange(String value) {
        this.exchange = value;
    }

    /**
     * Gets the value of the queue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueue() {
        return queue;
    }

    /**
     * Sets the value of the queue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueue(String value) {
        this.queue = value;
    }

    /**
     * Gets the value of the routingKeys property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoutingKeys() {
        return routingKeys;
    }

    /**
     * Sets the value of the routingKeys property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoutingKeys(String value) {
        this.routingKeys = value;
    }

    /**
     * Gets the value of the messagePipelineClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessagePipelineClass() {
        return messagePipelineClass;
    }

    /**
     * Sets the value of the messagePipelineClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessagePipelineClass(String value) {
        this.messagePipelineClass = value;
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
     *         &lt;element name="Username" type="{}nonEmptyString" minOccurs="0"/>
     *         &lt;element name="Password" type="{}nonEmptyString" minOccurs="0"/>
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
        "username",
        "password"
    })
    public static class Credentials {

        @XmlElement(name = "Username")
        protected String username;
        @XmlElement(name = "Password")
        protected String password;

        /**
         * Gets the value of the username property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUsername() {
            return username;
        }

        /**
         * Sets the value of the username property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUsername(String value) {
            this.username = value;
        }

        /**
         * Gets the value of the password property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPassword() {
            return password;
        }

        /**
         * Sets the value of the password property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPassword(String value) {
            this.password = value;
        }

    }

}

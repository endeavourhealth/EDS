
package org.endeavourhealth.messaging.configuration.schema.serviceConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RabbitReceiver complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RabbitReceiver">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Queue" type="{}nonEmptyString"/>
 *         &lt;element name="Exchange" type="{}nonEmptyString" minOccurs="0"/>
 *         &lt;element name="RoutingKey" type="{}nonEmptyString" minOccurs="0"/>
 *         &lt;element name="ReceiverClass" type="{}nonEmptyString"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RabbitReceiver", propOrder = {
    "queue",
    "exchange",
    "routingKey",
    "receiverClass"
})
public class RabbitReceiver {

    @XmlElement(name = "Queue", required = true)
    protected String queue;
    @XmlElement(name = "Exchange")
    protected String exchange;
    @XmlElement(name = "RoutingKey")
    protected String routingKey;
    @XmlElement(name = "ReceiverClass", required = true)
    protected String receiverClass;

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
     * Gets the value of the routingKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoutingKey() {
        return routingKey;
    }

    /**
     * Sets the value of the routingKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoutingKey(String value) {
        this.routingKey = value;
    }

    /**
     * Gets the value of the receiverClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiverClass() {
        return receiverClass;
    }

    /**
     * Sets the value of the receiverClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiverClass(String value) {
        this.receiverClass = value;
    }

}


package org.endeavourhealth.core.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ComponentConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ComponentConfig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ExchangeProperties" type="{}ExchangeProperties" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComponentConfig", propOrder = {
    "exchangeProperties"
})
@XmlSeeAlso({
    ReadMessageEnvelopeConfig.class,
    RunDataDistributionProtocolsConfig.class,
    ReturnResponseAcknowledgementConfig.class,
    ValidateMessageTypeConfig.class,
    PostMessageToLogConfig.class,
    PostMessageToQueueConfig.class,
    MessageTransformConfig.class,
    PostToSenderConfig.class,
    PostToSubscriberWebServiceConfig.class,
    PostToEventLogConfig.class,
    ValidateSenderConfig.class
})
public class ComponentConfig {

    @XmlElement(name = "ExchangeProperties")
    protected ExchangeProperties exchangeProperties;

    /**
     * Gets the value of the exchangeProperties property.
     * 
     * @return
     *     possible object is
     *     {@link ExchangeProperties }
     *     
     */
    public ExchangeProperties getExchangeProperties() {
        return exchangeProperties;
    }

    /**
     * Sets the value of the exchangeProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExchangeProperties }
     *     
     */
    public void setExchangeProperties(ExchangeProperties value) {
        this.exchangeProperties = value;
    }

}

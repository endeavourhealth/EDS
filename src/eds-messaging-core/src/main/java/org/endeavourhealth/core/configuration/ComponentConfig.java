
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
 *         &lt;element name="ExchangeHeaders" type="{}ExchangeHeaders" minOccurs="0"/>
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
    "exchangeHeaders"
})
@XmlSeeAlso({
    ForEachConfig.class,
    PostToRestConfig.class,
    RunDataDistributionProtocolsConfig.class,
    ReturnResponseAcknowledgementConfig.class,
    ValidateMessageTypeConfig.class,
    PostToEventStoreConfig.class,
    LoadSenderConfigurationConfig.class,
    MessageTransformInboundConfig.class,
    PGPDecryptConfig.class,
    PostMessageToLogConfig.class,
    DetermineRelevantProtocolIdsConfig.class,
    EnvelopMessageConfig.class,
    MessageTransformOutboundConfig.class,
    PostMessageToExchangeConfig.class,
    PostToSubscriberWebServiceConfig.class,
    OpenEnvelopeConfig.class,
    ValidateSenderConfig.class
})
public class ComponentConfig {

    @XmlElement(name = "ExchangeHeaders")
    protected ExchangeHeaders exchangeHeaders;

    /**
     * Gets the value of the exchangeHeaders property.
     * 
     * @return
     *     possible object is
     *     {@link ExchangeHeaders }
     *     
     */
    public ExchangeHeaders getExchangeHeaders() {
        return exchangeHeaders;
    }

    /**
     * Sets the value of the exchangeHeaders property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExchangeHeaders }
     *     
     */
    public void setExchangeHeaders(ExchangeHeaders value) {
        this.exchangeHeaders = value;
    }

}

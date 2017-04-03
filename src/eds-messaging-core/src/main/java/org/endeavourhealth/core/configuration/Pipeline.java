
package org.endeavourhealth.core.configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Pipeline complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Pipeline">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element name="ForeEach" type="{}ForEachConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="OpenEnvelope" type="{}OpenEnvelopeConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="EnvelopMessage" type="{}EnvelopMessageConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="LoadSenderConfiguration" type="{}LoadSenderConfigurationConfig" minOccurs="0"/>
 *           &lt;element name="ValidateSender" type="{}ValidateSenderConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="ValidateMessageType" type="{}ValidateMessageTypeConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PostMessageToLog" type="{}PostMessageToLogConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PostMessageToExchange" type="{}PostMessageToExchangeConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="ReturnResponseAcknowledgement" type="{}ReturnResponseAcknowledgementConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="MessageTransformInbound" type="{}MessageTransformInboundConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="MessageTransformOutbound" type="{}MessageTransformOutboundConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PostToEventStore" type="{}PostToEventStoreConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="DetermineRelevantProtocolIds" type="{}DetermineRelevantProtocolIdsConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="RunDataDistributionProtocols" type="{}RunDataDistributionProtocolsConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PostToSubscriberWebService" type="{}PostToSubscriberWebServiceConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PostToRest" type="{}PostToRestConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PGPDecrypt" type="{}PGPDecryptConfig" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Pipeline", propOrder = {
    "pipelineComponents"
})
public class Pipeline {

    @XmlElements({
        @XmlElement(name = "ForeEach", type = ForEachConfig.class),
        @XmlElement(name = "OpenEnvelope", type = OpenEnvelopeConfig.class),
        @XmlElement(name = "EnvelopMessage", type = EnvelopMessageConfig.class),
        @XmlElement(name = "LoadSenderConfiguration", type = LoadSenderConfigurationConfig.class),
        @XmlElement(name = "ValidateSender", type = ValidateSenderConfig.class),
        @XmlElement(name = "ValidateMessageType", type = ValidateMessageTypeConfig.class),
        @XmlElement(name = "PostMessageToLog", type = PostMessageToLogConfig.class),
        @XmlElement(name = "PostMessageToExchange", type = PostMessageToExchangeConfig.class),
        @XmlElement(name = "ReturnResponseAcknowledgement", type = ReturnResponseAcknowledgementConfig.class),
        @XmlElement(name = "MessageTransformInbound", type = MessageTransformInboundConfig.class),
        @XmlElement(name = "MessageTransformOutbound", type = MessageTransformOutboundConfig.class),
        @XmlElement(name = "PostToEventStore", type = PostToEventStoreConfig.class),
        @XmlElement(name = "DetermineRelevantProtocolIds", type = DetermineRelevantProtocolIdsConfig.class),
        @XmlElement(name = "RunDataDistributionProtocols", type = RunDataDistributionProtocolsConfig.class),
        @XmlElement(name = "PostToSubscriberWebService", type = PostToSubscriberWebServiceConfig.class),
        @XmlElement(name = "PostToRest", type = PostToRestConfig.class),
        @XmlElement(name = "PGPDecrypt", type = PGPDecryptConfig.class)
    })
    protected List<ComponentConfig> pipelineComponents;

    /**
     * Gets the value of the pipelineComponents property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pipelineComponents property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPipelineComponents().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ForEachConfig }
     * {@link OpenEnvelopeConfig }
     * {@link EnvelopMessageConfig }
     * {@link LoadSenderConfigurationConfig }
     * {@link ValidateSenderConfig }
     * {@link ValidateMessageTypeConfig }
     * {@link PostMessageToLogConfig }
     * {@link PostMessageToExchangeConfig }
     * {@link ReturnResponseAcknowledgementConfig }
     * {@link MessageTransformInboundConfig }
     * {@link MessageTransformOutboundConfig }
     * {@link PostToEventStoreConfig }
     * {@link DetermineRelevantProtocolIdsConfig }
     * {@link RunDataDistributionProtocolsConfig }
     * {@link PostToSubscriberWebServiceConfig }
     * {@link PostToRestConfig }
     * {@link PGPDecryptConfig }
     * 
     * 
     */
    public List<ComponentConfig> getPipelineComponents() {
        if (pipelineComponents == null) {
            pipelineComponents = new ArrayList<ComponentConfig>();
        }
        return this.pipelineComponents;
    }

}

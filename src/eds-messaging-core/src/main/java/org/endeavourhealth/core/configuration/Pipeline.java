
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
 *           &lt;element name="ReadMessageEnvelope" type="{}ReadMessageEnvelopeConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="LoadDataDistributionProtocols" type="{}LoadDataDistributionProtocolsConfig" minOccurs="0"/>
 *           &lt;element name="ValidateSender" type="{}ValidateSenderConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="ValidateMessageType" type="{}ValidateMessageTypeConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PostMessageToLog" type="{}PostMessageToLogConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PostMessageToQueue" type="{}PostMessageToQueueConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="ReturnResponseAcknowledgement" type="{}ReturnResponseAcknowledgementConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="MessageTransform" type="{}MessageTransformConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PostToEventLog" type="{}PostToEventLogConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="RunDataDistributionProtocols" type="{}RunDataDistributionProtocolsConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PostToSubscriberWebService" type="{}PostToSubscriberWebServiceConfig" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PostToSender" type="{}PostToSenderConfig" maxOccurs="unbounded" minOccurs="0"/>
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
        @XmlElement(name = "ReadMessageEnvelope", type = ReadMessageEnvelopeConfig.class),
        @XmlElement(name = "LoadDataDistributionProtocols", type = LoadDataDistributionProtocolsConfig.class),
        @XmlElement(name = "ValidateSender", type = ValidateSenderConfig.class),
        @XmlElement(name = "ValidateMessageType", type = ValidateMessageTypeConfig.class),
        @XmlElement(name = "PostMessageToLog", type = PostMessageToLogConfig.class),
        @XmlElement(name = "PostMessageToQueue", type = PostMessageToQueueConfig.class),
        @XmlElement(name = "ReturnResponseAcknowledgement", type = ReturnResponseAcknowledgementConfig.class),
        @XmlElement(name = "MessageTransform", type = MessageTransformConfig.class),
        @XmlElement(name = "PostToEventLog", type = PostToEventLogConfig.class),
        @XmlElement(name = "RunDataDistributionProtocols", type = RunDataDistributionProtocolsConfig.class),
        @XmlElement(name = "PostToSubscriberWebService", type = PostToSubscriberWebServiceConfig.class),
        @XmlElement(name = "PostToSender", type = PostToSenderConfig.class)
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
     * {@link ReadMessageEnvelopeConfig }
     * {@link LoadDataDistributionProtocolsConfig }
     * {@link ValidateSenderConfig }
     * {@link ValidateMessageTypeConfig }
     * {@link PostMessageToLogConfig }
     * {@link PostMessageToQueueConfig }
     * {@link ReturnResponseAcknowledgementConfig }
     * {@link MessageTransformConfig }
     * {@link PostToEventLogConfig }
     * {@link RunDataDistributionProtocolsConfig }
     * {@link PostToSubscriberWebServiceConfig }
     * {@link PostToSenderConfig }
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

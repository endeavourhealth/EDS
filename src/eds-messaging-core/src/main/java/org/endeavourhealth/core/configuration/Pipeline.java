
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
 *           &lt;element name="ValidateSender" type="{}ValidateSender" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="ValidateMessageType" type="{}ValidateMessageType" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PostMessageToQueue" type="{}PostMessageToQueue" maxOccurs="unbounded" minOccurs="0"/>
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
    "validateSenderOrValidateMessageTypeOrPostMessageToQueue"
})
public class Pipeline {

    @XmlElements({
        @XmlElement(name = "ValidateSender", type = ValidateSender.class),
        @XmlElement(name = "ValidateMessageType", type = ValidateMessageType.class),
        @XmlElement(name = "PostMessageToQueue", type = PostMessageToQueue.class)
    })
    protected List<Object> validateSenderOrValidateMessageTypeOrPostMessageToQueue;

    /**
     * Gets the value of the validateSenderOrValidateMessageTypeOrPostMessageToQueue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the validateSenderOrValidateMessageTypeOrPostMessageToQueue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValidateSenderOrValidateMessageTypeOrPostMessageToQueue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ValidateSender }
     * {@link ValidateMessageType }
     * {@link PostMessageToQueue }
     * 
     * 
     */
    public List<Object> getValidateSenderOrValidateMessageTypeOrPostMessageToQueue() {
        if (validateSenderOrValidateMessageTypeOrPostMessageToQueue == null) {
            validateSenderOrValidateMessageTypeOrPostMessageToQueue = new ArrayList<Object>();
        }
        return this.validateSenderOrValidateMessageTypeOrPostMessageToQueue;
    }

}

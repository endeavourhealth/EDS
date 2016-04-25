
package org.endeavourhealth.messaging.configuration.schema.routeConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MessageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LogInbound" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="LogOutbound" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="MessageProcessorClass" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageType", propOrder = {
    "id",
    "logInbound",
    "logOutbound",
    "messageProcessorClass"
})
public class MessageType {

    @XmlElement(name = "Id", required = true)
    protected String id;
    @XmlElement(name = "LogInbound")
    protected boolean logInbound;
    @XmlElement(name = "LogOutbound")
    protected boolean logOutbound;
    @XmlElement(name = "MessageProcessorClass", required = true)
    protected String messageProcessorClass;

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
     * Gets the value of the logInbound property.
     * 
     */
    public boolean isLogInbound() {
        return logInbound;
    }

    /**
     * Sets the value of the logInbound property.
     * 
     */
    public void setLogInbound(boolean value) {
        this.logInbound = value;
    }

    /**
     * Gets the value of the logOutbound property.
     * 
     */
    public boolean isLogOutbound() {
        return logOutbound;
    }

    /**
     * Sets the value of the logOutbound property.
     * 
     */
    public void setLogOutbound(boolean value) {
        this.logOutbound = value;
    }

    /**
     * Gets the value of the messageProcessorClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageProcessorClass() {
        return messageProcessorClass;
    }

    /**
     * Sets the value of the messageProcessorClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageProcessorClass(String value) {
        this.messageProcessorClass = value;
    }

}


package org.endeavourhealth.messaging.configuration.schema.pluginConfiguration;

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
 *         &lt;element ref="{}Listener"/>
 *         &lt;element ref="{}Messages"/>
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
    "listener",
    "messages"
})
@XmlRootElement(name = "PluginConfiguration")
public class PluginConfiguration {

    @XmlElement(name = "Listener", required = true)
    protected Listener listener;
    @XmlElement(name = "Messages", required = true)
    protected Messages messages;

    /**
     * Gets the value of the listener property.
     * 
     * @return
     *     possible object is
     *     {@link Listener }
     *     
     */
    public Listener getListener() {
        return listener;
    }

    /**
     * Sets the value of the listener property.
     * 
     * @param value
     *     allowed object is
     *     {@link Listener }
     *     
     */
    public void setListener(Listener value) {
        this.listener = value;
    }

    /**
     * Gets the value of the messages property.
     * 
     * @return
     *     possible object is
     *     {@link Messages }
     *     
     */
    public Messages getMessages() {
        return messages;
    }

    /**
     * Sets the value of the messages property.
     * 
     * @param value
     *     allowed object is
     *     {@link Messages }
     *     
     */
    public void setMessages(Messages value) {
        this.messages = value;
    }

}

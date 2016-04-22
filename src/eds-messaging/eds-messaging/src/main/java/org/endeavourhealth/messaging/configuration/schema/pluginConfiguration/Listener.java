
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
 *         &lt;element name="Port" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element ref="{}Protocol"/>
 *         &lt;element ref="{}Endpoints"/>
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
    "port",
    "protocol",
    "receivers"
})
@XmlRootElement(name = "Listener")
public class Listener {

    @XmlElement(name = "Port")
    protected int port;
    @XmlElement(name = "Protocol", required = true)
    protected String protocol;
    @XmlElement(name = "Receivers", required = true)
    protected Receivers receivers;

    /**
     * Gets the value of the port property.
     * 
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     */
    public void setPort(int value) {
        this.port = value;
    }

    /**
     * Gets the value of the protocol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the value of the protocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProtocol(String value) {
        this.protocol = value;
    }

    /**
     * Gets the value of the receivers property.
     * 
     * @return
     *     possible object is
     *     {@link Receivers }
     *     
     */
    public Receivers getReceivers() {
        return receivers;
    }

    /**
     * Sets the value of the endpoints property.
     * 
     * @param value
     *     allowed object is
     *     {@link Receivers }
     *     
     */
    public void setEndpoints(Receivers value) {
        this.receivers = value;
    }

}

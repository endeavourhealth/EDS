
package org.endeavourhealth.messaging.configuration.schema.serviceConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SendPortType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SendPortType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;element name="Http" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="RabbitMQ" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *         &lt;element name="SendPortClass" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SendPortType", propOrder = {
    "id",
    "http",
    "rabbitMQ",
    "sendPortClass"
})
public class SendPortType {

    @XmlElement(name = "Id", required = true)
    protected String id;
    @XmlElement(name = "Http")
    protected String http;
    @XmlElement(name = "RabbitMQ")
    protected String rabbitMQ;
    @XmlElement(name = "SendPortClass", required = true)
    protected String sendPortClass;

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
     * Gets the value of the http property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHttp() {
        return http;
    }

    /**
     * Sets the value of the http property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHttp(String value) {
        this.http = value;
    }

    /**
     * Gets the value of the rabbitMQ property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRabbitMQ() {
        return rabbitMQ;
    }

    /**
     * Sets the value of the rabbitMQ property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRabbitMQ(String value) {
        this.rabbitMQ = value;
    }

    /**
     * Gets the value of the sendPortClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSendPortClass() {
        return sendPortClass;
    }

    /**
     * Sets the value of the sendPortClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSendPortClass(String value) {
        this.sendPortClass = value;
    }

}

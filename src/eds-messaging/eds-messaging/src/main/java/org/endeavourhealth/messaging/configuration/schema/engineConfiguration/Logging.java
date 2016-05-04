
package org.endeavourhealth.messaging.configuration.schema.engineConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for logging complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="logging">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="keyspace" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "logging", propOrder = {
    "keyspace"
})
public class Logging {

    @XmlElement(required = true)
    protected String keyspace;

    /**
     * Gets the value of the keyspace property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyspace() {
        return keyspace;
    }

    /**
     * Sets the value of the keyspace property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyspace(String value) {
        this.keyspace = value;
    }

}

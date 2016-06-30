
package org.endeavourhealth.core.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PostToRestConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PostToRestConfig">
 *   &lt;complexContent>
 *     &lt;extension base="{}ComponentConfig">
 *       &lt;sequence>
 *         &lt;element name="SendHeaders" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PostToRestConfig", propOrder = {
    "sendHeaders"
})
public class PostToRestConfig
    extends ComponentConfig
{

    @XmlElement(name = "SendHeaders")
    protected String sendHeaders;

    /**
     * Gets the value of the sendHeaders property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSendHeaders() {
        return sendHeaders;
    }

    /**
     * Sets the value of the sendHeaders property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSendHeaders(String value) {
        this.sendHeaders = value;
    }

}

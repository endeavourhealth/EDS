
package org.endeavourhealth.coreui.framework.config.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for webServer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="webServer">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cookieDomain" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webServer", propOrder = {
    "cookieDomain"
})
public class WebServer {

    @XmlElement(required = true)
    protected String cookieDomain;

    /**
     * Gets the value of the cookieDomain property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCookieDomain() {
        return cookieDomain;
    }

    /**
     * Sets the value of the cookieDomain property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCookieDomain(String value) {
        this.cookieDomain = value;
    }

}

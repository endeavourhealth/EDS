
package org.endeavourhealth.patientui.framework.config.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for config complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="config">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="webServer" type="{}webServer"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "config", propOrder = {
    "webServer"
})
public class Config {

    @XmlElement(required = true)
    protected WebServer webServer;

    /**
     * Gets the value of the webServer property.
     * 
     * @return
     *     possible object is
     *     {@link WebServer }
     *     
     */
    public WebServer getWebServer() {
        return webServer;
    }

    /**
     * Sets the value of the webServer property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebServer }
     *     
     */
    public void setWebServer(WebServer value) {
        this.webServer = value;
    }

}

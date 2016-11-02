
package org.endeavourhealth.coreui.framework.config.models;

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
 *         &lt;element name="rabbitmqManagement" type="{}rabbitmqManagement"/>
 *         &lt;element name="rePostMessageToExchangeConfig" type="{}rePostMessageToExchangeConfig"/>
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
    "webServer",
    "rabbitmqManagement",
    "rePostMessageToExchangeConfig"
})
public class Config {

    @XmlElement(required = true)
    protected WebServer webServer;
    @XmlElement(required = true)
    protected RabbitmqManagement rabbitmqManagement;
    @XmlElement(required = true)
    protected RePostMessageToExchangeConfig rePostMessageToExchangeConfig;

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

    /**
     * Gets the value of the rabbitmqManagement property.
     * 
     * @return
     *     possible object is
     *     {@link RabbitmqManagement }
     *     
     */
    public RabbitmqManagement getRabbitmqManagement() {
        return rabbitmqManagement;
    }

    /**
     * Sets the value of the rabbitmqManagement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RabbitmqManagement }
     *     
     */
    public void setRabbitmqManagement(RabbitmqManagement value) {
        this.rabbitmqManagement = value;
    }

    /**
     * Gets the value of the rePostMessageToExchangeConfig property.
     * 
     * @return
     *     possible object is
     *     {@link RePostMessageToExchangeConfig }
     *     
     */
    public RePostMessageToExchangeConfig getRePostMessageToExchangeConfig() {
        return rePostMessageToExchangeConfig;
    }

    /**
     * Sets the value of the rePostMessageToExchangeConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link RePostMessageToExchangeConfig }
     *     
     */
    public void setRePostMessageToExchangeConfig(RePostMessageToExchangeConfig value) {
        this.rePostMessageToExchangeConfig = value;
    }

}

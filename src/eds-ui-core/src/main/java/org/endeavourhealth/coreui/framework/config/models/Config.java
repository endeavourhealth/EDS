
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
 *         &lt;element name="database" type="{}database"/>
 *         &lt;element name="webServer" type="{}webServer"/>
 *         &lt;element name="messagingQueue" type="{}messagingQueue" minOccurs="0"/>
 *         &lt;element name="email" type="{}email" minOccurs="0"/>
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
    "database",
    "webServer",
    "rabbitmqManagement",
    "messagingQueue",
    "email"
})
public class Config {

    @XmlElement(required = true)
    protected Database database;
    @XmlElement(required = true)
    protected WebServer webServer;
    @XmlElement(required = true)
    protected RabbitmqManagement rabbitmqManagement;
    protected MessagingQueue messagingQueue;
    protected Email email;

    /**
     * Gets the value of the database property.
     * 
     * @return
     *     possible object is
     *     {@link Database }
     *     
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Sets the value of the database property.
     * 
     * @param value
     *     allowed object is
     *     {@link Database }
     *     
     */
    public void setDatabase(Database value) {
        this.database = value;
    }

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
     * Gets the value of the messagingQueue property.
     * 
     * @return
     *     possible object is
     *     {@link MessagingQueue }
     *     
     */
    public MessagingQueue getMessagingQueue() {
        return messagingQueue;
    }

    /**
     * Sets the value of the messagingQueue property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessagingQueue }
     *     
     */
    public void setMessagingQueue(MessagingQueue value) {
        this.messagingQueue = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link Email }
     *     
     */
    public Email getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link Email }
     *     
     */
    public void setEmail(Email value) {
        this.email = value;
    }

}

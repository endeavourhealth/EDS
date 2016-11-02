
package org.endeavourhealth.coreui.framework.config.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rePostMessageToExchangeConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rePostMessageToExchangeConfig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nodes" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="exchange" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="routingHeader" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="multicastHeader" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rePostMessageToExchangeConfig", propOrder = {
    "username",
    "password",
    "nodes",
    "exchange",
    "routingHeader",
    "multicastHeader"
})
public class RePostMessageToExchangeConfig {

    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected String password;
    @XmlElement(required = true)
    protected String nodes;
    @XmlElement(required = true)
    protected String exchange;
    @XmlElement(required = true)
    protected String routingHeader;
    protected String multicastHeader;

    /**
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the nodes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNodes() {
        return nodes;
    }

    /**
     * Sets the value of the nodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNodes(String value) {
        this.nodes = value;
    }

    /**
     * Gets the value of the exchange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExchange() {
        return exchange;
    }

    /**
     * Sets the value of the exchange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExchange(String value) {
        this.exchange = value;
    }

    /**
     * Gets the value of the routingHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoutingHeader() {
        return routingHeader;
    }

    /**
     * Sets the value of the routingHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoutingHeader(String value) {
        this.routingHeader = value;
    }

    /**
     * Gets the value of the multicastHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMulticastHeader() {
        return multicastHeader;
    }

    /**
     * Sets the value of the multicastHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMulticastHeader(String value) {
        this.multicastHeader = value;
    }

}

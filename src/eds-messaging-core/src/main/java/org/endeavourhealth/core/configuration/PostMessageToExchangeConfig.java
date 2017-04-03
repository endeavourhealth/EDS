
package org.endeavourhealth.core.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PostMessageToExchangeConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PostMessageToExchangeConfig">
 *   &lt;complexContent>
 *     &lt;extension base="{}ComponentConfig">
 *       &lt;sequence>
 *         &lt;element name="Exchange" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RoutingHeader" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MulticastHeader" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PostMessageToExchangeConfig", propOrder = {
    "exchange",
    "routingHeader",
    "multicastHeader"
})
public class PostMessageToExchangeConfig
    extends ComponentConfig
{

    @XmlElement(name = "Exchange", required = true)
    protected String exchange;
    @XmlElement(name = "RoutingHeader", required = true)
    protected String routingHeader;
    @XmlElement(name = "MulticastHeader")
    protected String multicastHeader;

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

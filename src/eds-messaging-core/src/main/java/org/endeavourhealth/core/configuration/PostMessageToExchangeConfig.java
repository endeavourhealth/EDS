package org.endeavourhealth.core.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


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
 *         &lt;element name="RoutingHeader" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *         &lt;element name="MulticastHeader" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AddLastMessageFlag" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="PersistLastMessageFlag" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
    "multicastHeader",
    "addLastMessageFlag",
    "persistLastMessageFlag"
})
public class PostMessageToExchangeConfig
    extends ComponentConfig
{

    @XmlElement(name = "Exchange", required = true)
    protected String exchange;
    @XmlElement(name = "RoutingHeader", required = true)
    protected List<String> routingHeader;
    @XmlElement(name = "MulticastHeader")
    protected String multicastHeader;
    @XmlElement(name = "AddLastMessageFlag")
    protected Boolean addLastMessageFlag;
    @XmlElement(name = "PersistLastMessageFlag")
    protected Boolean persistLastMessageFlag;

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
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the routingHeader property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRoutingHeader().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getRoutingHeader() {
        if (routingHeader == null) {
            routingHeader = new ArrayList<String>();
        }
        return this.routingHeader;
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

    /**
     * Gets the value of the addLastMessageFlag property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAddLastMessageFlag() {
        return addLastMessageFlag;
    }

    /**
     * Sets the value of the addLastMessageFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAddLastMessageFlag(Boolean value) {
        this.addLastMessageFlag = value;
    }

    /**
     * Gets the value of the persistLastMessageFlag property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPersistLastMessageFlag() {
        return persistLastMessageFlag;
    }

    /**
     * Sets the value of the persistLastMessageFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPersistLastMessageFlag(Boolean value) {
        this.persistLastMessageFlag = value;
    }

}

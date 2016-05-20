
package org.endeavourhealth.core.configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RMQConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RMQConfig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Exchange" type="{}RMQExchange" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RMQConfig", propOrder = {
    "exchange"
})
public class RMQConfig {

    @XmlElement(name = "Exchange", required = true)
    protected List<RMQExchange> exchange;

    /**
     * Gets the value of the exchange property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the exchange property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExchange().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RMQExchange }
     * 
     * 
     */
    public List<RMQExchange> getExchange() {
        if (exchange == null) {
            exchange = new ArrayList<RMQExchange>();
        }
        return this.exchange;
    }

}

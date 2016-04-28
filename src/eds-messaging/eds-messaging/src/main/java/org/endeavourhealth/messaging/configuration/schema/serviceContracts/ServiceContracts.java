
package org.endeavourhealth.messaging.configuration.schema.serviceContracts;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}Contracts"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contracts"
})
@XmlRootElement(name = "ServiceContracts")
public class ServiceContracts {

    @XmlElement(name = "Contracts", required = true)
    protected Contracts contracts;

    /**
     * Gets the value of the contracts property.
     * 
     * @return
     *     possible object is
     *     {@link Contracts }
     *     
     */
    public Contracts getContracts() {
        return contracts;
    }

    /**
     * Sets the value of the contracts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Contracts }
     *     
     */
    public void setContracts(Contracts value) {
        this.contracts = value;
    }

}

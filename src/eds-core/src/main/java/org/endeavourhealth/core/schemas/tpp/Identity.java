
package org.endeavourhealth.core.schemas.tpp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Identity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Identity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NHSNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Identity", propOrder = {
    "nhsNumber"
})
public class Identity {

    @XmlElement(name = "NHSNumber", required = true)
    protected String nhsNumber;

    /**
     * Gets the value of the nhsNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNHSNumber() {
        return nhsNumber;
    }

    /**
     * Sets the value of the nhsNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNHSNumber(String value) {
        this.nhsNumber = value;
    }

}

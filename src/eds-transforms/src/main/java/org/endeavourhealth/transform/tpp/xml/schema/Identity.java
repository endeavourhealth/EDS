
package org.endeavourhealth.transform.tpp.xml.schema;

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
 *         &lt;element name="NHSNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PseudoNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "nhsNumber",
    "pseudoNumber"
})
public class Identity {

    @XmlElement(name = "NHSNumber")
    protected String nhsNumber;
    @XmlElement(name = "PseudoNumber")
    protected String pseudoNumber;

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

    /**
     * Gets the value of the pseudoNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPseudoNumber() {
        return pseudoNumber;
    }

    /**
     * Sets the value of the pseudoNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPseudoNumber(String value) {
        this.pseudoNumber = value;
    }

}

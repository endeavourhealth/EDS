
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Body complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Body">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Identity" type="{}Identity"/>
 *         &lt;element name="Demographics" type="{}Demographics"/>
 *         &lt;element name="Clinical" type="{}Clinical"/>
 *         &lt;element name="NonClinical" type="{}NonClinical"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Body", propOrder = {
    "identity",
    "demographics",
    "clinical",
    "nonClinical"
})
public class Body {

    @XmlElement(name = "Identity", required = true)
    protected Identity identity;
    @XmlElement(name = "Demographics", required = true)
    protected Demographics demographics;
    @XmlElement(name = "Clinical", required = true)
    protected Clinical clinical;
    @XmlElement(name = "NonClinical", required = true)
    protected NonClinical nonClinical;

    /**
     * Gets the value of the identity property.
     * 
     * @return
     *     possible object is
     *     {@link Identity }
     *     
     */
    public Identity getIdentity() {
        return identity;
    }

    /**
     * Sets the value of the identity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Identity }
     *     
     */
    public void setIdentity(Identity value) {
        this.identity = value;
    }

    /**
     * Gets the value of the demographics property.
     * 
     * @return
     *     possible object is
     *     {@link Demographics }
     *     
     */
    public Demographics getDemographics() {
        return demographics;
    }

    /**
     * Sets the value of the demographics property.
     * 
     * @param value
     *     allowed object is
     *     {@link Demographics }
     *     
     */
    public void setDemographics(Demographics value) {
        this.demographics = value;
    }

    /**
     * Gets the value of the clinical property.
     * 
     * @return
     *     possible object is
     *     {@link Clinical }
     *     
     */
    public Clinical getClinical() {
        return clinical;
    }

    /**
     * Sets the value of the clinical property.
     * 
     * @param value
     *     allowed object is
     *     {@link Clinical }
     *     
     */
    public void setClinical(Clinical value) {
        this.clinical = value;
    }

    /**
     * Gets the value of the nonClinical property.
     * 
     * @return
     *     possible object is
     *     {@link NonClinical }
     *     
     */
    public NonClinical getNonClinical() {
        return nonClinical;
    }

    /**
     * Sets the value of the nonClinical property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonClinical }
     *     
     */
    public void setNonClinical(NonClinical value) {
        this.nonClinical = value;
    }

}

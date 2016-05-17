
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Consent Type Name/Value Pair
 * 
 * <p>Java class for dt.ConsentValue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.ConsentValue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="consentType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consentValue" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dt.ConsentValue", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "consentType",
    "consentValue"
})
public class DtConsentValue {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String consentType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected boolean consentValue;

    /**
     * Gets the value of the consentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsentType() {
        return consentType;
    }

    /**
     * Sets the value of the consentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsentType(String value) {
        this.consentType = value;
    }

    /**
     * Gets the value of the consentValue property.
     * 
     */
    public boolean isConsentValue() {
        return consentValue;
    }

    /**
     * Sets the value of the consentValue property.
     * 
     */
    public void setConsentValue(boolean value) {
        this.consentValue = value;
    }

}

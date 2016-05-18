
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IntegerCodeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IntegerCodeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="Scheme" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Term" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OldCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MapCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MapScheme" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="InterventionTarget" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IntegerCodeType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "value",
    "scheme",
    "term",
    "oldCode",
    "mapCode",
    "mapScheme",
    "interventionTarget"
})
public class IntegerCodeType {

    @XmlElement(name = "Value", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger value;
    @XmlElement(name = "Scheme", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String scheme;
    @XmlElement(name = "Term", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String term;
    @XmlElement(name = "OldCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String oldCode;
    @XmlElement(name = "MapCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String mapCode;
    @XmlElement(name = "MapScheme", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String mapScheme;
    @XmlElement(name = "InterventionTarget", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType interventionTarget;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setValue(BigInteger value) {
        this.value = value;
    }

    /**
     * Gets the value of the scheme property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the value of the scheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScheme(String value) {
        this.scheme = value;
    }

    /**
     * Gets the value of the term property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTerm() {
        return term;
    }

    /**
     * Sets the value of the term property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerm(String value) {
        this.term = value;
    }

    /**
     * Gets the value of the oldCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldCode() {
        return oldCode;
    }

    /**
     * Sets the value of the oldCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldCode(String value) {
        this.oldCode = value;
    }

    /**
     * Gets the value of the mapCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMapCode() {
        return mapCode;
    }

    /**
     * Sets the value of the mapCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMapCode(String value) {
        this.mapCode = value;
    }

    /**
     * Gets the value of the mapScheme property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMapScheme() {
        return mapScheme;
    }

    /**
     * Sets the value of the mapScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMapScheme(String value) {
        this.mapScheme = value;
    }

    /**
     * Gets the value of the interventionTarget property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getInterventionTarget() {
        return interventionTarget;
    }

    /**
     * Sets the value of the interventionTarget property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setInterventionTarget(StringCodeType value) {
        this.interventionTarget = value;
    }

}

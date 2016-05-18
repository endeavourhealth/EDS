
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StringCodeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StringCodeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Scheme" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="READ2"/>
 *               &lt;enumeration value="READ4"/>
 *               &lt;enumeration value="EMISCLINICAL"/>
 *               &lt;enumeration value="EMISDRUGGROUP"/>
 *               &lt;enumeration value="EMISCONSTITUENT"/>
 *               &lt;enumeration value="EMISDRUGNAME"/>
 *               &lt;enumeration value="EMISBOTH"/>
 *               &lt;enumeration value="EMISNONDRUGALLERGY"/>
 *               &lt;enumeration value="SNOMED"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Term" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OldCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MapCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MapScheme" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StringCodeType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "value",
    "scheme",
    "term",
    "oldCode",
    "mapCode",
    "mapScheme"
})
public class StringCodeType {

    @XmlElement(name = "Value", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String value;
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

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
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

}

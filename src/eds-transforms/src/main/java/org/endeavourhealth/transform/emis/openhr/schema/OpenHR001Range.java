
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.Range complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Range">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="description">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;maxLength value="50"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="numericRange" type="{http://www.e-mis.com/emisopen}OpenHR001.NumericRange"/>
 *           &lt;element name="textRange" type="{http://www.e-mis.com/emisopen}OpenHR001.TextRange"/>
 *         &lt;/choice>
 *         &lt;element name="units" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="sex" type="{http://www.e-mis.com/emisopen}voc.Sex" minOccurs="0"/>
 *         &lt;element name="ageRange" type="{http://www.e-mis.com/emisopen}dt.AgeRange" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="qualifier" type="{http://www.e-mis.com/emisopen}voc.RangeQualifier" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Range", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "description",
    "numericRange",
    "textRange",
    "units",
    "sex",
    "ageRange"
})
public class OpenHR001Range {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String description;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001NumericRange numericRange;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001TextRange textRange;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String units;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocSex sex;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtAgeRange ageRange;
    @XmlAttribute(name = "qualifier")
    protected VocRangeQualifier qualifier;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the numericRange property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001NumericRange }
     *     
     */
    public OpenHR001NumericRange getNumericRange() {
        return numericRange;
    }

    /**
     * Sets the value of the numericRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001NumericRange }
     *     
     */
    public void setNumericRange(OpenHR001NumericRange value) {
        this.numericRange = value;
    }

    /**
     * Gets the value of the textRange property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001TextRange }
     *     
     */
    public OpenHR001TextRange getTextRange() {
        return textRange;
    }

    /**
     * Sets the value of the textRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001TextRange }
     *     
     */
    public void setTextRange(OpenHR001TextRange value) {
        this.textRange = value;
    }

    /**
     * Gets the value of the units property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnits(String value) {
        this.units = value;
    }

    /**
     * Gets the value of the sex property.
     * 
     * @return
     *     possible object is
     *     {@link VocSex }
     *     
     */
    public VocSex getSex() {
        return sex;
    }

    /**
     * Sets the value of the sex property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocSex }
     *     
     */
    public void setSex(VocSex value) {
        this.sex = value;
    }

    /**
     * Gets the value of the ageRange property.
     * 
     * @return
     *     possible object is
     *     {@link DtAgeRange }
     *     
     */
    public DtAgeRange getAgeRange() {
        return ageRange;
    }

    /**
     * Sets the value of the ageRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtAgeRange }
     *     
     */
    public void setAgeRange(DtAgeRange value) {
        this.ageRange = value;
    }

    /**
     * Gets the value of the qualifier property.
     * 
     * @return
     *     possible object is
     *     {@link VocRangeQualifier }
     *     
     */
    public VocRangeQualifier getQualifier() {
        return qualifier;
    }

    /**
     * Sets the value of the qualifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocRangeQualifier }
     *     
     */
    public void setQualifier(VocRangeQualifier value) {
        this.qualifier = value;
    }

}

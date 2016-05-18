
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AllergyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AllergyType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}CodedItemBaseType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.e-mis.com/emisopen/MedicalRecord}Episodicity" minOccurs="0"/>
 *         &lt;element name="AllergyType" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
 *         &lt;element name="Codes" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="Exclude" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AllergyType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "episodicity",
    "allergyType",
    "codes",
    "exclude"
})
public class AllergyType
    extends CodedItemBaseType
{

    @XmlElement(name = "Episodicity", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte episodicity;
    @XmlElement(name = "AllergyType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte allergyType;
    @XmlElement(name = "Codes", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType codes;
    @XmlElement(name = "Exclude", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String exclude;

    /**
     * Gets the value of the episodicity property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getEpisodicity() {
        return episodicity;
    }

    /**
     * Sets the value of the episodicity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setEpisodicity(Byte value) {
        this.episodicity = value;
    }

    /**
     * Gets the value of the allergyType property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getAllergyType() {
        return allergyType;
    }

    /**
     * Sets the value of the allergyType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setAllergyType(Byte value) {
        this.allergyType = value;
    }

    /**
     * Gets the value of the codes property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getCodes() {
        return codes;
    }

    /**
     * Sets the value of the codes property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setCodes(StringCodeType value) {
        this.codes = value;
    }

    /**
     * Gets the value of the exclude property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExclude() {
        return exclude;
    }

    /**
     * Sets the value of the exclude property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExclude(String value) {
        this.exclude = value;
    }

}

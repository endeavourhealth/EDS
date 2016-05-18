
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Community Care Plans
 * 
 * <p>Java class for CarePlanType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CarePlanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PKId" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType"/>
 *         &lt;element name="DisplayTerm" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TermId" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="Notes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TemplateId" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="InterventionCategoryId" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType"/>
 *         &lt;element name="InterventionTargetTermId" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CarePlanType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "pkId",
    "displayTerm",
    "termId",
    "notes",
    "templateId",
    "interventionCategoryId",
    "interventionTargetTermId"
})
public class CarePlanType {

    @XmlElement(name = "PKId", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected IdentType pkId;
    @XmlElement(name = "DisplayTerm", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String displayTerm;
    @XmlElement(name = "TermId", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType termId;
    @XmlElement(name = "Notes", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String notes;
    @XmlElement(name = "TemplateId", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType templateId;
    @XmlElement(name = "InterventionCategoryId", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected IdentType interventionCategoryId;
    @XmlElement(name = "InterventionTargetTermId", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType interventionTargetTermId;

    /**
     * Gets the value of the pkId property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getPKId() {
        return pkId;
    }

    /**
     * Sets the value of the pkId property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setPKId(IdentType value) {
        this.pkId = value;
    }

    /**
     * Gets the value of the displayTerm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayTerm() {
        return displayTerm;
    }

    /**
     * Sets the value of the displayTerm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayTerm(String value) {
        this.displayTerm = value;
    }

    /**
     * Gets the value of the termId property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getTermId() {
        return termId;
    }

    /**
     * Sets the value of the termId property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setTermId(StringCodeType value) {
        this.termId = value;
    }

    /**
     * Gets the value of the notes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotes(String value) {
        this.notes = value;
    }

    /**
     * Gets the value of the templateId property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getTemplateId() {
        return templateId;
    }

    /**
     * Sets the value of the templateId property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setTemplateId(IdentType value) {
        this.templateId = value;
    }

    /**
     * Gets the value of the interventionCategoryId property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getInterventionCategoryId() {
        return interventionCategoryId;
    }

    /**
     * Sets the value of the interventionCategoryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setInterventionCategoryId(IdentType value) {
        this.interventionCategoryId = value;
    }

    /**
     * Gets the value of the interventionTargetTermId property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getInterventionTargetTermId() {
        return interventionTargetTermId;
    }

    /**
     * Sets the value of the interventionTargetTermId property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setInterventionTargetTermId(StringCodeType value) {
        this.interventionTargetTermId = value;
    }

}


package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ControlledDrugInfoType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ControlledDrugInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DduBatch" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DduPrinted" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DduCollect" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DduNextDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DduReduceBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DduAfter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DduUntil" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DduNextDose" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DduOffset" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DduClWorker" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DduPickUp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DduDoseAbbrev" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ControlledDrugInfoType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "dduBatch",
    "dduPrinted",
    "dduCollect",
    "dduNextDate",
    "dduReduceBy",
    "dduAfter",
    "dduUntil",
    "dduNextDose",
    "dduOffset",
    "dduClWorker",
    "dduPickUp",
    "dduDoseAbbrev"
})
public class ControlledDrugInfoType {

    @XmlElement(name = "DduBatch", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduBatch;
    @XmlElement(name = "DduPrinted", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduPrinted;
    @XmlElement(name = "DduCollect", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduCollect;
    @XmlElement(name = "DduNextDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduNextDate;
    @XmlElement(name = "DduReduceBy", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduReduceBy;
    @XmlElement(name = "DduAfter", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduAfter;
    @XmlElement(name = "DduUntil", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduUntil;
    @XmlElement(name = "DduNextDose", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduNextDose;
    @XmlElement(name = "DduOffset", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduOffset;
    @XmlElement(name = "DduClWorker", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduClWorker;
    @XmlElement(name = "DduPickUp", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduPickUp;
    @XmlElement(name = "DduDoseAbbrev", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dduDoseAbbrev;

    /**
     * Gets the value of the dduBatch property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduBatch() {
        return dduBatch;
    }

    /**
     * Sets the value of the dduBatch property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduBatch(String value) {
        this.dduBatch = value;
    }

    /**
     * Gets the value of the dduPrinted property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduPrinted() {
        return dduPrinted;
    }

    /**
     * Sets the value of the dduPrinted property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduPrinted(String value) {
        this.dduPrinted = value;
    }

    /**
     * Gets the value of the dduCollect property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduCollect() {
        return dduCollect;
    }

    /**
     * Sets the value of the dduCollect property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduCollect(String value) {
        this.dduCollect = value;
    }

    /**
     * Gets the value of the dduNextDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduNextDate() {
        return dduNextDate;
    }

    /**
     * Sets the value of the dduNextDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduNextDate(String value) {
        this.dduNextDate = value;
    }

    /**
     * Gets the value of the dduReduceBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduReduceBy() {
        return dduReduceBy;
    }

    /**
     * Sets the value of the dduReduceBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduReduceBy(String value) {
        this.dduReduceBy = value;
    }

    /**
     * Gets the value of the dduAfter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduAfter() {
        return dduAfter;
    }

    /**
     * Sets the value of the dduAfter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduAfter(String value) {
        this.dduAfter = value;
    }

    /**
     * Gets the value of the dduUntil property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduUntil() {
        return dduUntil;
    }

    /**
     * Sets the value of the dduUntil property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduUntil(String value) {
        this.dduUntil = value;
    }

    /**
     * Gets the value of the dduNextDose property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduNextDose() {
        return dduNextDose;
    }

    /**
     * Sets the value of the dduNextDose property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduNextDose(String value) {
        this.dduNextDose = value;
    }

    /**
     * Gets the value of the dduOffset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduOffset() {
        return dduOffset;
    }

    /**
     * Sets the value of the dduOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduOffset(String value) {
        this.dduOffset = value;
    }

    /**
     * Gets the value of the dduClWorker property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduClWorker() {
        return dduClWorker;
    }

    /**
     * Sets the value of the dduClWorker property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduClWorker(String value) {
        this.dduClWorker = value;
    }

    /**
     * Gets the value of the dduPickUp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduPickUp() {
        return dduPickUp;
    }

    /**
     * Sets the value of the dduPickUp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduPickUp(String value) {
        this.dduPickUp = value;
    }

    /**
     * Gets the value of the dduDoseAbbrev property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDduDoseAbbrev() {
        return dduDoseAbbrev;
    }

    /**
     * Sets the value of the dduDoseAbbrev property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDduDoseAbbrev(String value) {
        this.dduDoseAbbrev = value;
    }

}

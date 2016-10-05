
package org.endeavourhealth.core.xml.enterprise;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;


/**
 * <p>Java class for medication_order complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="medication_order">
 *   &lt;complexContent>
 *     &lt;extension base="{}baseRecord">
 *       &lt;sequence>
 *         &lt;element name="organization_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="patient_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="encounter_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="practitioner_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="clinical_effective_date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="date_precision_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="dmd_id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="dose" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="quantity_value" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="quantity_unit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="duration_days" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="estimated_cost" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="medication_statement_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "medication_order", propOrder = {
    "organizationId",
    "patientId",
    "encounterId",
    "practitionerId",
    "clinicalEffectiveDate",
    "datePrecisionId",
    "dmdId",
    "dose",
    "quantityValue",
    "quantityUnit",
    "durationDays",
    "estimatedCost",
    "medicationStatementId"
})
public class MedicationOrder
    extends BaseRecord
{

    @XmlElement(name = "organization_id")
    protected int organizationId;
    @XmlElement(name = "patient_id")
    protected int patientId;
    @XmlElement(name = "encounter_id")
    protected Integer encounterId;
    @XmlElement(name = "practitioner_id")
    protected Integer practitionerId;
    @XmlElement(name = "clinical_effective_date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar clinicalEffectiveDate;
    @XmlElement(name = "date_precision_id")
    protected Integer datePrecisionId;
    @XmlElement(name = "dmd_id")
    protected Long dmdId;
    protected String dose;
    @XmlElement(name = "quantity_value")
    protected BigDecimal quantityValue;
    @XmlElement(name = "quantity_unit")
    protected String quantityUnit;
    @XmlElement(name = "duration_days")
    protected Integer durationDays;
    @XmlElement(name = "estimated_cost")
    protected BigDecimal estimatedCost;
    @XmlElement(name = "medication_statement_id")
    protected Integer medicationStatementId;

    /**
     * Gets the value of the organizationId property.
     * 
     */
    public int getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the value of the organizationId property.
     * 
     */
    public void setOrganizationId(int value) {
        this.organizationId = value;
    }

    /**
     * Gets the value of the patientId property.
     * 
     */
    public int getPatientId() {
        return patientId;
    }

    /**
     * Sets the value of the patientId property.
     * 
     */
    public void setPatientId(int value) {
        this.patientId = value;
    }

    /**
     * Gets the value of the encounterId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEncounterId() {
        return encounterId;
    }

    /**
     * Sets the value of the encounterId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEncounterId(Integer value) {
        this.encounterId = value;
    }

    /**
     * Gets the value of the practitionerId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPractitionerId() {
        return practitionerId;
    }

    /**
     * Sets the value of the practitionerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPractitionerId(Integer value) {
        this.practitionerId = value;
    }

    /**
     * Gets the value of the clinicalEffectiveDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getClinicalEffectiveDate() {
        return clinicalEffectiveDate;
    }

    /**
     * Sets the value of the clinicalEffectiveDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setClinicalEffectiveDate(XMLGregorianCalendar value) {
        this.clinicalEffectiveDate = value;
    }

    /**
     * Gets the value of the datePrecisionId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDatePrecisionId() {
        return datePrecisionId;
    }

    /**
     * Sets the value of the datePrecisionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDatePrecisionId(Integer value) {
        this.datePrecisionId = value;
    }

    /**
     * Gets the value of the dmdId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDmdId() {
        return dmdId;
    }

    /**
     * Sets the value of the dmdId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDmdId(Long value) {
        this.dmdId = value;
    }

    /**
     * Gets the value of the dose property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDose() {
        return dose;
    }

    /**
     * Sets the value of the dose property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDose(String value) {
        this.dose = value;
    }

    /**
     * Gets the value of the quantityValue property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getQuantityValue() {
        return quantityValue;
    }

    /**
     * Sets the value of the quantityValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setQuantityValue(BigDecimal value) {
        this.quantityValue = value;
    }

    /**
     * Gets the value of the quantityUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuantityUnit() {
        return quantityUnit;
    }

    /**
     * Sets the value of the quantityUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuantityUnit(String value) {
        this.quantityUnit = value;
    }

    /**
     * Gets the value of the durationDays property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDurationDays() {
        return durationDays;
    }

    /**
     * Sets the value of the durationDays property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDurationDays(Integer value) {
        this.durationDays = value;
    }

    /**
     * Gets the value of the estimatedCost property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    /**
     * Sets the value of the estimatedCost property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setEstimatedCost(BigDecimal value) {
        this.estimatedCost = value;
    }

    /**
     * Gets the value of the medicationStatementId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMedicationStatementId() {
        return medicationStatementId;
    }

    /**
     * Sets the value of the medicationStatementId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMedicationStatementId(Integer value) {
        this.medicationStatementId = value;
    }

}

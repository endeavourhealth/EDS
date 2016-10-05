
package org.endeavourhealth.core.xml.enterprise;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;


/**
 * <p>Java class for medication_statement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="medication_statement">
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
 *         &lt;element name="is_active" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="cancellation_date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="dose" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="quantity_value" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="quantity_unit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="medication_statement_authorisation_type_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "medication_statement", propOrder = {
    "organizationId",
    "patientId",
    "encounterId",
    "practitionerId",
    "clinicalEffectiveDate",
    "datePrecisionId",
    "dmdId",
    "isActive",
    "cancellationDate",
    "dose",
    "quantityValue",
    "quantityUnit",
    "medicationStatementAuthorisationTypeId"
})
public class MedicationStatement
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
    @XmlElement(name = "is_active")
    protected Boolean isActive;
    @XmlElement(name = "cancellation_date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar cancellationDate;
    protected String dose;
    @XmlElement(name = "quantity_value")
    protected BigDecimal quantityValue;
    @XmlElement(name = "quantity_unit")
    protected String quantityUnit;
    @XmlElement(name = "medication_statement_authorisation_type_id")
    protected int medicationStatementAuthorisationTypeId;

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
     * Gets the value of the isActive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsActive() {
        return isActive;
    }

    /**
     * Sets the value of the isActive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsActive(Boolean value) {
        this.isActive = value;
    }

    /**
     * Gets the value of the cancellationDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCancellationDate() {
        return cancellationDate;
    }

    /**
     * Sets the value of the cancellationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCancellationDate(XMLGregorianCalendar value) {
        this.cancellationDate = value;
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
     * Gets the value of the medicationStatementAuthorisationTypeId property.
     * 
     */
    public int getMedicationStatementAuthorisationTypeId() {
        return medicationStatementAuthorisationTypeId;
    }

    /**
     * Sets the value of the medicationStatementAuthorisationTypeId property.
     * 
     */
    public void setMedicationStatementAuthorisationTypeId(int value) {
        this.medicationStatementAuthorisationTypeId = value;
    }

}


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
 *         &lt;element name="organisation_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="patient_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="encounter_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="practitioner_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="date_precision" type="{}date_precision" minOccurs="0"/>
 *         &lt;element name="dmd_id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="status" type="{}medication_statement_status" minOccurs="0"/>
 *         &lt;element name="cancellation_date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="dose" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="quantity_value" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="quantity_unit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="authorisation_type" type="{}medication_statement_authorisation_type" minOccurs="0"/>
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
    "organisationId",
    "patientId",
    "encounterId",
    "practitionerId",
    "date",
    "datePrecision",
    "dmdId",
    "status",
    "cancellationDate",
    "dose",
    "quantityValue",
    "quantityUnit",
    "authorisationType"
})
public class MedicationStatement
    extends BaseRecord
{

    @XmlElement(name = "organisation_id")
    protected String organisationId;
    @XmlElement(name = "patient_id")
    protected String patientId;
    @XmlElement(name = "encounter_id")
    protected String encounterId;
    @XmlElement(name = "practitioner_id")
    protected String practitionerId;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar date;
    @XmlElement(name = "date_precision")
    @XmlSchemaType(name = "string")
    protected DatePrecision datePrecision;
    @XmlElement(name = "dmd_id")
    protected Long dmdId;
    @XmlSchemaType(name = "string")
    protected MedicationStatementStatus status;
    @XmlElement(name = "cancellation_date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar cancellationDate;
    protected String dose;
    @XmlElement(name = "quantity_value")
    protected BigDecimal quantityValue;
    @XmlElement(name = "quantity_unit")
    protected String quantityUnit;
    @XmlElement(name = "authorisation_type")
    @XmlSchemaType(name = "string")
    protected MedicationStatementAuthorisationType authorisationType;

    /**
     * Gets the value of the organisationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisationId() {
        return organisationId;
    }

    /**
     * Sets the value of the organisationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisationId(String value) {
        this.organisationId = value;
    }

    /**
     * Gets the value of the patientId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * Sets the value of the patientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientId(String value) {
        this.patientId = value;
    }

    /**
     * Gets the value of the encounterId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncounterId() {
        return encounterId;
    }

    /**
     * Sets the value of the encounterId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncounterId(String value) {
        this.encounterId = value;
    }

    /**
     * Gets the value of the practitionerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPractitionerId() {
        return practitionerId;
    }

    /**
     * Sets the value of the practitionerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPractitionerId(String value) {
        this.practitionerId = value;
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDate(XMLGregorianCalendar value) {
        this.date = value;
    }

    /**
     * Gets the value of the datePrecision property.
     * 
     * @return
     *     possible object is
     *     {@link DatePrecision }
     *     
     */
    public DatePrecision getDatePrecision() {
        return datePrecision;
    }

    /**
     * Sets the value of the datePrecision property.
     * 
     * @param value
     *     allowed object is
     *     {@link DatePrecision }
     *     
     */
    public void setDatePrecision(DatePrecision value) {
        this.datePrecision = value;
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
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link MedicationStatementStatus }
     *     
     */
    public MedicationStatementStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicationStatementStatus }
     *     
     */
    public void setStatus(MedicationStatementStatus value) {
        this.status = value;
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
     * Gets the value of the authorisationType property.
     * 
     * @return
     *     possible object is
     *     {@link MedicationStatementAuthorisationType }
     *     
     */
    public MedicationStatementAuthorisationType getAuthorisationType() {
        return authorisationType;
    }

    /**
     * Sets the value of the authorisationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicationStatementAuthorisationType }
     *     
     */
    public void setAuthorisationType(MedicationStatementAuthorisationType value) {
        this.authorisationType = value;
    }

}


package org.endeavourhealth.core.xml.enterprise;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for referral_request complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="referral_request">
 *   &lt;complexContent>
 *     &lt;extension base="{}baseRecord">
 *       &lt;sequence>
 *         &lt;element name="organisation_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="patient_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="encounter_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="practitioner_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="date_precision" type="{}date_precision" minOccurs="0"/>
 *         &lt;element name="snomed_concept_id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="recipient_organisation_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="recipient_organisation_ods_code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="priority" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="service_requested" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "referral_request", propOrder = {
    "organisationId",
    "patientId",
    "encounterId",
    "practitionerId",
    "date",
    "datePrecision",
    "snomedConceptId",
    "recipientOrganisationName",
    "recipientOrganisationOdsCode",
    "priority",
    "serviceRequested",
    "mode"
})
public class ReferralRequest
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
    @XmlElement(name = "snomed_concept_id")
    protected Long snomedConceptId;
    @XmlElement(name = "recipient_organisation_name")
    protected String recipientOrganisationName;
    @XmlElement(name = "recipient_organisation_ods_code")
    protected String recipientOrganisationOdsCode;
    protected String priority;
    @XmlElement(name = "service_requested")
    protected String serviceRequested;
    protected String mode;

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
     * Gets the value of the snomedConceptId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSnomedConceptId() {
        return snomedConceptId;
    }

    /**
     * Sets the value of the snomedConceptId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSnomedConceptId(Long value) {
        this.snomedConceptId = value;
    }

    /**
     * Gets the value of the recipientOrganisationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecipientOrganisationName() {
        return recipientOrganisationName;
    }

    /**
     * Sets the value of the recipientOrganisationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecipientOrganisationName(String value) {
        this.recipientOrganisationName = value;
    }

    /**
     * Gets the value of the recipientOrganisationOdsCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecipientOrganisationOdsCode() {
        return recipientOrganisationOdsCode;
    }

    /**
     * Sets the value of the recipientOrganisationOdsCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecipientOrganisationOdsCode(String value) {
        this.recipientOrganisationOdsCode = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPriority(String value) {
        this.priority = value;
    }

    /**
     * Gets the value of the serviceRequested property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceRequested() {
        return serviceRequested;
    }

    /**
     * Sets the value of the serviceRequested property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceRequested(String value) {
        this.serviceRequested = value;
    }

    /**
     * Gets the value of the mode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMode() {
        return mode;
    }

    /**
     * Sets the value of the mode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMode(String value) {
        this.mode = value;
    }

}

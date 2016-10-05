
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
 *         &lt;element name="organization_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="patient_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="encounter_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="practitioner_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="clinical_effective_date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="date_precision_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="snomed_concept_id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="recipient_organization_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="priority" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="service_requested" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="outgoing_referral" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
    "organizationId",
    "patientId",
    "encounterId",
    "practitionerId",
    "clinicalEffectiveDate",
    "datePrecisionId",
    "snomedConceptId",
    "recipientOrganizationId",
    "priority",
    "serviceRequested",
    "mode",
    "outgoingReferral"
})
public class ReferralRequest
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
    @XmlElement(name = "snomed_concept_id")
    protected Long snomedConceptId;
    @XmlElement(name = "recipient_organization_id")
    protected int recipientOrganizationId;
    protected String priority;
    @XmlElement(name = "service_requested")
    protected String serviceRequested;
    protected String mode;
    @XmlElement(name = "outgoing_referral")
    protected Boolean outgoingReferral;

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
     * Gets the value of the recipientOrganizationId property.
     * 
     */
    public int getRecipientOrganizationId() {
        return recipientOrganizationId;
    }

    /**
     * Sets the value of the recipientOrganizationId property.
     * 
     */
    public void setRecipientOrganizationId(int value) {
        this.recipientOrganizationId = value;
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

    /**
     * Gets the value of the outgoingReferral property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOutgoingReferral() {
        return outgoingReferral;
    }

    /**
     * Sets the value of the outgoingReferral property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOutgoingReferral(Boolean value) {
        this.outgoingReferral = value;
    }

}

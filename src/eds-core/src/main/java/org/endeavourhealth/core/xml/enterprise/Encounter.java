
package org.endeavourhealth.core.xml.enterprise;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for encounter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="encounter">
 *   &lt;complexContent>
 *     &lt;extension base="{}baseRecord">
 *       &lt;sequence>
 *         &lt;element name="organization_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="patient_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="practitioner_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="appointment_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="clinical_effective_date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="date_precision_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "encounter", propOrder = {
    "organizationId",
    "patientId",
    "practitionerId",
    "appointmentId",
    "clinicalEffectiveDate",
    "datePrecisionId"
})
public class Encounter
    extends BaseRecord
{

    @XmlElement(name = "organization_id")
    protected int organizationId;
    @XmlElement(name = "patient_id")
    protected int patientId;
    @XmlElement(name = "practitioner_id")
    protected Integer practitionerId;
    @XmlElement(name = "appointment_id")
    protected Integer appointmentId;
    @XmlElement(name = "clinical_effective_date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar clinicalEffectiveDate;
    @XmlElement(name = "date_precision_id")
    protected Integer datePrecisionId;

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
     * Gets the value of the appointmentId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAppointmentId() {
        return appointmentId;
    }

    /**
     * Sets the value of the appointmentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAppointmentId(Integer value) {
        this.appointmentId = value;
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

}

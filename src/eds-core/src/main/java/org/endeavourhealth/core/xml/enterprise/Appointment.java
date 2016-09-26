
package org.endeavourhealth.core.xml.enterprise;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for appointment complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="appointment">
 *   &lt;complexContent>
 *     &lt;extension base="{}baseRecord">
 *       &lt;sequence>
 *         &lt;element name="organisation_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="patient_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="practitioner_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="schedule_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="start" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="end" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="planned_duration" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="actual_duration" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="status" type="{}appointment_status" minOccurs="0"/>
 *         &lt;element name="patient_wait" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="patient_delay" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="sent_in" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="left" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "appointment", propOrder = {
    "organisationId",
    "patientId",
    "practitionerId",
    "scheduleId",
    "start",
    "end",
    "plannedDuration",
    "actualDuration",
    "status",
    "patientWait",
    "patientDelay",
    "sentIn",
    "left"
})
public class Appointment
    extends BaseRecord
{

    @XmlElement(name = "organisation_id")
    protected String organisationId;
    @XmlElement(name = "patient_id")
    protected String patientId;
    @XmlElement(name = "practitioner_id")
    protected String practitionerId;
    @XmlElement(name = "schedule_id")
    protected String scheduleId;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar start;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar end;
    @XmlElement(name = "planned_duration")
    protected Integer plannedDuration;
    @XmlElement(name = "actual_duration")
    protected Integer actualDuration;
    @XmlSchemaType(name = "string")
    protected AppointmentStatus status;
    @XmlElement(name = "patient_wait")
    protected Integer patientWait;
    @XmlElement(name = "patient_delay")
    protected Integer patientDelay;
    @XmlElement(name = "sent_in")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar sentIn;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar left;

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
     * Gets the value of the scheduleId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScheduleId() {
        return scheduleId;
    }

    /**
     * Sets the value of the scheduleId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScheduleId(String value) {
        this.scheduleId = value;
    }

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStart(XMLGregorianCalendar value) {
        this.start = value;
    }

    /**
     * Gets the value of the end property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEnd() {
        return end;
    }

    /**
     * Sets the value of the end property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEnd(XMLGregorianCalendar value) {
        this.end = value;
    }

    /**
     * Gets the value of the plannedDuration property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPlannedDuration() {
        return plannedDuration;
    }

    /**
     * Sets the value of the plannedDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPlannedDuration(Integer value) {
        this.plannedDuration = value;
    }

    /**
     * Gets the value of the actualDuration property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getActualDuration() {
        return actualDuration;
    }

    /**
     * Sets the value of the actualDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setActualDuration(Integer value) {
        this.actualDuration = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link AppointmentStatus }
     *     
     */
    public AppointmentStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link AppointmentStatus }
     *     
     */
    public void setStatus(AppointmentStatus value) {
        this.status = value;
    }

    /**
     * Gets the value of the patientWait property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPatientWait() {
        return patientWait;
    }

    /**
     * Sets the value of the patientWait property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPatientWait(Integer value) {
        this.patientWait = value;
    }

    /**
     * Gets the value of the patientDelay property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPatientDelay() {
        return patientDelay;
    }

    /**
     * Sets the value of the patientDelay property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPatientDelay(Integer value) {
        this.patientDelay = value;
    }

    /**
     * Gets the value of the sentIn property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSentIn() {
        return sentIn;
    }

    /**
     * Sets the value of the sentIn property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSentIn(XMLGregorianCalendar value) {
        this.sentIn = value;
    }

    /**
     * Gets the value of the left property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLeft() {
        return left;
    }

    /**
     * Sets the value of the left property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLeft(XMLGregorianCalendar value) {
        this.left = value;
    }

}

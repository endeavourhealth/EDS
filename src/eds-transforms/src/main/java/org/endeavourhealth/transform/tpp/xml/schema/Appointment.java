
package org.endeavourhealth.transform.tpp.xml.schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Appointment complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Appointment">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="Duration" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="Clinician" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="UserName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Site" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ClinicType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AppointmentUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Status" type="{}AppointmentStatus"/>
 *         &lt;element name="Comments" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LinkedReferralUID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Flag" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Appointment", propOrder = {
    "dateTime",
    "duration",
    "clinician",
    "userName",
    "site",
    "clinicType",
    "appointmentUID",
    "status",
    "comments",
    "linkedReferralUID",
    "flag"
})
public class Appointment {

    @XmlElement(name = "DateTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTime;
    @XmlElement(name = "Duration", required = true)
    protected BigDecimal duration;
    @XmlElement(name = "Clinician", required = true)
    protected String clinician;
    @XmlElement(name = "UserName", required = true)
    protected String userName;
    @XmlElement(name = "Site", required = true)
    protected String site;
    @XmlElement(name = "ClinicType", required = true)
    protected String clinicType;
    @XmlElement(name = "AppointmentUID", required = true)
    protected String appointmentUID;
    @XmlElement(name = "Status", required = true)
    @XmlSchemaType(name = "string")
    protected AppointmentStatus status;
    @XmlElement(name = "Comments")
    protected String comments;
    @XmlElement(name = "LinkedReferralUID")
    protected String linkedReferralUID;
    @XmlElement(name = "Flag")
    protected List<String> flag;

    /**
     * Gets the value of the dateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTime() {
        return dateTime;
    }

    /**
     * Sets the value of the dateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTime(XMLGregorianCalendar value) {
        this.dateTime = value;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setDuration(BigDecimal value) {
        this.duration = value;
    }

    /**
     * Gets the value of the clinician property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClinician() {
        return clinician;
    }

    /**
     * Sets the value of the clinician property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClinician(String value) {
        this.clinician = value;
    }

    /**
     * Gets the value of the userName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of the site property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSite() {
        return site;
    }

    /**
     * Sets the value of the site property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSite(String value) {
        this.site = value;
    }

    /**
     * Gets the value of the clinicType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClinicType() {
        return clinicType;
    }

    /**
     * Sets the value of the clinicType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClinicType(String value) {
        this.clinicType = value;
    }

    /**
     * Gets the value of the appointmentUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppointmentUID() {
        return appointmentUID;
    }

    /**
     * Sets the value of the appointmentUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppointmentUID(String value) {
        this.appointmentUID = value;
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
     * Gets the value of the comments property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the value of the comments property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComments(String value) {
        this.comments = value;
    }

    /**
     * Gets the value of the linkedReferralUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkedReferralUID() {
        return linkedReferralUID;
    }

    /**
     * Sets the value of the linkedReferralUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkedReferralUID(String value) {
        this.linkedReferralUID = value;
    }

    /**
     * Gets the value of the flag property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the flag property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFlag().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFlag() {
        if (flag == null) {
            flag = new ArrayList<String>();
        }
        return this.flag;
    }

}

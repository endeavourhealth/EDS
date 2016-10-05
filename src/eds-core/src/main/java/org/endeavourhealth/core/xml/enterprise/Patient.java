
package org.endeavourhealth.core.xml.enterprise;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for patient complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="patient">
 *   &lt;complexContent>
 *     &lt;extension base="{}baseRecord">
 *       &lt;sequence>
 *         &lt;element name="organization_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="date_of_birth" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="year_of_death" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="patient_gender_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="date_registered" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="date_registered_end" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="usual_gp_practitioner_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="registration_type_code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="registration_type_desc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pseudo_id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "patient", propOrder = {
    "organizationId",
    "dateOfBirth",
    "yearOfDeath",
    "patientGenderId",
    "dateRegistered",
    "dateRegisteredEnd",
    "usualGpPractitionerId",
    "registrationTypeCode",
    "registrationTypeDesc",
    "pseudoId"
})
public class Patient
    extends BaseRecord
{

    @XmlElement(name = "organization_id")
    protected int organizationId;
    @XmlElement(name = "date_of_birth", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateOfBirth;
    @XmlElement(name = "year_of_death")
    protected Integer yearOfDeath;
    @XmlElement(name = "patient_gender_id")
    protected int patientGenderId;
    @XmlElement(name = "date_registered")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateRegistered;
    @XmlElement(name = "date_registered_end")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateRegisteredEnd;
    @XmlElement(name = "usual_gp_practitioner_id")
    protected Integer usualGpPractitionerId;
    @XmlElement(name = "registration_type_code")
    protected String registrationTypeCode;
    @XmlElement(name = "registration_type_desc")
    protected String registrationTypeDesc;
    @XmlElement(name = "pseudo_id")
    protected String pseudoId;

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
     * Gets the value of the dateOfBirth property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the value of the dateOfBirth property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateOfBirth(XMLGregorianCalendar value) {
        this.dateOfBirth = value;
    }

    /**
     * Gets the value of the yearOfDeath property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getYearOfDeath() {
        return yearOfDeath;
    }

    /**
     * Sets the value of the yearOfDeath property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setYearOfDeath(Integer value) {
        this.yearOfDeath = value;
    }

    /**
     * Gets the value of the patientGenderId property.
     * 
     */
    public int getPatientGenderId() {
        return patientGenderId;
    }

    /**
     * Sets the value of the patientGenderId property.
     * 
     */
    public void setPatientGenderId(int value) {
        this.patientGenderId = value;
    }

    /**
     * Gets the value of the dateRegistered property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateRegistered() {
        return dateRegistered;
    }

    /**
     * Sets the value of the dateRegistered property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateRegistered(XMLGregorianCalendar value) {
        this.dateRegistered = value;
    }

    /**
     * Gets the value of the dateRegisteredEnd property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateRegisteredEnd() {
        return dateRegisteredEnd;
    }

    /**
     * Sets the value of the dateRegisteredEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateRegisteredEnd(XMLGregorianCalendar value) {
        this.dateRegisteredEnd = value;
    }

    /**
     * Gets the value of the usualGpPractitionerId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getUsualGpPractitionerId() {
        return usualGpPractitionerId;
    }

    /**
     * Sets the value of the usualGpPractitionerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setUsualGpPractitionerId(Integer value) {
        this.usualGpPractitionerId = value;
    }

    /**
     * Gets the value of the registrationTypeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegistrationTypeCode() {
        return registrationTypeCode;
    }

    /**
     * Sets the value of the registrationTypeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegistrationTypeCode(String value) {
        this.registrationTypeCode = value;
    }

    /**
     * Gets the value of the registrationTypeDesc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegistrationTypeDesc() {
        return registrationTypeDesc;
    }

    /**
     * Sets the value of the registrationTypeDesc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegistrationTypeDesc(String value) {
        this.registrationTypeDesc = value;
    }

    /**
     * Gets the value of the pseudoId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPseudoId() {
        return pseudoId;
    }

    /**
     * Sets the value of the pseudoId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPseudoId(String value) {
        this.pseudoId = value;
    }

}

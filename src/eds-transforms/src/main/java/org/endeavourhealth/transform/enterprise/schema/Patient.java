
package org.endeavourhealth.transform.enterprise.schema;

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
 *         &lt;element name="organisation_id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="date_of_birth" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="year_of_death" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="gender" type="{}gender" minOccurs="0"/>
 *         &lt;element name="date_registered" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="date_registered_end" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="usual_gp_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "organisationId",
    "dateOfBirth",
    "yearOfDeath",
    "gender",
    "dateRegistered",
    "dateRegisteredEnd",
    "usualGpName",
    "registrationTypeCode",
    "registrationTypeDesc",
    "pseudoId"
})
public class Patient
    extends BaseRecord
{

    @XmlElement(name = "organisation_id")
    protected String organisationId;
    @XmlElement(name = "date_of_birth")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateOfBirth;
    @XmlElement(name = "year_of_death")
    protected Integer yearOfDeath;
    @XmlSchemaType(name = "string")
    protected Gender gender;
    @XmlElement(name = "date_registered")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateRegistered;
    @XmlElement(name = "date_registered_end")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateRegisteredEnd;
    @XmlElement(name = "usual_gp_name")
    protected String usualGpName;
    @XmlElement(name = "registration_type_code")
    protected String registrationTypeCode;
    @XmlElement(name = "registration_type_desc")
    protected String registrationTypeDesc;
    @XmlElement(name = "pseudo_id")
    protected String pseudoId;

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
     * Gets the value of the gender property.
     * 
     * @return
     *     possible object is
     *     {@link Gender }
     *     
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Sets the value of the gender property.
     * 
     * @param value
     *     allowed object is
     *     {@link Gender }
     *     
     */
    public void setGender(Gender value) {
        this.gender = value;
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
     * Gets the value of the usualGpName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsualGpName() {
        return usualGpName;
    }

    /**
     * Sets the value of the usualGpName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsualGpName(String value) {
        this.usualGpName = value;
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

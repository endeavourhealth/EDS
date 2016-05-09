
package org.endeavourhealth.core.schemas.tpp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Demographics complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Demographics">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FirstName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MiddleNames" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Surname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="KnownAs" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DateOfBirth" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="Sex">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="M"/>
 *               &lt;enumeration value="F"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="MaritalStatus" type="{}Code" minOccurs="0"/>
 *         &lt;element name="Ethnicity" type="{}Code" minOccurs="0"/>
 *         &lt;element name="MainLanguage" type="{}Code" minOccurs="0"/>
 *         &lt;element name="EnglishSpeaker">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Unknown"/>
 *               &lt;enumeration value="Yes"/>
 *               &lt;enumeration value="No"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="HomeTelephone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="WorkTelephone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MobileTelephone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AlternateTelephone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EmailAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SMSConsent">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Yes"/>
 *               &lt;enumeration value="No"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="UsualGPUserName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CareStartDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="RegistrationType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Demographics", propOrder = {
    "title",
    "firstName",
    "middleNames",
    "surname",
    "knownAs",
    "dateOfBirth",
    "sex",
    "maritalStatus",
    "ethnicity",
    "mainLanguage",
    "englishSpeaker",
    "homeTelephone",
    "workTelephone",
    "mobileTelephone",
    "alternateTelephone",
    "emailAddress",
    "smsConsent",
    "usualGPUserName",
    "careStartDate",
    "registrationType"
})
public class Demographics {

    @XmlElement(name = "Title", required = true)
    protected String title;
    @XmlElement(name = "FirstName", required = true)
    protected String firstName;
    @XmlElement(name = "MiddleNames")
    protected String middleNames;
    @XmlElement(name = "Surname", required = true)
    protected String surname;
    @XmlElement(name = "KnownAs")
    protected String knownAs;
    @XmlElement(name = "DateOfBirth", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dateOfBirth;
    @XmlElement(name = "Sex", required = true)
    protected String sex;
    @XmlElement(name = "MaritalStatus")
    protected Code maritalStatus;
    @XmlElement(name = "Ethnicity")
    protected Code ethnicity;
    @XmlElement(name = "MainLanguage")
    protected Code mainLanguage;
    @XmlElement(name = "EnglishSpeaker", required = true)
    protected String englishSpeaker;
    @XmlElement(name = "HomeTelephone")
    protected String homeTelephone;
    @XmlElement(name = "WorkTelephone")
    protected String workTelephone;
    @XmlElement(name = "MobileTelephone")
    protected String mobileTelephone;
    @XmlElement(name = "AlternateTelephone")
    protected String alternateTelephone;
    @XmlElement(name = "EmailAddress")
    protected String emailAddress;
    @XmlElement(name = "SMSConsent", required = true)
    protected String smsConsent;
    @XmlElement(name = "UsualGPUserName")
    protected String usualGPUserName;
    @XmlElement(name = "CareStartDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar careStartDate;
    @XmlElement(name = "RegistrationType", required = true)
    protected String registrationType;

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the firstName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the value of the firstName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstName(String value) {
        this.firstName = value;
    }

    /**
     * Gets the value of the middleNames property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMiddleNames() {
        return middleNames;
    }

    /**
     * Sets the value of the middleNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMiddleNames(String value) {
        this.middleNames = value;
    }

    /**
     * Gets the value of the surname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Sets the value of the surname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSurname(String value) {
        this.surname = value;
    }

    /**
     * Gets the value of the knownAs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKnownAs() {
        return knownAs;
    }

    /**
     * Sets the value of the knownAs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKnownAs(String value) {
        this.knownAs = value;
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
     * Gets the value of the sex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSex() {
        return sex;
    }

    /**
     * Sets the value of the sex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSex(String value) {
        this.sex = value;
    }

    /**
     * Gets the value of the maritalStatus property.
     * 
     * @return
     *     possible object is
     *     {@link Code }
     *     
     */
    public Code getMaritalStatus() {
        return maritalStatus;
    }

    /**
     * Sets the value of the maritalStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link Code }
     *     
     */
    public void setMaritalStatus(Code value) {
        this.maritalStatus = value;
    }

    /**
     * Gets the value of the ethnicity property.
     * 
     * @return
     *     possible object is
     *     {@link Code }
     *     
     */
    public Code getEthnicity() {
        return ethnicity;
    }

    /**
     * Sets the value of the ethnicity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Code }
     *     
     */
    public void setEthnicity(Code value) {
        this.ethnicity = value;
    }

    /**
     * Gets the value of the mainLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link Code }
     *     
     */
    public Code getMainLanguage() {
        return mainLanguage;
    }

    /**
     * Sets the value of the mainLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Code }
     *     
     */
    public void setMainLanguage(Code value) {
        this.mainLanguage = value;
    }

    /**
     * Gets the value of the englishSpeaker property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnglishSpeaker() {
        return englishSpeaker;
    }

    /**
     * Sets the value of the englishSpeaker property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnglishSpeaker(String value) {
        this.englishSpeaker = value;
    }

    /**
     * Gets the value of the homeTelephone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHomeTelephone() {
        return homeTelephone;
    }

    /**
     * Sets the value of the homeTelephone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHomeTelephone(String value) {
        this.homeTelephone = value;
    }

    /**
     * Gets the value of the workTelephone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkTelephone() {
        return workTelephone;
    }

    /**
     * Sets the value of the workTelephone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkTelephone(String value) {
        this.workTelephone = value;
    }

    /**
     * Gets the value of the mobileTelephone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMobileTelephone() {
        return mobileTelephone;
    }

    /**
     * Sets the value of the mobileTelephone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMobileTelephone(String value) {
        this.mobileTelephone = value;
    }

    /**
     * Gets the value of the alternateTelephone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlternateTelephone() {
        return alternateTelephone;
    }

    /**
     * Sets the value of the alternateTelephone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlternateTelephone(String value) {
        this.alternateTelephone = value;
    }

    /**
     * Gets the value of the emailAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the value of the emailAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailAddress(String value) {
        this.emailAddress = value;
    }

    /**
     * Gets the value of the smsConsent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSMSConsent() {
        return smsConsent;
    }

    /**
     * Sets the value of the smsConsent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSMSConsent(String value) {
        this.smsConsent = value;
    }

    /**
     * Gets the value of the usualGPUserName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsualGPUserName() {
        return usualGPUserName;
    }

    /**
     * Sets the value of the usualGPUserName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsualGPUserName(String value) {
        this.usualGPUserName = value;
    }

    /**
     * Gets the value of the careStartDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCareStartDate() {
        return careStartDate;
    }

    /**
     * Sets the value of the careStartDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCareStartDate(XMLGregorianCalendar value) {
        this.careStartDate = value;
    }

    /**
     * Gets the value of the registrationType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegistrationType() {
        return registrationType;
    }

    /**
     * Sets the value of the registrationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegistrationType(String value) {
        this.registrationType = value;
    }

}


package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegistrationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegistrationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="DateOfBirth" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Sex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PreviousNames" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FamilyName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CallingName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FirstNames" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NhsNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Address" type="{http://www.e-mis.com/emisopen/MedicalRecord}AddressType" minOccurs="0"/>
 *         &lt;element name="HomeTelephone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RegisteredGpID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="UsualGpID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="TradingHaID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="WalkingQuarters" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="Dispensing" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RuralMileage" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="DifficultQuarters" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="ResidentialInstitute" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="BlockedSpecial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RecordsAt" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HospitalNumbers" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DeductedDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DateOfDeath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Dead" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="0"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="DateAdded" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OldNhsNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="WorkTelephone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Mobile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HomeAddress" type="{http://www.e-mis.com/emisopen/MedicalRecord}AddressType" minOccurs="0"/>
 *         &lt;element name="HomeGP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HomeGPCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CHINumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FootpathMiles" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="WaterMiles" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ServiceType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ServiceNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CustomRegistrationFields" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="CustomRegistrationEntry" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="FieldName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="FieldValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="GRONumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UPCINumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MaritalStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MedicationReviewDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ExemptionExpiry" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ScreenMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ReminderSent" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EthnicCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PrisonNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PreviousPrisonNumbers" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PrisonDoctorGP" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="SocialSecurityNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PolicyId" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="Archived" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="SCN" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegistrationType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "dateOfBirth",
    "sex",
    "previousNames",
    "familyName",
    "callingName",
    "firstNames",
    "title",
    "nhsNumber",
    "address",
    "homeTelephone",
    "registeredGpID",
    "usualGpID",
    "tradingHaID",
    "walkingQuarters",
    "dispensing",
    "ruralMileage",
    "difficultQuarters",
    "residentialInstitute",
    "blockedSpecial",
    "recordsAt",
    "hospitalNumbers",
    "deductedDate",
    "dateOfDeath",
    "dead",
    "dateAdded",
    "oldNhsNumber",
    "workTelephone",
    "mobile",
    "email",
    "homeAddress",
    "homeGP",
    "homeGPCode",
    "chiNumber",
    "footpathMiles",
    "waterMiles",
    "serviceType",
    "serviceNumber",
    "customRegistrationFields",
    "groNumber",
    "upciNumber",
    "maritalStatus",
    "medicationReviewDate",
    "exemptionExpiry",
    "screenMessage",
    "reminderSent",
    "ethnicCode",
    "prisonNumber",
    "previousPrisonNumbers",
    "prisonDoctorGP",
    "socialSecurityNumber",
    "policyId",
    "archived",
    "scn"
})
public class RegistrationType
    extends IdentType
{

    @XmlElement(name = "DateOfBirth", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateOfBirth;
    @XmlElement(name = "Sex", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String sex;
    @XmlElement(name = "PreviousNames", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String previousNames;
    @XmlElement(name = "FamilyName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String familyName;
    @XmlElement(name = "CallingName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String callingName;
    @XmlElement(name = "FirstNames", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String firstNames;
    @XmlElement(name = "Title", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String title;
    @XmlElement(name = "NhsNumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String nhsNumber;
    @XmlElement(name = "Address", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected AddressType address;
    @XmlElement(name = "HomeTelephone", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String homeTelephone;
    @XmlElement(name = "RegisteredGpID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType registeredGpID;
    @XmlElement(name = "UsualGpID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType usualGpID;
    @XmlElement(name = "TradingHaID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType tradingHaID;
    @XmlElement(name = "WalkingQuarters", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger walkingQuarters;
    @XmlElement(name = "Dispensing", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dispensing;
    @XmlElement(name = "RuralMileage", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger ruralMileage;
    @XmlElement(name = "DifficultQuarters", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger difficultQuarters;
    @XmlElement(name = "ResidentialInstitute", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String residentialInstitute;
    @XmlElement(name = "BlockedSpecial", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String blockedSpecial;
    @XmlElement(name = "RecordsAt", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String recordsAt;
    @XmlElement(name = "HospitalNumbers", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String hospitalNumbers;
    @XmlElement(name = "DeductedDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String deductedDate;
    @XmlElement(name = "DateOfDeath", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateOfDeath;
    @XmlElement(name = "Dead", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger dead;
    @XmlElement(name = "DateAdded", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateAdded;
    @XmlElement(name = "OldNhsNumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String oldNhsNumber;
    @XmlElement(name = "WorkTelephone", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String workTelephone;
    @XmlElement(name = "Mobile", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String mobile;
    @XmlElement(name = "Email", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String email;
    @XmlElement(name = "HomeAddress", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected AddressType homeAddress;
    @XmlElement(name = "HomeGP", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String homeGP;
    @XmlElement(name = "HomeGPCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String homeGPCode;
    @XmlElement(name = "CHINumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String chiNumber;
    @XmlElement(name = "FootpathMiles", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String footpathMiles;
    @XmlElement(name = "WaterMiles", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String waterMiles;
    @XmlElement(name = "ServiceType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String serviceType;
    @XmlElement(name = "ServiceNumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String serviceNumber;
    @XmlElement(name = "CustomRegistrationFields", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected RegistrationType.CustomRegistrationFields customRegistrationFields;
    @XmlElement(name = "GRONumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String groNumber;
    @XmlElement(name = "UPCINumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String upciNumber;
    @XmlElement(name = "MaritalStatus", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String maritalStatus;
    @XmlElement(name = "MedicationReviewDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String medicationReviewDate;
    @XmlElement(name = "ExemptionExpiry", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String exemptionExpiry;
    @XmlElement(name = "ScreenMessage", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String screenMessage;
    @XmlElement(name = "ReminderSent", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String reminderSent;
    @XmlElement(name = "EthnicCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String ethnicCode;
    @XmlElement(name = "PrisonNumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String prisonNumber;
    @XmlElement(name = "PreviousPrisonNumbers", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String previousPrisonNumbers;
    @XmlElement(name = "PrisonDoctorGP", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType prisonDoctorGP;
    @XmlElement(name = "SocialSecurityNumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String socialSecurityNumber;
    @XmlElement(name = "PolicyId", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger policyId;
    @XmlElement(name = "Archived", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger archived;
    @XmlElement(name = "SCN", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger scn;

    /**
     * Gets the value of the dateOfBirth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the value of the dateOfBirth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateOfBirth(String value) {
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
     * Gets the value of the previousNames property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreviousNames() {
        return previousNames;
    }

    /**
     * Sets the value of the previousNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreviousNames(String value) {
        this.previousNames = value;
    }

    /**
     * Gets the value of the familyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Sets the value of the familyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFamilyName(String value) {
        this.familyName = value;
    }

    /**
     * Gets the value of the callingName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallingName() {
        return callingName;
    }

    /**
     * Sets the value of the callingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallingName(String value) {
        this.callingName = value;
    }

    /**
     * Gets the value of the firstNames property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstNames() {
        return firstNames;
    }

    /**
     * Sets the value of the firstNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstNames(String value) {
        this.firstNames = value;
    }

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
     * Gets the value of the nhsNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNhsNumber() {
        return nhsNumber;
    }

    /**
     * Sets the value of the nhsNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNhsNumber(String value) {
        this.nhsNumber = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link AddressType }
     *     
     */
    public AddressType getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressType }
     *     
     */
    public void setAddress(AddressType value) {
        this.address = value;
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
     * Gets the value of the registeredGpID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getRegisteredGpID() {
        return registeredGpID;
    }

    /**
     * Sets the value of the registeredGpID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setRegisteredGpID(IdentType value) {
        this.registeredGpID = value;
    }

    /**
     * Gets the value of the usualGpID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getUsualGpID() {
        return usualGpID;
    }

    /**
     * Sets the value of the usualGpID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setUsualGpID(IdentType value) {
        this.usualGpID = value;
    }

    /**
     * Gets the value of the tradingHaID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getTradingHaID() {
        return tradingHaID;
    }

    /**
     * Sets the value of the tradingHaID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setTradingHaID(IdentType value) {
        this.tradingHaID = value;
    }

    /**
     * Gets the value of the walkingQuarters property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getWalkingQuarters() {
        return walkingQuarters;
    }

    /**
     * Sets the value of the walkingQuarters property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setWalkingQuarters(BigInteger value) {
        this.walkingQuarters = value;
    }

    /**
     * Gets the value of the dispensing property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDispensing() {
        return dispensing;
    }

    /**
     * Sets the value of the dispensing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDispensing(String value) {
        this.dispensing = value;
    }

    /**
     * Gets the value of the ruralMileage property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRuralMileage() {
        return ruralMileage;
    }

    /**
     * Sets the value of the ruralMileage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRuralMileage(BigInteger value) {
        this.ruralMileage = value;
    }

    /**
     * Gets the value of the difficultQuarters property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDifficultQuarters() {
        return difficultQuarters;
    }

    /**
     * Sets the value of the difficultQuarters property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDifficultQuarters(BigInteger value) {
        this.difficultQuarters = value;
    }

    /**
     * Gets the value of the residentialInstitute property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResidentialInstitute() {
        return residentialInstitute;
    }

    /**
     * Sets the value of the residentialInstitute property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResidentialInstitute(String value) {
        this.residentialInstitute = value;
    }

    /**
     * Gets the value of the blockedSpecial property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBlockedSpecial() {
        return blockedSpecial;
    }

    /**
     * Sets the value of the blockedSpecial property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBlockedSpecial(String value) {
        this.blockedSpecial = value;
    }

    /**
     * Gets the value of the recordsAt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordsAt() {
        return recordsAt;
    }

    /**
     * Sets the value of the recordsAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordsAt(String value) {
        this.recordsAt = value;
    }

    /**
     * Gets the value of the hospitalNumbers property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHospitalNumbers() {
        return hospitalNumbers;
    }

    /**
     * Sets the value of the hospitalNumbers property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHospitalNumbers(String value) {
        this.hospitalNumbers = value;
    }

    /**
     * Gets the value of the deductedDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeductedDate() {
        return deductedDate;
    }

    /**
     * Sets the value of the deductedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeductedDate(String value) {
        this.deductedDate = value;
    }

    /**
     * Gets the value of the dateOfDeath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateOfDeath() {
        return dateOfDeath;
    }

    /**
     * Sets the value of the dateOfDeath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateOfDeath(String value) {
        this.dateOfDeath = value;
    }

    /**
     * Gets the value of the dead property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDead() {
        return dead;
    }

    /**
     * Sets the value of the dead property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDead(BigInteger value) {
        this.dead = value;
    }

    /**
     * Gets the value of the dateAdded property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateAdded() {
        return dateAdded;
    }

    /**
     * Sets the value of the dateAdded property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateAdded(String value) {
        this.dateAdded = value;
    }

    /**
     * Gets the value of the oldNhsNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldNhsNumber() {
        return oldNhsNumber;
    }

    /**
     * Sets the value of the oldNhsNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldNhsNumber(String value) {
        this.oldNhsNumber = value;
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
     * Gets the value of the mobile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * Sets the value of the mobile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMobile(String value) {
        this.mobile = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the homeAddress property.
     * 
     * @return
     *     possible object is
     *     {@link AddressType }
     *     
     */
    public AddressType getHomeAddress() {
        return homeAddress;
    }

    /**
     * Sets the value of the homeAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressType }
     *     
     */
    public void setHomeAddress(AddressType value) {
        this.homeAddress = value;
    }

    /**
     * Gets the value of the homeGP property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHomeGP() {
        return homeGP;
    }

    /**
     * Sets the value of the homeGP property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHomeGP(String value) {
        this.homeGP = value;
    }

    /**
     * Gets the value of the homeGPCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHomeGPCode() {
        return homeGPCode;
    }

    /**
     * Sets the value of the homeGPCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHomeGPCode(String value) {
        this.homeGPCode = value;
    }

    /**
     * Gets the value of the chiNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCHINumber() {
        return chiNumber;
    }

    /**
     * Sets the value of the chiNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCHINumber(String value) {
        this.chiNumber = value;
    }

    /**
     * Gets the value of the footpathMiles property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFootpathMiles() {
        return footpathMiles;
    }

    /**
     * Sets the value of the footpathMiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFootpathMiles(String value) {
        this.footpathMiles = value;
    }

    /**
     * Gets the value of the waterMiles property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWaterMiles() {
        return waterMiles;
    }

    /**
     * Sets the value of the waterMiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWaterMiles(String value) {
        this.waterMiles = value;
    }

    /**
     * Gets the value of the serviceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Sets the value of the serviceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceType(String value) {
        this.serviceType = value;
    }

    /**
     * Gets the value of the serviceNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceNumber() {
        return serviceNumber;
    }

    /**
     * Sets the value of the serviceNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceNumber(String value) {
        this.serviceNumber = value;
    }

    /**
     * Gets the value of the customRegistrationFields property.
     * 
     * @return
     *     possible object is
     *     {@link RegistrationType.CustomRegistrationFields }
     *     
     */
    public RegistrationType.CustomRegistrationFields getCustomRegistrationFields() {
        return customRegistrationFields;
    }

    /**
     * Sets the value of the customRegistrationFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistrationType.CustomRegistrationFields }
     *     
     */
    public void setCustomRegistrationFields(RegistrationType.CustomRegistrationFields value) {
        this.customRegistrationFields = value;
    }

    /**
     * Gets the value of the groNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGRONumber() {
        return groNumber;
    }

    /**
     * Sets the value of the groNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGRONumber(String value) {
        this.groNumber = value;
    }

    /**
     * Gets the value of the upciNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUPCINumber() {
        return upciNumber;
    }

    /**
     * Sets the value of the upciNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUPCINumber(String value) {
        this.upciNumber = value;
    }

    /**
     * Gets the value of the maritalStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaritalStatus() {
        return maritalStatus;
    }

    /**
     * Sets the value of the maritalStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaritalStatus(String value) {
        this.maritalStatus = value;
    }

    /**
     * Gets the value of the medicationReviewDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMedicationReviewDate() {
        return medicationReviewDate;
    }

    /**
     * Sets the value of the medicationReviewDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMedicationReviewDate(String value) {
        this.medicationReviewDate = value;
    }

    /**
     * Gets the value of the exemptionExpiry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExemptionExpiry() {
        return exemptionExpiry;
    }

    /**
     * Sets the value of the exemptionExpiry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExemptionExpiry(String value) {
        this.exemptionExpiry = value;
    }

    /**
     * Gets the value of the screenMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScreenMessage() {
        return screenMessage;
    }

    /**
     * Sets the value of the screenMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScreenMessage(String value) {
        this.screenMessage = value;
    }

    /**
     * Gets the value of the reminderSent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReminderSent() {
        return reminderSent;
    }

    /**
     * Sets the value of the reminderSent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReminderSent(String value) {
        this.reminderSent = value;
    }

    /**
     * Gets the value of the ethnicCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEthnicCode() {
        return ethnicCode;
    }

    /**
     * Sets the value of the ethnicCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEthnicCode(String value) {
        this.ethnicCode = value;
    }

    /**
     * Gets the value of the prisonNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrisonNumber() {
        return prisonNumber;
    }

    /**
     * Sets the value of the prisonNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrisonNumber(String value) {
        this.prisonNumber = value;
    }

    /**
     * Gets the value of the previousPrisonNumbers property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreviousPrisonNumbers() {
        return previousPrisonNumbers;
    }

    /**
     * Sets the value of the previousPrisonNumbers property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreviousPrisonNumbers(String value) {
        this.previousPrisonNumbers = value;
    }

    /**
     * Gets the value of the prisonDoctorGP property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getPrisonDoctorGP() {
        return prisonDoctorGP;
    }

    /**
     * Sets the value of the prisonDoctorGP property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setPrisonDoctorGP(IdentType value) {
        this.prisonDoctorGP = value;
    }

    /**
     * Gets the value of the socialSecurityNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    /**
     * Sets the value of the socialSecurityNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSocialSecurityNumber(String value) {
        this.socialSecurityNumber = value;
    }

    /**
     * Gets the value of the policyId property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPolicyId() {
        return policyId;
    }

    /**
     * Sets the value of the policyId property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPolicyId(BigInteger value) {
        this.policyId = value;
    }

    /**
     * Gets the value of the archived property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getArchived() {
        return archived;
    }

    /**
     * Sets the value of the archived property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setArchived(BigInteger value) {
        this.archived = value;
    }

    /**
     * Gets the value of the scn property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSCN() {
        return scn;
    }

    /**
     * Sets the value of the scn property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSCN(BigInteger value) {
        this.scn = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="CustomRegistrationEntry" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="FieldName" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="FieldValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "customRegistrationEntry"
    })
    public static class CustomRegistrationFields {

        @XmlElement(name = "CustomRegistrationEntry", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected List<RegistrationType.CustomRegistrationFields.CustomRegistrationEntry> customRegistrationEntry;

        /**
         * Gets the value of the customRegistrationEntry property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the customRegistrationEntry property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCustomRegistrationEntry().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link RegistrationType.CustomRegistrationFields.CustomRegistrationEntry }
         * 
         * 
         */
        public List<RegistrationType.CustomRegistrationFields.CustomRegistrationEntry> getCustomRegistrationEntry() {
            if (customRegistrationEntry == null) {
                customRegistrationEntry = new ArrayList<RegistrationType.CustomRegistrationFields.CustomRegistrationEntry>();
            }
            return this.customRegistrationEntry;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="FieldName" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="FieldValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "fieldName",
            "fieldValue"
        })
        public static class CustomRegistrationEntry {

            @XmlElement(name = "FieldName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
            protected String fieldName;
            @XmlElement(name = "FieldValue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String fieldValue;

            /**
             * Gets the value of the fieldName property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFieldName() {
                return fieldName;
            }

            /**
             * Sets the value of the fieldName property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFieldName(String value) {
                this.fieldName = value;
            }

            /**
             * Gets the value of the fieldValue property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFieldValue() {
                return fieldValue;
            }

            /**
             * Sets the value of the fieldValue property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFieldValue(String value) {
                this.fieldValue = value;
            }

        }

    }

}

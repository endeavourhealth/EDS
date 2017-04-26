
package org.endeavourhealth.transform.adastra.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


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
 *         &lt;element name="adastraCaseReference">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="40"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="adastraCaseNumber">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="20"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="patient">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="firstName">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;maxLength value="50"/>
 *                         &lt;minLength value="1"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="lastName">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;minLength value="1"/>
 *                         &lt;maxLength value="50"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="dateOfBirth" type="{http://www.adastra.com/dataExport}dateOfBirthType" minOccurs="0"/>
 *                   &lt;element name="gender" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="Male"/>
 *                         &lt;enumeration value="Female"/>
 *                         &lt;enumeration value="Unknown"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="nationalNumber" type="{http://www.adastra.com/dataExport}nationalNumberType" minOccurs="0"/>
 *                   &lt;element name="address" type="{http://www.adastra.com/dataExport}patientAddressType" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="phone" type="{http://www.adastra.com/dataExport}patientPhoneNumberType" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="gpRegistration">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="registrationStatus">
 *                               &lt;simpleType>
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                   &lt;maxLength value="50"/>
 *                                 &lt;/restriction>
 *                               &lt;/simpleType>
 *                             &lt;/element>
 *                             &lt;element name="gpNationalCode" minOccurs="0">
 *                               &lt;simpleType>
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                   &lt;maxLength value="10"/>
 *                                   &lt;minLength value="1"/>
 *                                 &lt;/restriction>
 *                               &lt;/simpleType>
 *                             &lt;/element>
 *                             &lt;element name="surgeryNationalCode" minOccurs="0">
 *                               &lt;simpleType>
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                   &lt;maxLength value="10"/>
 *                                   &lt;minLength value="1"/>
 *                                 &lt;/restriction>
 *                               &lt;/simpleType>
 *                             &lt;/element>
 *                             &lt;element name="surgeryPostcode" minOccurs="0">
 *                               &lt;simpleType>
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                   &lt;minLength value="1"/>
 *                                   &lt;maxLength value="15"/>
 *                                 &lt;/restriction>
 *                               &lt;/simpleType>
 *                             &lt;/element>
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
 *         &lt;element name="presentingCondition">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="symptoms" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="comments" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="priority" type="{http://www.adastra.com/dataExport}codedItem"/>
 *         &lt;element name="caseType" type="{http://www.adastra.com/dataExport}codedItem"/>
 *         &lt;element name="caseStatus">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="0"/>
 *               &lt;maxLength value="10"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="activeDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="completedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="returnPhone" type="{http://www.adastra.com/dataExport}phoneNumberType" minOccurs="0"/>
 *         &lt;element name="callerPhone" type="{http://www.adastra.com/dataExport}phoneNumberType" minOccurs="0"/>
 *         &lt;element name="latestAppointment" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="appointmentTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="location">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;maxLength value="50"/>
 *                         &lt;minLength value="0"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="status">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="None"/>
 *                         &lt;enumeration value="Arrived"/>
 *                         &lt;enumeration value="DidNotAttend"/>
 *                         &lt;enumeration value="Cancelled"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="questions" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="consultation" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="endTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="consultationBy">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="name">
 *                               &lt;simpleType>
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                   &lt;minLength value="0"/>
 *                                   &lt;maxLength value="50"/>
 *                                 &lt;/restriction>
 *                               &lt;/simpleType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                           &lt;attribute name="providerType">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;maxLength value="10"/>
 *                                 &lt;minLength value="1"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="location" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;minLength value="0"/>
 *                         &lt;maxLength value="50"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="summary" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="medicalHistory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="eventOutcome" type="{http://www.adastra.com/dataExport}codedItem" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="clinicalCode" type="{http://www.adastra.com/dataExport}codedItem" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="specialNote" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="reviewDate" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *                   &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="outcome" type="{http://www.adastra.com/dataExport}codedItem" maxOccurs="unbounded" minOccurs="0"/>
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
    "adastraCaseReference",
    "adastraCaseNumber",
    "patient",
    "presentingCondition",
    "priority",
    "caseType",
    "caseStatus",
    "activeDate",
    "completedDate",
    "returnPhone",
    "callerPhone",
    "latestAppointment",
    "questions",
    "consultation",
    "specialNote",
    "outcome"
})
@XmlRootElement(name = "adastraCaseDataExport", namespace = "http://www.adastra.com/dataExport")
public class AdastraCaseDataExport {

    @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
    protected String adastraCaseReference;
    @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
    protected String adastraCaseNumber;
    @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
    protected AdastraCaseDataExport.Patient patient;
    @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
    protected AdastraCaseDataExport.PresentingCondition presentingCondition;
    @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
    protected CodedItem priority;
    @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
    protected CodedItem caseType;
    @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
    protected String caseStatus;
    @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar activeDate;
    @XmlElement(namespace = "http://www.adastra.com/dataExport")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar completedDate;
    @XmlElement(namespace = "http://www.adastra.com/dataExport")
    protected PhoneNumberType returnPhone;
    @XmlElement(namespace = "http://www.adastra.com/dataExport")
    protected PhoneNumberType callerPhone;
    @XmlElement(namespace = "http://www.adastra.com/dataExport")
    protected AdastraCaseDataExport.LatestAppointment latestAppointment;
    @XmlElement(namespace = "http://www.adastra.com/dataExport")
    protected String questions;
    @XmlElement(namespace = "http://www.adastra.com/dataExport")
    protected List<AdastraCaseDataExport.Consultation> consultation;
    @XmlElement(namespace = "http://www.adastra.com/dataExport")
    protected List<AdastraCaseDataExport.SpecialNote> specialNote;
    @XmlElement(namespace = "http://www.adastra.com/dataExport")
    protected List<CodedItem> outcome;

    /**
     * Gets the value of the adastraCaseReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdastraCaseReference() {
        return adastraCaseReference;
    }

    /**
     * Sets the value of the adastraCaseReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdastraCaseReference(String value) {
        this.adastraCaseReference = value;
    }

    /**
     * Gets the value of the adastraCaseNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdastraCaseNumber() {
        return adastraCaseNumber;
    }

    /**
     * Sets the value of the adastraCaseNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdastraCaseNumber(String value) {
        this.adastraCaseNumber = value;
    }

    /**
     * Gets the value of the patient property.
     * 
     * @return
     *     possible object is
     *     {@link AdastraCaseDataExport.Patient }
     *     
     */
    public AdastraCaseDataExport.Patient getPatient() {
        return patient;
    }

    /**
     * Sets the value of the patient property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdastraCaseDataExport.Patient }
     *     
     */
    public void setPatient(AdastraCaseDataExport.Patient value) {
        this.patient = value;
    }

    /**
     * Gets the value of the presentingCondition property.
     * 
     * @return
     *     possible object is
     *     {@link AdastraCaseDataExport.PresentingCondition }
     *     
     */
    public AdastraCaseDataExport.PresentingCondition getPresentingCondition() {
        return presentingCondition;
    }

    /**
     * Sets the value of the presentingCondition property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdastraCaseDataExport.PresentingCondition }
     *     
     */
    public void setPresentingCondition(AdastraCaseDataExport.PresentingCondition value) {
        this.presentingCondition = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     * @return
     *     possible object is
     *     {@link CodedItem }
     *     
     */
    public CodedItem getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     * @param value
     *     allowed object is
     *     {@link CodedItem }
     *     
     */
    public void setPriority(CodedItem value) {
        this.priority = value;
    }

    /**
     * Gets the value of the caseType property.
     * 
     * @return
     *     possible object is
     *     {@link CodedItem }
     *     
     */
    public CodedItem getCaseType() {
        return caseType;
    }

    /**
     * Sets the value of the caseType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CodedItem }
     *     
     */
    public void setCaseType(CodedItem value) {
        this.caseType = value;
    }

    /**
     * Gets the value of the caseStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCaseStatus() {
        return caseStatus;
    }

    /**
     * Sets the value of the caseStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCaseStatus(String value) {
        this.caseStatus = value;
    }

    /**
     * Gets the value of the activeDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getActiveDate() {
        return activeDate;
    }

    /**
     * Sets the value of the activeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setActiveDate(XMLGregorianCalendar value) {
        this.activeDate = value;
    }

    /**
     * Gets the value of the completedDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCompletedDate() {
        return completedDate;
    }

    /**
     * Sets the value of the completedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCompletedDate(XMLGregorianCalendar value) {
        this.completedDate = value;
    }

    /**
     * Gets the value of the returnPhone property.
     * 
     * @return
     *     possible object is
     *     {@link PhoneNumberType }
     *     
     */
    public PhoneNumberType getReturnPhone() {
        return returnPhone;
    }

    /**
     * Sets the value of the returnPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link PhoneNumberType }
     *     
     */
    public void setReturnPhone(PhoneNumberType value) {
        this.returnPhone = value;
    }

    /**
     * Gets the value of the callerPhone property.
     * 
     * @return
     *     possible object is
     *     {@link PhoneNumberType }
     *     
     */
    public PhoneNumberType getCallerPhone() {
        return callerPhone;
    }

    /**
     * Sets the value of the callerPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link PhoneNumberType }
     *     
     */
    public void setCallerPhone(PhoneNumberType value) {
        this.callerPhone = value;
    }

    /**
     * Gets the value of the latestAppointment property.
     * 
     * @return
     *     possible object is
     *     {@link AdastraCaseDataExport.LatestAppointment }
     *     
     */
    public AdastraCaseDataExport.LatestAppointment getLatestAppointment() {
        return latestAppointment;
    }

    /**
     * Sets the value of the latestAppointment property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdastraCaseDataExport.LatestAppointment }
     *     
     */
    public void setLatestAppointment(AdastraCaseDataExport.LatestAppointment value) {
        this.latestAppointment = value;
    }

    /**
     * Gets the value of the questions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuestions() {
        return questions;
    }

    /**
     * Sets the value of the questions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuestions(String value) {
        this.questions = value;
    }

    /**
     * Gets the value of the consultation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the consultation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConsultation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AdastraCaseDataExport.Consultation }
     * 
     * 
     */
    public List<AdastraCaseDataExport.Consultation> getConsultation() {
        if (consultation == null) {
            consultation = new ArrayList<AdastraCaseDataExport.Consultation>();
        }
        return this.consultation;
    }

    /**
     * Gets the value of the specialNote property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the specialNote property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpecialNote().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AdastraCaseDataExport.SpecialNote }
     * 
     * 
     */
    public List<AdastraCaseDataExport.SpecialNote> getSpecialNote() {
        if (specialNote == null) {
            specialNote = new ArrayList<AdastraCaseDataExport.SpecialNote>();
        }
        return this.specialNote;
    }

    /**
     * Gets the value of the outcome property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the outcome property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOutcome().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CodedItem }
     * 
     * 
     */
    public List<CodedItem> getOutcome() {
        if (outcome == null) {
            outcome = new ArrayList<CodedItem>();
        }
        return this.outcome;
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
     *         &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="endTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="consultationBy">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="name">
     *                     &lt;simpleType>
     *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                         &lt;minLength value="0"/>
     *                         &lt;maxLength value="50"/>
     *                       &lt;/restriction>
     *                     &lt;/simpleType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *                 &lt;attribute name="providerType">
     *                   &lt;simpleType>
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                       &lt;maxLength value="10"/>
     *                       &lt;minLength value="1"/>
     *                     &lt;/restriction>
     *                   &lt;/simpleType>
     *                 &lt;/attribute>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="location" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;minLength value="0"/>
     *               &lt;maxLength value="50"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="summary" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="medicalHistory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="eventOutcome" type="{http://www.adastra.com/dataExport}codedItem" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="clinicalCode" type="{http://www.adastra.com/dataExport}codedItem" maxOccurs="unbounded" minOccurs="0"/>
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
        "startTime",
        "endTime",
        "consultationBy",
        "location",
        "summary",
        "medicalHistory",
        "eventOutcome",
        "clinicalCode"
    })
    public static class Consultation {

        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar startTime;
        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar endTime;
        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        protected AdastraCaseDataExport.Consultation.ConsultationBy consultationBy;
        @XmlElement(namespace = "http://www.adastra.com/dataExport")
        protected String location;
        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        protected String summary;
        @XmlElement(namespace = "http://www.adastra.com/dataExport")
        protected String medicalHistory;
        @XmlElement(namespace = "http://www.adastra.com/dataExport")
        protected List<CodedItem> eventOutcome;
        @XmlElement(namespace = "http://www.adastra.com/dataExport")
        protected List<CodedItem> clinicalCode;

        /**
         * Gets the value of the startTime property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getStartTime() {
            return startTime;
        }

        /**
         * Sets the value of the startTime property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setStartTime(XMLGregorianCalendar value) {
            this.startTime = value;
        }

        /**
         * Gets the value of the endTime property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getEndTime() {
            return endTime;
        }

        /**
         * Sets the value of the endTime property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setEndTime(XMLGregorianCalendar value) {
            this.endTime = value;
        }

        /**
         * Gets the value of the consultationBy property.
         * 
         * @return
         *     possible object is
         *     {@link AdastraCaseDataExport.Consultation.ConsultationBy }
         *     
         */
        public AdastraCaseDataExport.Consultation.ConsultationBy getConsultationBy() {
            return consultationBy;
        }

        /**
         * Sets the value of the consultationBy property.
         * 
         * @param value
         *     allowed object is
         *     {@link AdastraCaseDataExport.Consultation.ConsultationBy }
         *     
         */
        public void setConsultationBy(AdastraCaseDataExport.Consultation.ConsultationBy value) {
            this.consultationBy = value;
        }

        /**
         * Gets the value of the location property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLocation() {
            return location;
        }

        /**
         * Sets the value of the location property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLocation(String value) {
            this.location = value;
        }

        /**
         * Gets the value of the summary property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSummary() {
            return summary;
        }

        /**
         * Sets the value of the summary property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSummary(String value) {
            this.summary = value;
        }

        /**
         * Gets the value of the medicalHistory property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMedicalHistory() {
            return medicalHistory;
        }

        /**
         * Sets the value of the medicalHistory property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMedicalHistory(String value) {
            this.medicalHistory = value;
        }

        /**
         * Gets the value of the eventOutcome property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the eventOutcome property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEventOutcome().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CodedItem }
         * 
         * 
         */
        public List<CodedItem> getEventOutcome() {
            if (eventOutcome == null) {
                eventOutcome = new ArrayList<CodedItem>();
            }
            return this.eventOutcome;
        }

        /**
         * Gets the value of the clinicalCode property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the clinicalCode property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getClinicalCode().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CodedItem }
         * 
         * 
         */
        public List<CodedItem> getClinicalCode() {
            if (clinicalCode == null) {
                clinicalCode = new ArrayList<CodedItem>();
            }
            return this.clinicalCode;
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
         *         &lt;element name="name">
         *           &lt;simpleType>
         *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *               &lt;minLength value="0"/>
         *               &lt;maxLength value="50"/>
         *             &lt;/restriction>
         *           &lt;/simpleType>
         *         &lt;/element>
         *       &lt;/sequence>
         *       &lt;attribute name="providerType">
         *         &lt;simpleType>
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *             &lt;maxLength value="10"/>
         *             &lt;minLength value="1"/>
         *           &lt;/restriction>
         *         &lt;/simpleType>
         *       &lt;/attribute>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "name"
        })
        public static class ConsultationBy {

            @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
            protected String name;
            @XmlAttribute(name = "providerType")
            protected String providerType;

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

            /**
             * Gets the value of the providerType property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getProviderType() {
                return providerType;
            }

            /**
             * Sets the value of the providerType property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setProviderType(String value) {
                this.providerType = value;
            }

        }

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
     *         &lt;element name="appointmentTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="location">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;maxLength value="50"/>
     *               &lt;minLength value="0"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="status">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="None"/>
     *               &lt;enumeration value="Arrived"/>
     *               &lt;enumeration value="DidNotAttend"/>
     *               &lt;enumeration value="Cancelled"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
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
        "appointmentTime",
        "location",
        "status"
    })
    public static class LatestAppointment {

        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar appointmentTime;
        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        protected String location;
        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        protected String status;

        /**
         * Gets the value of the appointmentTime property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getAppointmentTime() {
            return appointmentTime;
        }

        /**
         * Sets the value of the appointmentTime property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setAppointmentTime(XMLGregorianCalendar value) {
            this.appointmentTime = value;
        }

        /**
         * Gets the value of the location property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLocation() {
            return location;
        }

        /**
         * Sets the value of the location property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLocation(String value) {
            this.location = value;
        }

        /**
         * Gets the value of the status property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getStatus() {
            return status;
        }

        /**
         * Sets the value of the status property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setStatus(String value) {
            this.status = value;
        }

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
     *         &lt;element name="firstName">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;maxLength value="50"/>
     *               &lt;minLength value="1"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="lastName">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;minLength value="1"/>
     *               &lt;maxLength value="50"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="dateOfBirth" type="{http://www.adastra.com/dataExport}dateOfBirthType" minOccurs="0"/>
     *         &lt;element name="gender" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="Male"/>
     *               &lt;enumeration value="Female"/>
     *               &lt;enumeration value="Unknown"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="nationalNumber" type="{http://www.adastra.com/dataExport}nationalNumberType" minOccurs="0"/>
     *         &lt;element name="address" type="{http://www.adastra.com/dataExport}patientAddressType" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="phone" type="{http://www.adastra.com/dataExport}patientPhoneNumberType" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="gpRegistration">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="registrationStatus">
     *                     &lt;simpleType>
     *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                         &lt;maxLength value="50"/>
     *                       &lt;/restriction>
     *                     &lt;/simpleType>
     *                   &lt;/element>
     *                   &lt;element name="gpNationalCode" minOccurs="0">
     *                     &lt;simpleType>
     *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                         &lt;maxLength value="10"/>
     *                         &lt;minLength value="1"/>
     *                       &lt;/restriction>
     *                     &lt;/simpleType>
     *                   &lt;/element>
     *                   &lt;element name="surgeryNationalCode" minOccurs="0">
     *                     &lt;simpleType>
     *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                         &lt;maxLength value="10"/>
     *                         &lt;minLength value="1"/>
     *                       &lt;/restriction>
     *                     &lt;/simpleType>
     *                   &lt;/element>
     *                   &lt;element name="surgeryPostcode" minOccurs="0">
     *                     &lt;simpleType>
     *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                         &lt;minLength value="1"/>
     *                         &lt;maxLength value="15"/>
     *                       &lt;/restriction>
     *                     &lt;/simpleType>
     *                   &lt;/element>
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
        "firstName",
        "lastName",
        "dateOfBirth",
        "gender",
        "nationalNumber",
        "address",
        "phone",
        "gpRegistration"
    })
    public static class Patient {

        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        protected String firstName;
        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        protected String lastName;
        @XmlElement(namespace = "http://www.adastra.com/dataExport")
        protected DateOfBirthType dateOfBirth;
        @XmlElement(namespace = "http://www.adastra.com/dataExport")
        protected String gender;
        @XmlElement(namespace = "http://www.adastra.com/dataExport")
        protected NationalNumberType nationalNumber;
        @XmlElement(namespace = "http://www.adastra.com/dataExport")
        protected List<PatientAddressType> address;
        @XmlElement(namespace = "http://www.adastra.com/dataExport")
        protected List<PatientPhoneNumberType> phone;
        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        protected AdastraCaseDataExport.Patient.GpRegistration gpRegistration;

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
         * Gets the value of the lastName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLastName() {
            return lastName;
        }

        /**
         * Sets the value of the lastName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLastName(String value) {
            this.lastName = value;
        }

        /**
         * Gets the value of the dateOfBirth property.
         * 
         * @return
         *     possible object is
         *     {@link DateOfBirthType }
         *     
         */
        public DateOfBirthType getDateOfBirth() {
            return dateOfBirth;
        }

        /**
         * Sets the value of the dateOfBirth property.
         * 
         * @param value
         *     allowed object is
         *     {@link DateOfBirthType }
         *     
         */
        public void setDateOfBirth(DateOfBirthType value) {
            this.dateOfBirth = value;
        }

        /**
         * Gets the value of the gender property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getGender() {
            return gender;
        }

        /**
         * Sets the value of the gender property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setGender(String value) {
            this.gender = value;
        }

        /**
         * Gets the value of the nationalNumber property.
         * 
         * @return
         *     possible object is
         *     {@link NationalNumberType }
         *     
         */
        public NationalNumberType getNationalNumber() {
            return nationalNumber;
        }

        /**
         * Sets the value of the nationalNumber property.
         * 
         * @param value
         *     allowed object is
         *     {@link NationalNumberType }
         *     
         */
        public void setNationalNumber(NationalNumberType value) {
            this.nationalNumber = value;
        }

        /**
         * Gets the value of the address property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the address property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAddress().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link PatientAddressType }
         * 
         * 
         */
        public List<PatientAddressType> getAddress() {
            if (address == null) {
                address = new ArrayList<PatientAddressType>();
            }
            return this.address;
        }

        /**
         * Gets the value of the phone property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the phone property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPhone().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link PatientPhoneNumberType }
         * 
         * 
         */
        public List<PatientPhoneNumberType> getPhone() {
            if (phone == null) {
                phone = new ArrayList<PatientPhoneNumberType>();
            }
            return this.phone;
        }

        /**
         * Gets the value of the gpRegistration property.
         * 
         * @return
         *     possible object is
         *     {@link AdastraCaseDataExport.Patient.GpRegistration }
         *     
         */
        public AdastraCaseDataExport.Patient.GpRegistration getGpRegistration() {
            return gpRegistration;
        }

        /**
         * Sets the value of the gpRegistration property.
         * 
         * @param value
         *     allowed object is
         *     {@link AdastraCaseDataExport.Patient.GpRegistration }
         *     
         */
        public void setGpRegistration(AdastraCaseDataExport.Patient.GpRegistration value) {
            this.gpRegistration = value;
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
         *         &lt;element name="registrationStatus">
         *           &lt;simpleType>
         *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *               &lt;maxLength value="50"/>
         *             &lt;/restriction>
         *           &lt;/simpleType>
         *         &lt;/element>
         *         &lt;element name="gpNationalCode" minOccurs="0">
         *           &lt;simpleType>
         *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *               &lt;maxLength value="10"/>
         *               &lt;minLength value="1"/>
         *             &lt;/restriction>
         *           &lt;/simpleType>
         *         &lt;/element>
         *         &lt;element name="surgeryNationalCode" minOccurs="0">
         *           &lt;simpleType>
         *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *               &lt;maxLength value="10"/>
         *               &lt;minLength value="1"/>
         *             &lt;/restriction>
         *           &lt;/simpleType>
         *         &lt;/element>
         *         &lt;element name="surgeryPostcode" minOccurs="0">
         *           &lt;simpleType>
         *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *               &lt;minLength value="1"/>
         *               &lt;maxLength value="15"/>
         *             &lt;/restriction>
         *           &lt;/simpleType>
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
            "registrationStatus",
            "gpNationalCode",
            "surgeryNationalCode",
            "surgeryPostcode"
        })
        public static class GpRegistration {

            @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
            protected String registrationStatus;
            @XmlElement(namespace = "http://www.adastra.com/dataExport")
            protected String gpNationalCode;
            @XmlElement(namespace = "http://www.adastra.com/dataExport")
            protected String surgeryNationalCode;
            @XmlElement(namespace = "http://www.adastra.com/dataExport")
            protected String surgeryPostcode;

            /**
             * Gets the value of the registrationStatus property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getRegistrationStatus() {
                return registrationStatus;
            }

            /**
             * Sets the value of the registrationStatus property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setRegistrationStatus(String value) {
                this.registrationStatus = value;
            }

            /**
             * Gets the value of the gpNationalCode property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getGpNationalCode() {
                return gpNationalCode;
            }

            /**
             * Sets the value of the gpNationalCode property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setGpNationalCode(String value) {
                this.gpNationalCode = value;
            }

            /**
             * Gets the value of the surgeryNationalCode property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getSurgeryNationalCode() {
                return surgeryNationalCode;
            }

            /**
             * Sets the value of the surgeryNationalCode property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setSurgeryNationalCode(String value) {
                this.surgeryNationalCode = value;
            }

            /**
             * Gets the value of the surgeryPostcode property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getSurgeryPostcode() {
                return surgeryPostcode;
            }

            /**
             * Sets the value of the surgeryPostcode property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setSurgeryPostcode(String value) {
                this.surgeryPostcode = value;
            }

        }

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
     *         &lt;element name="symptoms" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="comments" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
        "symptoms",
        "comments"
    })
    public static class PresentingCondition {

        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        protected String symptoms;
        @XmlElement(namespace = "http://www.adastra.com/dataExport")
        protected String comments;

        /**
         * Gets the value of the symptoms property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSymptoms() {
            return symptoms;
        }

        /**
         * Sets the value of the symptoms property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSymptoms(String value) {
            this.symptoms = value;
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
     *         &lt;element name="reviewDate" type="{http://www.w3.org/2001/XMLSchema}date"/>
     *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "reviewDate",
        "text"
    })
    public static class SpecialNote {

        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar reviewDate;
        @XmlElement(namespace = "http://www.adastra.com/dataExport", required = true)
        protected String text;

        /**
         * Gets the value of the reviewDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getReviewDate() {
            return reviewDate;
        }

        /**
         * Sets the value of the reviewDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setReviewDate(XMLGregorianCalendar value) {
            this.reviewDate = value;
        }

        /**
         * Gets the value of the text property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getText() {
            return text;
        }

        /**
         * Sets the value of the text property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setText(String value) {
            this.text = value;
        }

    }

}

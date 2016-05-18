
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MedicalRecordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MedicalRecordType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Registration" type="{http://www.e-mis.com/emisopen/MedicalRecord}RegistrationType" minOccurs="0"/>
 *         &lt;element name="RegistrationChangeHistory" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="RegistrationEntry" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="FieldName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="FieldValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="DateChanged" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="UserID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType"/>
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
 *         &lt;element name="MiscellaneousData" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="MedicationMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="AutomaticWeekNumber" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="EventList" type="{http://www.e-mis.com/emisopen/MedicalRecord}EventListType" minOccurs="0"/>
 *         &lt;element name="ConsultationList" type="{http://www.e-mis.com/emisopen/MedicalRecord}ConsultationListType" minOccurs="0"/>
 *         &lt;element name="InvestigationList" type="{http://www.e-mis.com/emisopen/MedicalRecord}InvestigationListType" minOccurs="0"/>
 *         &lt;element name="ReferralList" type="{http://www.e-mis.com/emisopen/MedicalRecord}ReferralListType" minOccurs="0"/>
 *         &lt;element name="AllergyList" type="{http://www.e-mis.com/emisopen/MedicalRecord}AllergyListType" minOccurs="0"/>
 *         &lt;element name="MedicationList" type="{http://www.e-mis.com/emisopen/MedicalRecord}MedicationListType" minOccurs="0"/>
 *         &lt;element name="IssueList" type="{http://www.e-mis.com/emisopen/MedicalRecord}IssueListType" minOccurs="0"/>
 *         &lt;element name="AttachmentList" type="{http://www.e-mis.com/emisopen/MedicalRecord}AttachmentListType" minOccurs="0"/>
 *         &lt;element name="DiaryList" type="{http://www.e-mis.com/emisopen/MedicalRecord}DiaryListType" minOccurs="0"/>
 *         &lt;element name="AlertList" type="{http://www.e-mis.com/emisopen/MedicalRecord}AlertListType" minOccurs="0"/>
 *         &lt;element name="RegistrationStatus" type="{http://www.e-mis.com/emisopen/MedicalRecord}RegistrationStatusType" minOccurs="0"/>
 *         &lt;element name="EDIPathologyReportList" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDIPathologyReportListType" minOccurs="0"/>
 *         &lt;element name="TestRequestHeaderList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="TestRequestHeader" type="{http://www.e-mis.com/emisopen/MedicalRecord}TestRequestHeaderType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="NoteList" type="{http://www.e-mis.com/emisopen/MedicalRecord}NoteListType" minOccurs="0"/>
 *         &lt;element name="AppointmentList" type="{http://www.e-mis.com/emisopen/MedicalRecord}AppointmentListType" minOccurs="0"/>
 *         &lt;element name="PeopleList" type="{http://www.e-mis.com/emisopen/MedicalRecord}PeopleListType" minOccurs="0"/>
 *         &lt;element name="LocationList" type="{http://www.e-mis.com/emisopen/MedicalRecord}LocationListType" minOccurs="0"/>
 *         &lt;element name="LocationTypeList" type="{http://www.e-mis.com/emisopen/MedicalRecord}LocationTypeListType" minOccurs="0"/>
 *         &lt;element name="Originator" type="{http://www.e-mis.com/emisopen/MedicalRecord}OriginatorType" minOccurs="0"/>
 *         &lt;element name="RecipientID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="MessageInformation" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="DateCreated" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="TimeCreated" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="MessagePurpose" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;choice>
 *                             &lt;element name="Post">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="PostType">
 *                                         &lt;simpleType>
 *                                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                                             &lt;enumeration value="1"/>
 *                                             &lt;enumeration value="2"/>
 *                                           &lt;/restriction>
 *                                         &lt;/simpleType>
 *                                       &lt;/element>
 *                                       &lt;element name="SessionID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="Get">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="MRGetType">
 *                                         &lt;simpleType>
 *                                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                                             &lt;enumeration value="1"/>
 *                                             &lt;enumeration value="2"/>
 *                                             &lt;enumeration value="3"/>
 *                                             &lt;enumeration value="4"/>
 *                                           &lt;/restriction>
 *                                         &lt;/simpleType>
 *                                       &lt;/element>
 *                                       &lt;element name="DataSet" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                                       &lt;element name="SessionID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/choice>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="MessagingError" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="ErrorString" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="Errornumber" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
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
 *         &lt;element name="PolicyList" type="{http://www.e-mis.com/emisopen/MedicalRecord}PolicyListType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="PatientID" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MedicalRecordType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "registration",
    "registrationChangeHistory",
    "miscellaneousData",
    "eventList",
    "consultationList",
    "investigationList",
    "referralList",
    "allergyList",
    "medicationList",
    "issueList",
    "attachmentList",
    "diaryList",
    "alertList",
    "registrationStatus",
    "ediPathologyReportList",
    "testRequestHeaderList",
    "noteList",
    "appointmentList",
    "peopleList",
    "locationList",
    "locationTypeList",
    "originator",
    "recipientID",
    "messageInformation",
    "policyList"
})
public class MedicalRecordType {

    @XmlElement(name = "Registration", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected RegistrationType registration;
    @XmlElement(name = "RegistrationChangeHistory", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected MedicalRecordType.RegistrationChangeHistory registrationChangeHistory;
    @XmlElement(name = "MiscellaneousData", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected MedicalRecordType.MiscellaneousData miscellaneousData;
    @XmlElement(name = "EventList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected EventListType eventList;
    @XmlElement(name = "ConsultationList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected ConsultationListType consultationList;
    @XmlElement(name = "InvestigationList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected InvestigationListType investigationList;
    @XmlElement(name = "ReferralList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected ReferralListType referralList;
    @XmlElement(name = "AllergyList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected AllergyListType allergyList;
    @XmlElement(name = "MedicationList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected MedicationListType medicationList;
    @XmlElement(name = "IssueList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IssueListType issueList;
    @XmlElement(name = "AttachmentList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected AttachmentListType attachmentList;
    @XmlElement(name = "DiaryList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected DiaryListType diaryList;
    @XmlElement(name = "AlertList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected AlertListType alertList;
    @XmlElement(name = "RegistrationStatus", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected RegistrationStatusType registrationStatus;
    @XmlElement(name = "EDIPathologyReportList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected EDIPathologyReportListType ediPathologyReportList;
    @XmlElement(name = "TestRequestHeaderList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected MedicalRecordType.TestRequestHeaderList testRequestHeaderList;
    @XmlElement(name = "NoteList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected NoteListType noteList;
    @XmlElement(name = "AppointmentList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected AppointmentListType appointmentList;
    @XmlElement(name = "PeopleList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected PeopleListType peopleList;
    @XmlElement(name = "LocationList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected LocationListType locationList;
    @XmlElement(name = "LocationTypeList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected LocationTypeListType locationTypeList;
    @XmlElement(name = "Originator", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected OriginatorType originator;
    @XmlElement(name = "RecipientID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType recipientID;
    @XmlElement(name = "MessageInformation", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected MedicalRecordType.MessageInformation messageInformation;
    @XmlElement(name = "PolicyList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected PolicyListType policyList;
    @XmlAttribute(name = "PatientID")
    protected BigInteger patientID;

    /**
     * Gets the value of the registration property.
     * 
     * @return
     *     possible object is
     *     {@link RegistrationType }
     *     
     */
    public RegistrationType getRegistration() {
        return registration;
    }

    /**
     * Sets the value of the registration property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistrationType }
     *     
     */
    public void setRegistration(RegistrationType value) {
        this.registration = value;
    }

    /**
     * Gets the value of the registrationChangeHistory property.
     * 
     * @return
     *     possible object is
     *     {@link MedicalRecordType.RegistrationChangeHistory }
     *     
     */
    public MedicalRecordType.RegistrationChangeHistory getRegistrationChangeHistory() {
        return registrationChangeHistory;
    }

    /**
     * Sets the value of the registrationChangeHistory property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicalRecordType.RegistrationChangeHistory }
     *     
     */
    public void setRegistrationChangeHistory(MedicalRecordType.RegistrationChangeHistory value) {
        this.registrationChangeHistory = value;
    }

    /**
     * Gets the value of the miscellaneousData property.
     * 
     * @return
     *     possible object is
     *     {@link MedicalRecordType.MiscellaneousData }
     *     
     */
    public MedicalRecordType.MiscellaneousData getMiscellaneousData() {
        return miscellaneousData;
    }

    /**
     * Sets the value of the miscellaneousData property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicalRecordType.MiscellaneousData }
     *     
     */
    public void setMiscellaneousData(MedicalRecordType.MiscellaneousData value) {
        this.miscellaneousData = value;
    }

    /**
     * Gets the value of the eventList property.
     * 
     * @return
     *     possible object is
     *     {@link EventListType }
     *     
     */
    public EventListType getEventList() {
        return eventList;
    }

    /**
     * Sets the value of the eventList property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventListType }
     *     
     */
    public void setEventList(EventListType value) {
        this.eventList = value;
    }

    /**
     * Gets the value of the consultationList property.
     * 
     * @return
     *     possible object is
     *     {@link ConsultationListType }
     *     
     */
    public ConsultationListType getConsultationList() {
        return consultationList;
    }

    /**
     * Sets the value of the consultationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConsultationListType }
     *     
     */
    public void setConsultationList(ConsultationListType value) {
        this.consultationList = value;
    }

    /**
     * Gets the value of the investigationList property.
     * 
     * @return
     *     possible object is
     *     {@link InvestigationListType }
     *     
     */
    public InvestigationListType getInvestigationList() {
        return investigationList;
    }

    /**
     * Sets the value of the investigationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvestigationListType }
     *     
     */
    public void setInvestigationList(InvestigationListType value) {
        this.investigationList = value;
    }

    /**
     * Gets the value of the referralList property.
     * 
     * @return
     *     possible object is
     *     {@link ReferralListType }
     *     
     */
    public ReferralListType getReferralList() {
        return referralList;
    }

    /**
     * Sets the value of the referralList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferralListType }
     *     
     */
    public void setReferralList(ReferralListType value) {
        this.referralList = value;
    }

    /**
     * Gets the value of the allergyList property.
     * 
     * @return
     *     possible object is
     *     {@link AllergyListType }
     *     
     */
    public AllergyListType getAllergyList() {
        return allergyList;
    }

    /**
     * Sets the value of the allergyList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AllergyListType }
     *     
     */
    public void setAllergyList(AllergyListType value) {
        this.allergyList = value;
    }

    /**
     * Gets the value of the medicationList property.
     * 
     * @return
     *     possible object is
     *     {@link MedicationListType }
     *     
     */
    public MedicationListType getMedicationList() {
        return medicationList;
    }

    /**
     * Sets the value of the medicationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicationListType }
     *     
     */
    public void setMedicationList(MedicationListType value) {
        this.medicationList = value;
    }

    /**
     * Gets the value of the issueList property.
     * 
     * @return
     *     possible object is
     *     {@link IssueListType }
     *     
     */
    public IssueListType getIssueList() {
        return issueList;
    }

    /**
     * Sets the value of the issueList property.
     * 
     * @param value
     *     allowed object is
     *     {@link IssueListType }
     *     
     */
    public void setIssueList(IssueListType value) {
        this.issueList = value;
    }

    /**
     * Gets the value of the attachmentList property.
     * 
     * @return
     *     possible object is
     *     {@link AttachmentListType }
     *     
     */
    public AttachmentListType getAttachmentList() {
        return attachmentList;
    }

    /**
     * Sets the value of the attachmentList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttachmentListType }
     *     
     */
    public void setAttachmentList(AttachmentListType value) {
        this.attachmentList = value;
    }

    /**
     * Gets the value of the diaryList property.
     * 
     * @return
     *     possible object is
     *     {@link DiaryListType }
     *     
     */
    public DiaryListType getDiaryList() {
        return diaryList;
    }

    /**
     * Sets the value of the diaryList property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiaryListType }
     *     
     */
    public void setDiaryList(DiaryListType value) {
        this.diaryList = value;
    }

    /**
     * Gets the value of the alertList property.
     * 
     * @return
     *     possible object is
     *     {@link AlertListType }
     *     
     */
    public AlertListType getAlertList() {
        return alertList;
    }

    /**
     * Sets the value of the alertList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AlertListType }
     *     
     */
    public void setAlertList(AlertListType value) {
        this.alertList = value;
    }

    /**
     * Gets the value of the registrationStatus property.
     * 
     * @return
     *     possible object is
     *     {@link RegistrationStatusType }
     *     
     */
    public RegistrationStatusType getRegistrationStatus() {
        return registrationStatus;
    }

    /**
     * Sets the value of the registrationStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistrationStatusType }
     *     
     */
    public void setRegistrationStatus(RegistrationStatusType value) {
        this.registrationStatus = value;
    }

    /**
     * Gets the value of the ediPathologyReportList property.
     * 
     * @return
     *     possible object is
     *     {@link EDIPathologyReportListType }
     *     
     */
    public EDIPathologyReportListType getEDIPathologyReportList() {
        return ediPathologyReportList;
    }

    /**
     * Sets the value of the ediPathologyReportList property.
     * 
     * @param value
     *     allowed object is
     *     {@link EDIPathologyReportListType }
     *     
     */
    public void setEDIPathologyReportList(EDIPathologyReportListType value) {
        this.ediPathologyReportList = value;
    }

    /**
     * Gets the value of the testRequestHeaderList property.
     * 
     * @return
     *     possible object is
     *     {@link MedicalRecordType.TestRequestHeaderList }
     *     
     */
    public MedicalRecordType.TestRequestHeaderList getTestRequestHeaderList() {
        return testRequestHeaderList;
    }

    /**
     * Sets the value of the testRequestHeaderList property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicalRecordType.TestRequestHeaderList }
     *     
     */
    public void setTestRequestHeaderList(MedicalRecordType.TestRequestHeaderList value) {
        this.testRequestHeaderList = value;
    }

    /**
     * Gets the value of the noteList property.
     * 
     * @return
     *     possible object is
     *     {@link NoteListType }
     *     
     */
    public NoteListType getNoteList() {
        return noteList;
    }

    /**
     * Sets the value of the noteList property.
     * 
     * @param value
     *     allowed object is
     *     {@link NoteListType }
     *     
     */
    public void setNoteList(NoteListType value) {
        this.noteList = value;
    }

    /**
     * Gets the value of the appointmentList property.
     * 
     * @return
     *     possible object is
     *     {@link AppointmentListType }
     *     
     */
    public AppointmentListType getAppointmentList() {
        return appointmentList;
    }

    /**
     * Sets the value of the appointmentList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AppointmentListType }
     *     
     */
    public void setAppointmentList(AppointmentListType value) {
        this.appointmentList = value;
    }

    /**
     * Gets the value of the peopleList property.
     * 
     * @return
     *     possible object is
     *     {@link PeopleListType }
     *     
     */
    public PeopleListType getPeopleList() {
        return peopleList;
    }

    /**
     * Sets the value of the peopleList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PeopleListType }
     *     
     */
    public void setPeopleList(PeopleListType value) {
        this.peopleList = value;
    }

    /**
     * Gets the value of the locationList property.
     * 
     * @return
     *     possible object is
     *     {@link LocationListType }
     *     
     */
    public LocationListType getLocationList() {
        return locationList;
    }

    /**
     * Sets the value of the locationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationListType }
     *     
     */
    public void setLocationList(LocationListType value) {
        this.locationList = value;
    }

    /**
     * Gets the value of the locationTypeList property.
     * 
     * @return
     *     possible object is
     *     {@link LocationTypeListType }
     *     
     */
    public LocationTypeListType getLocationTypeList() {
        return locationTypeList;
    }

    /**
     * Sets the value of the locationTypeList property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationTypeListType }
     *     
     */
    public void setLocationTypeList(LocationTypeListType value) {
        this.locationTypeList = value;
    }

    /**
     * Gets the value of the originator property.
     * 
     * @return
     *     possible object is
     *     {@link OriginatorType }
     *     
     */
    public OriginatorType getOriginator() {
        return originator;
    }

    /**
     * Sets the value of the originator property.
     * 
     * @param value
     *     allowed object is
     *     {@link OriginatorType }
     *     
     */
    public void setOriginator(OriginatorType value) {
        this.originator = value;
    }

    /**
     * Gets the value of the recipientID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getRecipientID() {
        return recipientID;
    }

    /**
     * Sets the value of the recipientID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setRecipientID(IdentType value) {
        this.recipientID = value;
    }

    /**
     * Gets the value of the messageInformation property.
     * 
     * @return
     *     possible object is
     *     {@link MedicalRecordType.MessageInformation }
     *     
     */
    public MedicalRecordType.MessageInformation getMessageInformation() {
        return messageInformation;
    }

    /**
     * Sets the value of the messageInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicalRecordType.MessageInformation }
     *     
     */
    public void setMessageInformation(MedicalRecordType.MessageInformation value) {
        this.messageInformation = value;
    }

    /**
     * Gets the value of the policyList property.
     * 
     * @return
     *     possible object is
     *     {@link PolicyListType }
     *     
     */
    public PolicyListType getPolicyList() {
        return policyList;
    }

    /**
     * Sets the value of the policyList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PolicyListType }
     *     
     */
    public void setPolicyList(PolicyListType value) {
        this.policyList = value;
    }

    /**
     * Gets the value of the patientID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPatientID() {
        return patientID;
    }

    /**
     * Sets the value of the patientID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPatientID(BigInteger value) {
        this.patientID = value;
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
     *         &lt;element name="DateCreated" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="TimeCreated" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="MessagePurpose" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;choice>
     *                   &lt;element name="Post">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="PostType">
     *                               &lt;simpleType>
     *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *                                   &lt;enumeration value="1"/>
     *                                   &lt;enumeration value="2"/>
     *                                 &lt;/restriction>
     *                               &lt;/simpleType>
     *                             &lt;/element>
     *                             &lt;element name="SessionID" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="Get">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="MRGetType">
     *                               &lt;simpleType>
     *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *                                   &lt;enumeration value="1"/>
     *                                   &lt;enumeration value="2"/>
     *                                   &lt;enumeration value="3"/>
     *                                   &lt;enumeration value="4"/>
     *                                 &lt;/restriction>
     *                               &lt;/simpleType>
     *                             &lt;/element>
     *                             &lt;element name="DataSet" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                             &lt;element name="SessionID" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/choice>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="MessagingError" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="ErrorString" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="Errornumber" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
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
        "dateCreated",
        "timeCreated",
        "messagePurpose",
        "messagingError"
    })
    public static class MessageInformation {

        @XmlElement(name = "DateCreated", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String dateCreated;
        @XmlElement(name = "TimeCreated", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String timeCreated;
        @XmlElement(name = "MessagePurpose", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected MedicalRecordType.MessageInformation.MessagePurpose messagePurpose;
        @XmlElement(name = "MessagingError", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected MedicalRecordType.MessageInformation.MessagingError messagingError;

        /**
         * Gets the value of the dateCreated property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDateCreated() {
            return dateCreated;
        }

        /**
         * Sets the value of the dateCreated property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDateCreated(String value) {
            this.dateCreated = value;
        }

        /**
         * Gets the value of the timeCreated property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTimeCreated() {
            return timeCreated;
        }

        /**
         * Sets the value of the timeCreated property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTimeCreated(String value) {
            this.timeCreated = value;
        }

        /**
         * Gets the value of the messagePurpose property.
         * 
         * @return
         *     possible object is
         *     {@link MedicalRecordType.MessageInformation.MessagePurpose }
         *     
         */
        public MedicalRecordType.MessageInformation.MessagePurpose getMessagePurpose() {
            return messagePurpose;
        }

        /**
         * Sets the value of the messagePurpose property.
         * 
         * @param value
         *     allowed object is
         *     {@link MedicalRecordType.MessageInformation.MessagePurpose }
         *     
         */
        public void setMessagePurpose(MedicalRecordType.MessageInformation.MessagePurpose value) {
            this.messagePurpose = value;
        }

        /**
         * Gets the value of the messagingError property.
         * 
         * @return
         *     possible object is
         *     {@link MedicalRecordType.MessageInformation.MessagingError }
         *     
         */
        public MedicalRecordType.MessageInformation.MessagingError getMessagingError() {
            return messagingError;
        }

        /**
         * Sets the value of the messagingError property.
         * 
         * @param value
         *     allowed object is
         *     {@link MedicalRecordType.MessageInformation.MessagingError }
         *     
         */
        public void setMessagingError(MedicalRecordType.MessageInformation.MessagingError value) {
            this.messagingError = value;
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
         *       &lt;choice>
         *         &lt;element name="Post">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="PostType">
         *                     &lt;simpleType>
         *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
         *                         &lt;enumeration value="1"/>
         *                         &lt;enumeration value="2"/>
         *                       &lt;/restriction>
         *                     &lt;/simpleType>
         *                   &lt;/element>
         *                   &lt;element name="SessionID" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="Get">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="MRGetType">
         *                     &lt;simpleType>
         *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
         *                         &lt;enumeration value="1"/>
         *                         &lt;enumeration value="2"/>
         *                         &lt;enumeration value="3"/>
         *                         &lt;enumeration value="4"/>
         *                       &lt;/restriction>
         *                     &lt;/simpleType>
         *                   &lt;/element>
         *                   &lt;element name="DataSet" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                   &lt;element name="SessionID" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/choice>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "post",
            "get"
        })
        public static class MessagePurpose {

            @XmlElement(name = "Post", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected MedicalRecordType.MessageInformation.MessagePurpose.Post post;
            @XmlElement(name = "Get", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected MedicalRecordType.MessageInformation.MessagePurpose.Get get;

            /**
             * Gets the value of the post property.
             * 
             * @return
             *     possible object is
             *     {@link MedicalRecordType.MessageInformation.MessagePurpose.Post }
             *     
             */
            public MedicalRecordType.MessageInformation.MessagePurpose.Post getPost() {
                return post;
            }

            /**
             * Sets the value of the post property.
             * 
             * @param value
             *     allowed object is
             *     {@link MedicalRecordType.MessageInformation.MessagePurpose.Post }
             *     
             */
            public void setPost(MedicalRecordType.MessageInformation.MessagePurpose.Post value) {
                this.post = value;
            }

            /**
             * Gets the value of the get property.
             * 
             * @return
             *     possible object is
             *     {@link MedicalRecordType.MessageInformation.MessagePurpose.Get }
             *     
             */
            public MedicalRecordType.MessageInformation.MessagePurpose.Get getGet() {
                return get;
            }

            /**
             * Sets the value of the get property.
             * 
             * @param value
             *     allowed object is
             *     {@link MedicalRecordType.MessageInformation.MessagePurpose.Get }
             *     
             */
            public void setGet(MedicalRecordType.MessageInformation.MessagePurpose.Get value) {
                this.get = value;
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
             *         &lt;element name="MRGetType">
             *           &lt;simpleType>
             *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
             *               &lt;enumeration value="1"/>
             *               &lt;enumeration value="2"/>
             *               &lt;enumeration value="3"/>
             *               &lt;enumeration value="4"/>
             *             &lt;/restriction>
             *           &lt;/simpleType>
             *         &lt;/element>
             *         &lt;element name="DataSet" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         &lt;element name="SessionID" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
                "mrGetType",
                "dataSet",
                "sessionID"
            })
            public static class Get {

                @XmlElement(name = "MRGetType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
                protected BigInteger mrGetType;
                @XmlElement(name = "DataSet", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
                protected String dataSet;
                @XmlElement(name = "SessionID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
                protected String sessionID;

                /**
                 * Gets the value of the mrGetType property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getMRGetType() {
                    return mrGetType;
                }

                /**
                 * Sets the value of the mrGetType property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setMRGetType(BigInteger value) {
                    this.mrGetType = value;
                }

                /**
                 * Gets the value of the dataSet property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getDataSet() {
                    return dataSet;
                }

                /**
                 * Sets the value of the dataSet property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setDataSet(String value) {
                    this.dataSet = value;
                }

                /**
                 * Gets the value of the sessionID property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getSessionID() {
                    return sessionID;
                }

                /**
                 * Sets the value of the sessionID property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setSessionID(String value) {
                    this.sessionID = value;
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
             *         &lt;element name="PostType">
             *           &lt;simpleType>
             *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
             *               &lt;enumeration value="1"/>
             *               &lt;enumeration value="2"/>
             *             &lt;/restriction>
             *           &lt;/simpleType>
             *         &lt;/element>
             *         &lt;element name="SessionID" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
                "postType",
                "sessionID"
            })
            public static class Post {

                @XmlElement(name = "PostType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
                protected BigInteger postType;
                @XmlElement(name = "SessionID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
                protected String sessionID;

                /**
                 * Gets the value of the postType property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getPostType() {
                    return postType;
                }

                /**
                 * Sets the value of the postType property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setPostType(BigInteger value) {
                    this.postType = value;
                }

                /**
                 * Gets the value of the sessionID property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getSessionID() {
                    return sessionID;
                }

                /**
                 * Sets the value of the sessionID property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setSessionID(String value) {
                    this.sessionID = value;
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
         *         &lt;element name="ErrorString" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="Errornumber" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
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
            "errorString",
            "errornumber"
        })
        public static class MessagingError {

            @XmlElement(name = "ErrorString", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
            protected String errorString;
            @XmlElement(name = "Errornumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected BigInteger errornumber;

            /**
             * Gets the value of the errorString property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getErrorString() {
                return errorString;
            }

            /**
             * Sets the value of the errorString property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setErrorString(String value) {
                this.errorString = value;
            }

            /**
             * Gets the value of the errornumber property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getErrornumber() {
                return errornumber;
            }

            /**
             * Sets the value of the errornumber property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setErrornumber(BigInteger value) {
                this.errornumber = value;
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
     *         &lt;element name="MedicationMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="AutomaticWeekNumber" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
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
        "medicationMessage",
        "automaticWeekNumber"
    })
    public static class MiscellaneousData {

        @XmlElement(name = "MedicationMessage", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String medicationMessage;
        @XmlElement(name = "AutomaticWeekNumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected BigInteger automaticWeekNumber;

        /**
         * Gets the value of the medicationMessage property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMedicationMessage() {
            return medicationMessage;
        }

        /**
         * Sets the value of the medicationMessage property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMedicationMessage(String value) {
            this.medicationMessage = value;
        }

        /**
         * Gets the value of the automaticWeekNumber property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getAutomaticWeekNumber() {
            return automaticWeekNumber;
        }

        /**
         * Sets the value of the automaticWeekNumber property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setAutomaticWeekNumber(BigInteger value) {
            this.automaticWeekNumber = value;
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
     *         &lt;element name="RegistrationEntry" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="FieldName" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="FieldValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="DateChanged" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="UserID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType"/>
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
        "registrationEntry"
    })
    public static class RegistrationChangeHistory {

        @XmlElement(name = "RegistrationEntry", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected List<MedicalRecordType.RegistrationChangeHistory.RegistrationEntry> registrationEntry;

        /**
         * Gets the value of the registrationEntry property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the registrationEntry property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRegistrationEntry().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MedicalRecordType.RegistrationChangeHistory.RegistrationEntry }
         * 
         * 
         */
        public List<MedicalRecordType.RegistrationChangeHistory.RegistrationEntry> getRegistrationEntry() {
            if (registrationEntry == null) {
                registrationEntry = new ArrayList<MedicalRecordType.RegistrationChangeHistory.RegistrationEntry>();
            }
            return this.registrationEntry;
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
         *         &lt;element name="DateChanged" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="UserID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType"/>
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
            "fieldValue",
            "dateChanged",
            "userID"
        })
        public static class RegistrationEntry {

            @XmlElement(name = "FieldName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
            protected String fieldName;
            @XmlElement(name = "FieldValue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String fieldValue;
            @XmlElement(name = "DateChanged", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
            protected String dateChanged;
            @XmlElement(name = "UserID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
            protected IdentType userID;

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

            /**
             * Gets the value of the dateChanged property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getDateChanged() {
                return dateChanged;
            }

            /**
             * Sets the value of the dateChanged property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDateChanged(String value) {
                this.dateChanged = value;
            }

            /**
             * Gets the value of the userID property.
             * 
             * @return
             *     possible object is
             *     {@link IdentType }
             *     
             */
            public IdentType getUserID() {
                return userID;
            }

            /**
             * Sets the value of the userID property.
             * 
             * @param value
             *     allowed object is
             *     {@link IdentType }
             *     
             */
            public void setUserID(IdentType value) {
                this.userID = value;
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
     *         &lt;element name="TestRequestHeader" type="{http://www.e-mis.com/emisopen/MedicalRecord}TestRequestHeaderType" maxOccurs="unbounded" minOccurs="0"/>
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
        "testRequestHeader"
    })
    public static class TestRequestHeaderList {

        @XmlElement(name = "TestRequestHeader", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected List<TestRequestHeaderType> testRequestHeader;

        /**
         * Gets the value of the testRequestHeader property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the testRequestHeader property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTestRequestHeader().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link TestRequestHeaderType }
         * 
         * 
         */
        public List<TestRequestHeaderType> getTestRequestHeader() {
            if (testRequestHeader == null) {
                testRequestHeader = new ArrayList<TestRequestHeaderType>();
            }
            return this.testRequestHeader;
        }

    }

}

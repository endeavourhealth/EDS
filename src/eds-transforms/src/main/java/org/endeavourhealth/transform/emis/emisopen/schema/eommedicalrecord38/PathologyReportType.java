
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PathologyReportType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PathologyReportType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="ReportDataType" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="ReportID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ReportReceivedDateTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ReportIssueDateTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ReportTextList" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDICommentListType" minOccurs="0"/>
 *         &lt;element name="Abnormal" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="RecordFileStatus" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="ConsultationRef" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Box" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="RequestID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RequestDateTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CurrentOwner" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
 *         &lt;element name="Identifiers" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="OriginalDetails" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="Forename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="LastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="DateOfBirth" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="NHS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="Address" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="Sex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="LabSubjectID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="MatchedDetails" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="Forename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="LastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="DateOfBirth" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="NHS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="PatientID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="Sex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
 *         &lt;element name="OriginalRequestor" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="Organisation" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
 *                   &lt;element name="Person" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="MessageRecepient" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
 *         &lt;element name="ServiceProvider" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Department" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
 *                   &lt;element name="Organisation" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
 *                   &lt;element name="Person" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="OriginalMessageDetails" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="SenderEDICode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="ReceiverEDICode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="InterChange" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="MessageID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="MessageType" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="ASTM"/>
 *                         &lt;enumeration value="MEDRPT"/>
 *                         &lt;enumeration value="NHS002"/>
 *                         &lt;enumeration value="NHS003"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ClinicalInformationList" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDICommentListType" minOccurs="0"/>
 *         &lt;element name="ViewedBy" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="TrueReportID" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="SpecimenList" type="{http://www.e-mis.com/emisopen/MedicalRecord}SpecimenListType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PathologyReportType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "reportDataType",
    "reportID",
    "reportReceivedDateTime",
    "reportIssueDateTime",
    "reportTextList",
    "abnormal",
    "recordFileStatus",
    "consultationRef",
    "box",
    "requestID",
    "requestDateTime",
    "currentOwner",
    "identifiers",
    "originalRequestor",
    "messageRecepient",
    "serviceProvider",
    "originalMessageDetails",
    "clinicalInformationList",
    "viewedBy",
    "trueReportID",
    "specimenList"
})
public class PathologyReportType
    extends IdentType
{

    @XmlElement(name = "ReportDataType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger reportDataType;
    @XmlElement(name = "ReportID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String reportID;
    @XmlElement(name = "ReportReceivedDateTime", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String reportReceivedDateTime;
    @XmlElement(name = "ReportIssueDateTime", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String reportIssueDateTime;
    @XmlElement(name = "ReportTextList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected EDICommentListType reportTextList;
    @XmlElement(name = "Abnormal", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger abnormal;
    @XmlElement(name = "RecordFileStatus", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger recordFileStatus;
    @XmlElement(name = "ConsultationRef", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String consultationRef;
    @XmlElement(name = "Box", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger box;
    @XmlElement(name = "RequestID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String requestID;
    @XmlElement(name = "RequestDateTime", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String requestDateTime;
    @XmlElement(name = "CurrentOwner", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StructuredIdentType currentOwner;
    @XmlElement(name = "Identifiers", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected PathologyReportType.Identifiers identifiers;
    @XmlElement(name = "OriginalRequestor", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected PathologyReportType.OriginalRequestor originalRequestor;
    @XmlElement(name = "MessageRecepient", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StructuredIdentType messageRecepient;
    @XmlElement(name = "ServiceProvider", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected PathologyReportType.ServiceProvider serviceProvider;
    @XmlElement(name = "OriginalMessageDetails", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected PathologyReportType.OriginalMessageDetails originalMessageDetails;
    @XmlElement(name = "ClinicalInformationList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected EDICommentListType clinicalInformationList;
    @XmlElement(name = "ViewedBy", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Object viewedBy;
    @XmlElement(name = "TrueReportID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Object trueReportID;
    @XmlElement(name = "SpecimenList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected SpecimenListType specimenList;

    /**
     * Gets the value of the reportDataType property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getReportDataType() {
        return reportDataType;
    }

    /**
     * Sets the value of the reportDataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setReportDataType(BigInteger value) {
        this.reportDataType = value;
    }

    /**
     * Gets the value of the reportID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportID() {
        return reportID;
    }

    /**
     * Sets the value of the reportID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportID(String value) {
        this.reportID = value;
    }

    /**
     * Gets the value of the reportReceivedDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportReceivedDateTime() {
        return reportReceivedDateTime;
    }

    /**
     * Sets the value of the reportReceivedDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportReceivedDateTime(String value) {
        this.reportReceivedDateTime = value;
    }

    /**
     * Gets the value of the reportIssueDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportIssueDateTime() {
        return reportIssueDateTime;
    }

    /**
     * Sets the value of the reportIssueDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportIssueDateTime(String value) {
        this.reportIssueDateTime = value;
    }

    /**
     * Gets the value of the reportTextList property.
     * 
     * @return
     *     possible object is
     *     {@link EDICommentListType }
     *     
     */
    public EDICommentListType getReportTextList() {
        return reportTextList;
    }

    /**
     * Sets the value of the reportTextList property.
     * 
     * @param value
     *     allowed object is
     *     {@link EDICommentListType }
     *     
     */
    public void setReportTextList(EDICommentListType value) {
        this.reportTextList = value;
    }

    /**
     * Gets the value of the abnormal property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAbnormal() {
        return abnormal;
    }

    /**
     * Sets the value of the abnormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAbnormal(BigInteger value) {
        this.abnormal = value;
    }

    /**
     * Gets the value of the recordFileStatus property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRecordFileStatus() {
        return recordFileStatus;
    }

    /**
     * Sets the value of the recordFileStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRecordFileStatus(BigInteger value) {
        this.recordFileStatus = value;
    }

    /**
     * Gets the value of the consultationRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsultationRef() {
        return consultationRef;
    }

    /**
     * Sets the value of the consultationRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsultationRef(String value) {
        this.consultationRef = value;
    }

    /**
     * Gets the value of the box property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getBox() {
        return box;
    }

    /**
     * Sets the value of the box property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setBox(BigInteger value) {
        this.box = value;
    }

    /**
     * Gets the value of the requestID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestID() {
        return requestID;
    }

    /**
     * Sets the value of the requestID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestID(String value) {
        this.requestID = value;
    }

    /**
     * Gets the value of the requestDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestDateTime() {
        return requestDateTime;
    }

    /**
     * Sets the value of the requestDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestDateTime(String value) {
        this.requestDateTime = value;
    }

    /**
     * Gets the value of the currentOwner property.
     * 
     * @return
     *     possible object is
     *     {@link StructuredIdentType }
     *     
     */
    public StructuredIdentType getCurrentOwner() {
        return currentOwner;
    }

    /**
     * Sets the value of the currentOwner property.
     * 
     * @param value
     *     allowed object is
     *     {@link StructuredIdentType }
     *     
     */
    public void setCurrentOwner(StructuredIdentType value) {
        this.currentOwner = value;
    }

    /**
     * Gets the value of the identifiers property.
     * 
     * @return
     *     possible object is
     *     {@link PathologyReportType.Identifiers }
     *     
     */
    public PathologyReportType.Identifiers getIdentifiers() {
        return identifiers;
    }

    /**
     * Sets the value of the identifiers property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathologyReportType.Identifiers }
     *     
     */
    public void setIdentifiers(PathologyReportType.Identifiers value) {
        this.identifiers = value;
    }

    /**
     * Gets the value of the originalRequestor property.
     * 
     * @return
     *     possible object is
     *     {@link PathologyReportType.OriginalRequestor }
     *     
     */
    public PathologyReportType.OriginalRequestor getOriginalRequestor() {
        return originalRequestor;
    }

    /**
     * Sets the value of the originalRequestor property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathologyReportType.OriginalRequestor }
     *     
     */
    public void setOriginalRequestor(PathologyReportType.OriginalRequestor value) {
        this.originalRequestor = value;
    }

    /**
     * Gets the value of the messageRecepient property.
     * 
     * @return
     *     possible object is
     *     {@link StructuredIdentType }
     *     
     */
    public StructuredIdentType getMessageRecepient() {
        return messageRecepient;
    }

    /**
     * Sets the value of the messageRecepient property.
     * 
     * @param value
     *     allowed object is
     *     {@link StructuredIdentType }
     *     
     */
    public void setMessageRecepient(StructuredIdentType value) {
        this.messageRecepient = value;
    }

    /**
     * Gets the value of the serviceProvider property.
     * 
     * @return
     *     possible object is
     *     {@link PathologyReportType.ServiceProvider }
     *     
     */
    public PathologyReportType.ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    /**
     * Sets the value of the serviceProvider property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathologyReportType.ServiceProvider }
     *     
     */
    public void setServiceProvider(PathologyReportType.ServiceProvider value) {
        this.serviceProvider = value;
    }

    /**
     * Gets the value of the originalMessageDetails property.
     * 
     * @return
     *     possible object is
     *     {@link PathologyReportType.OriginalMessageDetails }
     *     
     */
    public PathologyReportType.OriginalMessageDetails getOriginalMessageDetails() {
        return originalMessageDetails;
    }

    /**
     * Sets the value of the originalMessageDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathologyReportType.OriginalMessageDetails }
     *     
     */
    public void setOriginalMessageDetails(PathologyReportType.OriginalMessageDetails value) {
        this.originalMessageDetails = value;
    }

    /**
     * Gets the value of the clinicalInformationList property.
     * 
     * @return
     *     possible object is
     *     {@link EDICommentListType }
     *     
     */
    public EDICommentListType getClinicalInformationList() {
        return clinicalInformationList;
    }

    /**
     * Sets the value of the clinicalInformationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link EDICommentListType }
     *     
     */
    public void setClinicalInformationList(EDICommentListType value) {
        this.clinicalInformationList = value;
    }

    /**
     * Gets the value of the viewedBy property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getViewedBy() {
        return viewedBy;
    }

    /**
     * Sets the value of the viewedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setViewedBy(Object value) {
        this.viewedBy = value;
    }

    /**
     * Gets the value of the trueReportID property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getTrueReportID() {
        return trueReportID;
    }

    /**
     * Sets the value of the trueReportID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setTrueReportID(Object value) {
        this.trueReportID = value;
    }

    /**
     * Gets the value of the specimenList property.
     * 
     * @return
     *     possible object is
     *     {@link SpecimenListType }
     *     
     */
    public SpecimenListType getSpecimenList() {
        return specimenList;
    }

    /**
     * Sets the value of the specimenList property.
     * 
     * @param value
     *     allowed object is
     *     {@link SpecimenListType }
     *     
     */
    public void setSpecimenList(SpecimenListType value) {
        this.specimenList = value;
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
     *         &lt;element name="OriginalDetails" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="Forename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="LastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="DateOfBirth" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="NHS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="Address" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="Sex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="LabSubjectID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="MatchedDetails" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="Forename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="LastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="DateOfBirth" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="NHS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="PatientID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="Sex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
        "originalDetails",
        "matchedDetails"
    })
    public static class Identifiers {

        @XmlElement(name = "OriginalDetails", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected PathologyReportType.Identifiers.OriginalDetails originalDetails;
        @XmlElement(name = "MatchedDetails", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected PathologyReportType.Identifiers.MatchedDetails matchedDetails;

        /**
         * Gets the value of the originalDetails property.
         * 
         * @return
         *     possible object is
         *     {@link PathologyReportType.Identifiers.OriginalDetails }
         *     
         */
        public PathologyReportType.Identifiers.OriginalDetails getOriginalDetails() {
            return originalDetails;
        }

        /**
         * Sets the value of the originalDetails property.
         * 
         * @param value
         *     allowed object is
         *     {@link PathologyReportType.Identifiers.OriginalDetails }
         *     
         */
        public void setOriginalDetails(PathologyReportType.Identifiers.OriginalDetails value) {
            this.originalDetails = value;
        }

        /**
         * Gets the value of the matchedDetails property.
         * 
         * @return
         *     possible object is
         *     {@link PathologyReportType.Identifiers.MatchedDetails }
         *     
         */
        public PathologyReportType.Identifiers.MatchedDetails getMatchedDetails() {
            return matchedDetails;
        }

        /**
         * Sets the value of the matchedDetails property.
         * 
         * @param value
         *     allowed object is
         *     {@link PathologyReportType.Identifiers.MatchedDetails }
         *     
         */
        public void setMatchedDetails(PathologyReportType.Identifiers.MatchedDetails value) {
            this.matchedDetails = value;
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
         *         &lt;element name="Forename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="LastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="DateOfBirth" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="NHS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="PatientID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="Sex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
            "forename",
            "lastName",
            "dateOfBirth",
            "nhs",
            "patientID",
            "sex"
        })
        public static class MatchedDetails {

            @XmlElement(name = "Forename", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String forename;
            @XmlElement(name = "LastName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String lastName;
            @XmlElement(name = "DateOfBirth", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String dateOfBirth;
            @XmlElement(name = "NHS", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String nhs;
            @XmlElement(name = "PatientID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String patientID;
            @XmlElement(name = "Sex", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String sex;

            /**
             * Gets the value of the forename property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getForename() {
                return forename;
            }

            /**
             * Sets the value of the forename property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setForename(String value) {
                this.forename = value;
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
             * Gets the value of the nhs property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getNHS() {
                return nhs;
            }

            /**
             * Sets the value of the nhs property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setNHS(String value) {
                this.nhs = value;
            }

            /**
             * Gets the value of the patientID property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getPatientID() {
                return patientID;
            }

            /**
             * Sets the value of the patientID property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPatientID(String value) {
                this.patientID = value;
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
         *         &lt;element name="Forename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="LastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="DateOfBirth" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="NHS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="Address" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="Sex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="LabSubjectID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
            "forename",
            "lastName",
            "dateOfBirth",
            "nhs",
            "address",
            "sex",
            "labSubjectID"
        })
        public static class OriginalDetails {

            @XmlElement(name = "Forename", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String forename;
            @XmlElement(name = "LastName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String lastName;
            @XmlElement(name = "DateOfBirth", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String dateOfBirth;
            @XmlElement(name = "NHS", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String nhs;
            @XmlElement(name = "Address", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String address;
            @XmlElement(name = "Sex", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String sex;
            @XmlElement(name = "LabSubjectID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String labSubjectID;

            /**
             * Gets the value of the forename property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getForename() {
                return forename;
            }

            /**
             * Sets the value of the forename property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setForename(String value) {
                this.forename = value;
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
             * Gets the value of the nhs property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getNHS() {
                return nhs;
            }

            /**
             * Sets the value of the nhs property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setNHS(String value) {
                this.nhs = value;
            }

            /**
             * Gets the value of the address property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAddress() {
                return address;
            }

            /**
             * Sets the value of the address property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAddress(String value) {
                this.address = value;
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
             * Gets the value of the labSubjectID property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getLabSubjectID() {
                return labSubjectID;
            }

            /**
             * Sets the value of the labSubjectID property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setLabSubjectID(String value) {
                this.labSubjectID = value;
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
     *         &lt;element name="SenderEDICode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="ReceiverEDICode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="InterChange" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="MessageID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="MessageType" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="ASTM"/>
     *               &lt;enumeration value="MEDRPT"/>
     *               &lt;enumeration value="NHS002"/>
     *               &lt;enumeration value="NHS003"/>
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
        "senderEDICode",
        "receiverEDICode",
        "interChange",
        "messageID",
        "messageType"
    })
    public static class OriginalMessageDetails {

        @XmlElement(name = "SenderEDICode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String senderEDICode;
        @XmlElement(name = "ReceiverEDICode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String receiverEDICode;
        @XmlElement(name = "InterChange", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String interChange;
        @XmlElement(name = "MessageID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String messageID;
        @XmlElement(name = "MessageType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String messageType;

        /**
         * Gets the value of the senderEDICode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSenderEDICode() {
            return senderEDICode;
        }

        /**
         * Sets the value of the senderEDICode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSenderEDICode(String value) {
            this.senderEDICode = value;
        }

        /**
         * Gets the value of the receiverEDICode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getReceiverEDICode() {
            return receiverEDICode;
        }

        /**
         * Sets the value of the receiverEDICode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setReceiverEDICode(String value) {
            this.receiverEDICode = value;
        }

        /**
         * Gets the value of the interChange property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getInterChange() {
            return interChange;
        }

        /**
         * Sets the value of the interChange property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setInterChange(String value) {
            this.interChange = value;
        }

        /**
         * Gets the value of the messageID property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMessageID() {
            return messageID;
        }

        /**
         * Sets the value of the messageID property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMessageID(String value) {
            this.messageID = value;
        }

        /**
         * Gets the value of the messageType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMessageType() {
            return messageType;
        }

        /**
         * Sets the value of the messageType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMessageType(String value) {
            this.messageType = value;
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
     *       &lt;choice>
     *         &lt;element name="Organisation" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
     *         &lt;element name="Person" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
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
        "organisation",
        "person"
    })
    public static class OriginalRequestor {

        @XmlElement(name = "Organisation", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected StructuredIdentType organisation;
        @XmlElement(name = "Person", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected StructuredIdentType person;

        /**
         * Gets the value of the organisation property.
         * 
         * @return
         *     possible object is
         *     {@link StructuredIdentType }
         *     
         */
        public StructuredIdentType getOrganisation() {
            return organisation;
        }

        /**
         * Sets the value of the organisation property.
         * 
         * @param value
         *     allowed object is
         *     {@link StructuredIdentType }
         *     
         */
        public void setOrganisation(StructuredIdentType value) {
            this.organisation = value;
        }

        /**
         * Gets the value of the person property.
         * 
         * @return
         *     possible object is
         *     {@link StructuredIdentType }
         *     
         */
        public StructuredIdentType getPerson() {
            return person;
        }

        /**
         * Sets the value of the person property.
         * 
         * @param value
         *     allowed object is
         *     {@link StructuredIdentType }
         *     
         */
        public void setPerson(StructuredIdentType value) {
            this.person = value;
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
     *         &lt;element name="Department" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
     *         &lt;element name="Organisation" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
     *         &lt;element name="Person" type="{http://www.e-mis.com/emisopen/MedicalRecord}StructuredIdentType" minOccurs="0"/>
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
        "department",
        "organisation",
        "person"
    })
    public static class ServiceProvider {

        @XmlElement(name = "Department", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected StructuredIdentType department;
        @XmlElement(name = "Organisation", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected StructuredIdentType organisation;
        @XmlElement(name = "Person", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected StructuredIdentType person;

        /**
         * Gets the value of the department property.
         * 
         * @return
         *     possible object is
         *     {@link StructuredIdentType }
         *     
         */
        public StructuredIdentType getDepartment() {
            return department;
        }

        /**
         * Sets the value of the department property.
         * 
         * @param value
         *     allowed object is
         *     {@link StructuredIdentType }
         *     
         */
        public void setDepartment(StructuredIdentType value) {
            this.department = value;
        }

        /**
         * Gets the value of the organisation property.
         * 
         * @return
         *     possible object is
         *     {@link StructuredIdentType }
         *     
         */
        public StructuredIdentType getOrganisation() {
            return organisation;
        }

        /**
         * Sets the value of the organisation property.
         * 
         * @param value
         *     allowed object is
         *     {@link StructuredIdentType }
         *     
         */
        public void setOrganisation(StructuredIdentType value) {
            this.organisation = value;
        }

        /**
         * Gets the value of the person property.
         * 
         * @return
         *     possible object is
         *     {@link StructuredIdentType }
         *     
         */
        public StructuredIdentType getPerson() {
            return person;
        }

        /**
         * Sets the value of the person property.
         * 
         * @param value
         *     allowed object is
         *     {@link StructuredIdentType }
         *     
         */
        public void setPerson(StructuredIdentType value) {
            this.person = value;
        }

    }

}

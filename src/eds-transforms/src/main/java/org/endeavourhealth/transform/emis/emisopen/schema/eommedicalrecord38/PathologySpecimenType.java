
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PathologySpecimenType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PathologySpecimenType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="ReportID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SpecimenID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SpecimenType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FastingStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SpecimenVolume" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="SpecimenUnits" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CollectionProcedure" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AnotomicalOrigin" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SpecimenTextList" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDICommentListType" minOccurs="0"/>
 *         &lt;element name="SampleDateTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CollectionStartDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CollectionEndDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ReceivedByLabDateTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EDIInvestigationList" type="{http://www.e-mis.com/emisopen/MedicalRecord}PathologyInvestigationListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PathologySpecimenType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "reportID",
    "specimenID",
    "specimenType",
    "fastingStatus",
    "specimenVolume",
    "specimenUnits",
    "collectionProcedure",
    "anotomicalOrigin",
    "specimenTextList",
    "sampleDateTime",
    "collectionStartDate",
    "collectionEndDate",
    "receivedByLabDateTime",
    "ediInvestigationList"
})
public class PathologySpecimenType
    extends IdentType
{

    @XmlElement(name = "ReportID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String reportID;
    @XmlElement(name = "SpecimenID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String specimenID;
    @XmlElement(name = "SpecimenType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String specimenType;
    @XmlElement(name = "FastingStatus", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String fastingStatus;
    @XmlElement(name = "SpecimenVolume", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Float specimenVolume;
    @XmlElement(name = "SpecimenUnits", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String specimenUnits;
    @XmlElement(name = "CollectionProcedure", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String collectionProcedure;
    @XmlElement(name = "AnotomicalOrigin", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String anotomicalOrigin;
    @XmlElement(name = "SpecimenTextList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected EDICommentListType specimenTextList;
    @XmlElement(name = "SampleDateTime", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String sampleDateTime;
    @XmlElement(name = "CollectionStartDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String collectionStartDate;
    @XmlElement(name = "CollectionEndDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String collectionEndDate;
    @XmlElement(name = "ReceivedByLabDateTime", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String receivedByLabDateTime;
    @XmlElement(name = "EDIInvestigationList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected PathologyInvestigationListType ediInvestigationList;

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
     * Gets the value of the specimenID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecimenID() {
        return specimenID;
    }

    /**
     * Sets the value of the specimenID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecimenID(String value) {
        this.specimenID = value;
    }

    /**
     * Gets the value of the specimenType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecimenType() {
        return specimenType;
    }

    /**
     * Sets the value of the specimenType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecimenType(String value) {
        this.specimenType = value;
    }

    /**
     * Gets the value of the fastingStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFastingStatus() {
        return fastingStatus;
    }

    /**
     * Sets the value of the fastingStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFastingStatus(String value) {
        this.fastingStatus = value;
    }

    /**
     * Gets the value of the specimenVolume property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getSpecimenVolume() {
        return specimenVolume;
    }

    /**
     * Sets the value of the specimenVolume property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setSpecimenVolume(Float value) {
        this.specimenVolume = value;
    }

    /**
     * Gets the value of the specimenUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecimenUnits() {
        return specimenUnits;
    }

    /**
     * Sets the value of the specimenUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecimenUnits(String value) {
        this.specimenUnits = value;
    }

    /**
     * Gets the value of the collectionProcedure property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionProcedure() {
        return collectionProcedure;
    }

    /**
     * Sets the value of the collectionProcedure property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionProcedure(String value) {
        this.collectionProcedure = value;
    }

    /**
     * Gets the value of the anotomicalOrigin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnotomicalOrigin() {
        return anotomicalOrigin;
    }

    /**
     * Sets the value of the anotomicalOrigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnotomicalOrigin(String value) {
        this.anotomicalOrigin = value;
    }

    /**
     * Gets the value of the specimenTextList property.
     * 
     * @return
     *     possible object is
     *     {@link EDICommentListType }
     *     
     */
    public EDICommentListType getSpecimenTextList() {
        return specimenTextList;
    }

    /**
     * Sets the value of the specimenTextList property.
     * 
     * @param value
     *     allowed object is
     *     {@link EDICommentListType }
     *     
     */
    public void setSpecimenTextList(EDICommentListType value) {
        this.specimenTextList = value;
    }

    /**
     * Gets the value of the sampleDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSampleDateTime() {
        return sampleDateTime;
    }

    /**
     * Sets the value of the sampleDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSampleDateTime(String value) {
        this.sampleDateTime = value;
    }

    /**
     * Gets the value of the collectionStartDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionStartDate() {
        return collectionStartDate;
    }

    /**
     * Sets the value of the collectionStartDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionStartDate(String value) {
        this.collectionStartDate = value;
    }

    /**
     * Gets the value of the collectionEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionEndDate() {
        return collectionEndDate;
    }

    /**
     * Sets the value of the collectionEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionEndDate(String value) {
        this.collectionEndDate = value;
    }

    /**
     * Gets the value of the receivedByLabDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceivedByLabDateTime() {
        return receivedByLabDateTime;
    }

    /**
     * Sets the value of the receivedByLabDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceivedByLabDateTime(String value) {
        this.receivedByLabDateTime = value;
    }

    /**
     * Gets the value of the ediInvestigationList property.
     * 
     * @return
     *     possible object is
     *     {@link PathologyInvestigationListType }
     *     
     */
    public PathologyInvestigationListType getEDIInvestigationList() {
        return ediInvestigationList;
    }

    /**
     * Sets the value of the ediInvestigationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathologyInvestigationListType }
     *     
     */
    public void setEDIInvestigationList(PathologyInvestigationListType value) {
        this.ediInvestigationList = value;
    }

}

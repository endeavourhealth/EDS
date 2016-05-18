
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NoteType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NoteType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="AssignedDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Time" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Urgency" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="OwnerID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="AuthorID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="ActionBefore" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NoteCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NoteText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ReportID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NoteType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "assignedDate",
    "time",
    "urgency",
    "ownerID",
    "authorID",
    "actionBefore",
    "noteCode",
    "status",
    "noteText",
    "dataType",
    "reportID"
})
public class NoteType
    extends IdentType
{

    @XmlElement(name = "AssignedDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String assignedDate;
    @XmlElement(name = "Time", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String time;
    @XmlElement(name = "Urgency", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger urgency;
    @XmlElement(name = "OwnerID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType ownerID;
    @XmlElement(name = "AuthorID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType authorID;
    @XmlElement(name = "ActionBefore", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String actionBefore;
    @XmlElement(name = "NoteCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String noteCode;
    @XmlElement(name = "Status", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String status;
    @XmlElement(name = "NoteText", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String noteText;
    @XmlElement(name = "DataType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dataType;
    @XmlElement(name = "ReportID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String reportID;

    /**
     * Gets the value of the assignedDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssignedDate() {
        return assignedDate;
    }

    /**
     * Sets the value of the assignedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssignedDate(String value) {
        this.assignedDate = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTime(String value) {
        this.time = value;
    }

    /**
     * Gets the value of the urgency property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getUrgency() {
        return urgency;
    }

    /**
     * Sets the value of the urgency property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setUrgency(BigInteger value) {
        this.urgency = value;
    }

    /**
     * Gets the value of the ownerID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getOwnerID() {
        return ownerID;
    }

    /**
     * Sets the value of the ownerID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setOwnerID(IdentType value) {
        this.ownerID = value;
    }

    /**
     * Gets the value of the authorID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getAuthorID() {
        return authorID;
    }

    /**
     * Sets the value of the authorID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setAuthorID(IdentType value) {
        this.authorID = value;
    }

    /**
     * Gets the value of the actionBefore property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActionBefore() {
        return actionBefore;
    }

    /**
     * Sets the value of the actionBefore property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActionBefore(String value) {
        this.actionBefore = value;
    }

    /**
     * Gets the value of the noteCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNoteCode() {
        return noteCode;
    }

    /**
     * Sets the value of the noteCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNoteCode(String value) {
        this.noteCode = value;
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

    /**
     * Gets the value of the noteText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNoteText() {
        return noteText;
    }

    /**
     * Sets the value of the noteText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNoteText(String value) {
        this.noteText = value;
    }

    /**
     * Gets the value of the dataType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the dataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataType(String value) {
        this.dataType = value;
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

}

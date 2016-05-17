
package org.endeavourhealth.transform.emis.emisopen.schema.eomslotsforsession;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SlotStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SlotStruct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DBID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="RefID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="GUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SessionGUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="StartTime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SlotLength" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Type" type="{}TypeStruct"/>
 *         &lt;element name="Status" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PatientList" type="{}PatientListStruct" minOccurs="0"/>
 *         &lt;element name="Notes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Reason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SlotStruct", propOrder = {
    "dbid",
    "refID",
    "guid",
    "sessionGUID",
    "date",
    "startTime",
    "slotLength",
    "type",
    "status",
    "patientList",
    "notes",
    "reason"
})
public class SlotStruct {

    @XmlElement(name = "DBID")
    protected int dbid;
    @XmlElement(name = "RefID")
    protected int refID;
    @XmlElement(name = "GUID", required = true)
    protected String guid;
    @XmlElement(name = "SessionGUID", required = true)
    protected String sessionGUID;
    @XmlElement(name = "Date", required = true)
    protected String date;
    @XmlElement(name = "StartTime", required = true)
    protected String startTime;
    @XmlElement(name = "SlotLength", required = true)
    protected String slotLength;
    @XmlElement(name = "Type", required = true)
    protected TypeStruct type;
    @XmlElement(name = "Status", required = true)
    protected String status;
    @XmlElement(name = "PatientList")
    protected PatientListStruct patientList;
    @XmlElement(name = "Notes")
    protected String notes;
    @XmlElement(name = "Reason")
    protected String reason;

    /**
     * Gets the value of the dbid property.
     * 
     */
    public int getDBID() {
        return dbid;
    }

    /**
     * Sets the value of the dbid property.
     * 
     */
    public void setDBID(int value) {
        this.dbid = value;
    }

    /**
     * Gets the value of the refID property.
     * 
     */
    public int getRefID() {
        return refID;
    }

    /**
     * Sets the value of the refID property.
     * 
     */
    public void setRefID(int value) {
        this.refID = value;
    }

    /**
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGUID() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGUID(String value) {
        this.guid = value;
    }

    /**
     * Gets the value of the sessionGUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionGUID() {
        return sessionGUID;
    }

    /**
     * Sets the value of the sessionGUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionGUID(String value) {
        this.sessionGUID = value;
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate(String value) {
        this.date = value;
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartTime(String value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the slotLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSlotLength() {
        return slotLength;
    }

    /**
     * Sets the value of the slotLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSlotLength(String value) {
        this.slotLength = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link TypeStruct }
     *     
     */
    public TypeStruct getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeStruct }
     *     
     */
    public void setType(TypeStruct value) {
        this.type = value;
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
     * Gets the value of the patientList property.
     * 
     * @return
     *     possible object is
     *     {@link PatientListStruct }
     *     
     */
    public PatientListStruct getPatientList() {
        return patientList;
    }

    /**
     * Sets the value of the patientList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PatientListStruct }
     *     
     */
    public void setPatientList(PatientListStruct value) {
        this.patientList = value;
    }

    /**
     * Gets the value of the notes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotes(String value) {
        this.notes = value;
    }

    /**
     * Gets the value of the reason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReason(String value) {
        this.reason = value;
    }

}

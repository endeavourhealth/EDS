
package org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AppointmentSessionStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AppointmentSessionStruct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DBID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="GUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="StartTime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="EndTime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SlotLength" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SlotTypeList" type="{http://www.e-mis.com/emisopen/MedicalRecord}SlotTypeList"/>
 *         &lt;element name="Site" type="{http://www.e-mis.com/emisopen/MedicalRecord}SiteStruct"/>
 *         &lt;element name="HolderList" type="{http://www.e-mis.com/emisopen/MedicalRecord}HolderList"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AppointmentSessionStruct", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "dbid",
    "guid",
    "name",
    "date",
    "startTime",
    "endTime",
    "slotLength",
    "slotTypeList",
    "site",
    "holderList"
})
public class AppointmentSessionStruct {

    @XmlElement(name = "DBID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected int dbid;
    @XmlElement(name = "GUID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String guid;
    @XmlElement(name = "Name", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String name;
    @XmlElement(name = "Date", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String date;
    @XmlElement(name = "StartTime", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String startTime;
    @XmlElement(name = "EndTime", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String endTime;
    @XmlElement(name = "SlotLength", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String slotLength;
    @XmlElement(name = "SlotTypeList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected SlotTypeList slotTypeList;
    @XmlElement(name = "Site", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected SiteStruct site;
    @XmlElement(name = "HolderList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected HolderList holderList;

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
     * Gets the value of the endTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * Sets the value of the endTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndTime(String value) {
        this.endTime = value;
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
     * Gets the value of the slotTypeList property.
     * 
     * @return
     *     possible object is
     *     {@link SlotTypeList }
     *     
     */
    public SlotTypeList getSlotTypeList() {
        return slotTypeList;
    }

    /**
     * Sets the value of the slotTypeList property.
     * 
     * @param value
     *     allowed object is
     *     {@link SlotTypeList }
     *     
     */
    public void setSlotTypeList(SlotTypeList value) {
        this.slotTypeList = value;
    }

    /**
     * Gets the value of the site property.
     * 
     * @return
     *     possible object is
     *     {@link SiteStruct }
     *     
     */
    public SiteStruct getSite() {
        return site;
    }

    /**
     * Sets the value of the site property.
     * 
     * @param value
     *     allowed object is
     *     {@link SiteStruct }
     *     
     */
    public void setSite(SiteStruct value) {
        this.site = value;
    }

    /**
     * Gets the value of the holderList property.
     * 
     * @return
     *     possible object is
     *     {@link HolderList }
     *     
     */
    public HolderList getHolderList() {
        return holderList;
    }

    /**
     * Sets the value of the holderList property.
     * 
     * @param value
     *     allowed object is
     *     {@link HolderList }
     *     
     */
    public void setHolderList(HolderList value) {
        this.holderList = value;
    }

}

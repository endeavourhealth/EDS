
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.PatientContact complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.PatientContact">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ContactGUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PatientAssociateTypeId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Forenames" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Surname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="HomePhone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="WorkPhone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MobilePhone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NextOfKin" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="EmergencyContact" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="CanDiscussRecord" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Notes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LinkedPatientId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PatientAssociateTypeUserSpecified" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ContactAddress" type="{http://www.e-mis.com/emisopen}dt.Address" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.PatientContact", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "contactGUID",
    "patientAssociateTypeId",
    "title",
    "forenames",
    "surname",
    "homePhone",
    "workPhone",
    "mobilePhone",
    "nextOfKin",
    "emergencyContact",
    "canDiscussRecord",
    "notes",
    "linkedPatientId",
    "patientAssociateTypeUserSpecified",
    "contactAddress"
})
public class OpenHR001PatientContact {

    @XmlElement(name = "ContactGUID", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String contactGUID;
    @XmlElement(name = "PatientAssociateTypeId", namespace = "http://www.e-mis.com/emisopen")
    protected int patientAssociateTypeId;
    @XmlElement(name = "Title", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String title;
    @XmlElement(name = "Forenames", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String forenames;
    @XmlElement(name = "Surname", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String surname;
    @XmlElement(name = "HomePhone", namespace = "http://www.e-mis.com/emisopen")
    protected String homePhone;
    @XmlElement(name = "WorkPhone", namespace = "http://www.e-mis.com/emisopen")
    protected String workPhone;
    @XmlElement(name = "MobilePhone", namespace = "http://www.e-mis.com/emisopen")
    protected String mobilePhone;
    @XmlElement(name = "NextOfKin", namespace = "http://www.e-mis.com/emisopen")
    protected boolean nextOfKin;
    @XmlElement(name = "EmergencyContact", namespace = "http://www.e-mis.com/emisopen")
    protected boolean emergencyContact;
    @XmlElement(name = "CanDiscussRecord", namespace = "http://www.e-mis.com/emisopen")
    protected boolean canDiscussRecord;
    @XmlElement(name = "Notes", namespace = "http://www.e-mis.com/emisopen")
    protected String notes;
    @XmlElement(name = "LinkedPatientId", namespace = "http://www.e-mis.com/emisopen")
    protected String linkedPatientId;
    @XmlElement(name = "PatientAssociateTypeUserSpecified", namespace = "http://www.e-mis.com/emisopen")
    protected String patientAssociateTypeUserSpecified;
    @XmlElement(name = "ContactAddress", namespace = "http://www.e-mis.com/emisopen")
    protected List<DtAddress> contactAddress;

    /**
     * Gets the value of the contactGUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContactGUID() {
        return contactGUID;
    }

    /**
     * Sets the value of the contactGUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContactGUID(String value) {
        this.contactGUID = value;
    }

    /**
     * Gets the value of the patientAssociateTypeId property.
     * 
     */
    public int getPatientAssociateTypeId() {
        return patientAssociateTypeId;
    }

    /**
     * Sets the value of the patientAssociateTypeId property.
     * 
     */
    public void setPatientAssociateTypeId(int value) {
        this.patientAssociateTypeId = value;
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
     * Gets the value of the forenames property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getForenames() {
        return forenames;
    }

    /**
     * Sets the value of the forenames property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setForenames(String value) {
        this.forenames = value;
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
     * Gets the value of the homePhone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHomePhone() {
        return homePhone;
    }

    /**
     * Sets the value of the homePhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHomePhone(String value) {
        this.homePhone = value;
    }

    /**
     * Gets the value of the workPhone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkPhone() {
        return workPhone;
    }

    /**
     * Sets the value of the workPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkPhone(String value) {
        this.workPhone = value;
    }

    /**
     * Gets the value of the mobilePhone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMobilePhone() {
        return mobilePhone;
    }

    /**
     * Sets the value of the mobilePhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMobilePhone(String value) {
        this.mobilePhone = value;
    }

    /**
     * Gets the value of the nextOfKin property.
     * 
     */
    public boolean isNextOfKin() {
        return nextOfKin;
    }

    /**
     * Sets the value of the nextOfKin property.
     * 
     */
    public void setNextOfKin(boolean value) {
        this.nextOfKin = value;
    }

    /**
     * Gets the value of the emergencyContact property.
     * 
     */
    public boolean isEmergencyContact() {
        return emergencyContact;
    }

    /**
     * Sets the value of the emergencyContact property.
     * 
     */
    public void setEmergencyContact(boolean value) {
        this.emergencyContact = value;
    }

    /**
     * Gets the value of the canDiscussRecord property.
     * 
     */
    public boolean isCanDiscussRecord() {
        return canDiscussRecord;
    }

    /**
     * Sets the value of the canDiscussRecord property.
     * 
     */
    public void setCanDiscussRecord(boolean value) {
        this.canDiscussRecord = value;
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
     * Gets the value of the linkedPatientId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkedPatientId() {
        return linkedPatientId;
    }

    /**
     * Sets the value of the linkedPatientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkedPatientId(String value) {
        this.linkedPatientId = value;
    }

    /**
     * Gets the value of the patientAssociateTypeUserSpecified property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientAssociateTypeUserSpecified() {
        return patientAssociateTypeUserSpecified;
    }

    /**
     * Sets the value of the patientAssociateTypeUserSpecified property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientAssociateTypeUserSpecified(String value) {
        this.patientAssociateTypeUserSpecified = value;
    }

    /**
     * Gets the value of the contactAddress property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contactAddress property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContactAddress().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DtAddress }
     * 
     * 
     */
    public List<DtAddress> getContactAddress() {
        if (contactAddress == null) {
            contactAddress = new ArrayList<DtAddress>();
        }
        return this.contactAddress;
    }

}

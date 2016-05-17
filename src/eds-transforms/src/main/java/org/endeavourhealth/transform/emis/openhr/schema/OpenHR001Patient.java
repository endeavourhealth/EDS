
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.Patient complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Patient">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="patientIdentifier" type="{http://www.e-mis.com/emisopen}dt.PatientIdentifier" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="patientPerson" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="registeredGPUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="previousGPName" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="150"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="usualGPUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="dateOfEntryCountry" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="dateOfEntryArea" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="dateOfDeath" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="dead" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="archived" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="confidentialityPolicy" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="caseloadPatient" type="{http://www.e-mis.com/emisopen}OpenHR001.CaseloadPatient" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="caseloadPatientHistory" type="{http://www.e-mis.com/emisopen}OpenHR001.CaseloadPatient" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="consent" type="{http://www.e-mis.com/emisopen}dt.ConsentValue" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PatientCarerDetails" type="{http://www.e-mis.com/emisopen}OpenHR001.PatientCarer" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PatientContact" type="{http://www.e-mis.com/emisopen}OpenHR001.PatientContact" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Patient", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "patientIdentifier",
    "patientPerson",
    "registeredGPUserInRole",
    "previousGPName",
    "usualGPUserInRole",
    "dateOfEntryCountry",
    "dateOfEntryArea",
    "dateOfDeath",
    "dead",
    "archived",
    "confidentialityPolicy",
    "caseloadPatient",
    "caseloadPatientHistory",
    "consent",
    "patientCarerDetails",
    "patientContact"
})
public class OpenHR001Patient
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<DtPatientIdentifier> patientIdentifier;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String patientPerson;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String registeredGPUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String previousGPName;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String usualGPUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dateOfEntryCountry;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dateOfEntryArea;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dateOfDeath;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean dead;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean archived;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String confidentialityPolicy;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001CaseloadPatient> caseloadPatient;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001CaseloadPatient> caseloadPatientHistory;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<DtConsentValue> consent;
    @XmlElement(name = "PatientCarerDetails", namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001PatientCarer> patientCarerDetails;
    @XmlElement(name = "PatientContact", namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001PatientContact> patientContact;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the patientIdentifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patientIdentifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatientIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DtPatientIdentifier }
     * 
     * 
     */
    public List<DtPatientIdentifier> getPatientIdentifier() {
        if (patientIdentifier == null) {
            patientIdentifier = new ArrayList<DtPatientIdentifier>();
        }
        return this.patientIdentifier;
    }

    /**
     * Gets the value of the patientPerson property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientPerson() {
        return patientPerson;
    }

    /**
     * Sets the value of the patientPerson property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientPerson(String value) {
        this.patientPerson = value;
    }

    /**
     * Gets the value of the registeredGPUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegisteredGPUserInRole() {
        return registeredGPUserInRole;
    }

    /**
     * Sets the value of the registeredGPUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegisteredGPUserInRole(String value) {
        this.registeredGPUserInRole = value;
    }

    /**
     * Gets the value of the previousGPName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreviousGPName() {
        return previousGPName;
    }

    /**
     * Sets the value of the previousGPName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreviousGPName(String value) {
        this.previousGPName = value;
    }

    /**
     * Gets the value of the usualGPUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsualGPUserInRole() {
        return usualGPUserInRole;
    }

    /**
     * Sets the value of the usualGPUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsualGPUserInRole(String value) {
        this.usualGPUserInRole = value;
    }

    /**
     * Gets the value of the dateOfEntryCountry property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateOfEntryCountry() {
        return dateOfEntryCountry;
    }

    /**
     * Sets the value of the dateOfEntryCountry property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateOfEntryCountry(XMLGregorianCalendar value) {
        this.dateOfEntryCountry = value;
    }

    /**
     * Gets the value of the dateOfEntryArea property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateOfEntryArea() {
        return dateOfEntryArea;
    }

    /**
     * Sets the value of the dateOfEntryArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateOfEntryArea(XMLGregorianCalendar value) {
        this.dateOfEntryArea = value;
    }

    /**
     * Gets the value of the dateOfDeath property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateOfDeath() {
        return dateOfDeath;
    }

    /**
     * Sets the value of the dateOfDeath property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateOfDeath(XMLGregorianCalendar value) {
        this.dateOfDeath = value;
    }

    /**
     * Gets the value of the dead property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDead() {
        return dead;
    }

    /**
     * Sets the value of the dead property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDead(Boolean value) {
        this.dead = value;
    }

    /**
     * Gets the value of the archived property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isArchived() {
        return archived;
    }

    /**
     * Sets the value of the archived property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setArchived(Boolean value) {
        this.archived = value;
    }

    /**
     * Gets the value of the confidentialityPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConfidentialityPolicy() {
        return confidentialityPolicy;
    }

    /**
     * Sets the value of the confidentialityPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConfidentialityPolicy(String value) {
        this.confidentialityPolicy = value;
    }

    /**
     * Gets the value of the caseloadPatient property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the caseloadPatient property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCaseloadPatient().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001CaseloadPatient }
     * 
     * 
     */
    public List<OpenHR001CaseloadPatient> getCaseloadPatient() {
        if (caseloadPatient == null) {
            caseloadPatient = new ArrayList<OpenHR001CaseloadPatient>();
        }
        return this.caseloadPatient;
    }

    /**
     * Gets the value of the caseloadPatientHistory property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the caseloadPatientHistory property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCaseloadPatientHistory().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001CaseloadPatient }
     * 
     * 
     */
    public List<OpenHR001CaseloadPatient> getCaseloadPatientHistory() {
        if (caseloadPatientHistory == null) {
            caseloadPatientHistory = new ArrayList<OpenHR001CaseloadPatient>();
        }
        return this.caseloadPatientHistory;
    }

    /**
     * Gets the value of the consent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the consent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConsent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DtConsentValue }
     * 
     * 
     */
    public List<DtConsentValue> getConsent() {
        if (consent == null) {
            consent = new ArrayList<DtConsentValue>();
        }
        return this.consent;
    }

    /**
     * Gets the value of the patientCarerDetails property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patientCarerDetails property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatientCarerDetails().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001PatientCarer }
     * 
     * 
     */
    public List<OpenHR001PatientCarer> getPatientCarerDetails() {
        if (patientCarerDetails == null) {
            patientCarerDetails = new ArrayList<OpenHR001PatientCarer>();
        }
        return this.patientCarerDetails;
    }

    /**
     * Gets the value of the patientContact property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patientContact property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatientContact().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001PatientContact }
     * 
     * 
     */
    public List<OpenHR001PatientContact> getPatientContact() {
        if (patientContact == null) {
            patientContact = new ArrayList<OpenHR001PatientContact>();
        }
        return this.patientContact;
    }

}


package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.Event complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Event">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="patient" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="eventType" type="{http://www.e-mis.com/emisopen}voc.EventType" minOccurs="0"/>
 *         &lt;element name="effectiveTime" type="{http://www.e-mis.com/emisopen}dt.DatePart" minOccurs="0"/>
 *         &lt;element name="availabilityTimeStamp" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="authorisingUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="enteredByUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="code" type="{http://www.e-mis.com/emisopen}dt.CodeQualified" minOccurs="0"/>
 *         &lt;element name="displayTerm" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="200"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="associatedText" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.e-mis.com/emisopen}OpenHR001.AssociatedText">
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="organisation" type="{http://www.e-mis.com/emisopen}dt.uid" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="confidentialityPolicy" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="hidden" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="template" type="{http://www.e-mis.com/emisopen}OpenHR001.TemplateIdentifier" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="observation" type="{http://www.e-mis.com/emisopen}OpenHR001.Observation"/>
 *           &lt;element name="diary" type="{http://www.e-mis.com/emisopen}OpenHR001.Diary"/>
 *           &lt;element name="referral" type="{http://www.e-mis.com/emisopen}OpenHR001.Referral"/>
 *           &lt;element name="medication" type="{http://www.e-mis.com/emisopen}OpenHR001.Medication"/>
 *           &lt;element name="medicationIssue" type="{http://www.e-mis.com/emisopen}OpenHR001.MedicationIssue"/>
 *           &lt;element name="report" type="{http://www.e-mis.com/emisopen}OpenHR001.Report"/>
 *           &lt;element name="orderHeader" type="{http://www.e-mis.com/emisopen}OpenHR001.OrderHeader"/>
 *           &lt;element name="alert" type="{http://www.e-mis.com/emisopen}OpenHR001.Alert"/>
 *           &lt;element name="allergy" type="{http://www.e-mis.com/emisopen}OpenHR001.Allergy"/>
 *         &lt;/choice>
 *         &lt;element name="annotations" type="{http://www.e-mis.com/emisopen}OpenHR001.Annotations" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="unFiled" type="{http://www.e-mis.com/emisopen}dt.bool" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Event", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "patient",
    "eventType",
    "effectiveTime",
    "availabilityTimeStamp",
    "authorisingUserInRole",
    "enteredByUserInRole",
    "code",
    "displayTerm",
    "associatedText",
    "organisation",
    "confidentialityPolicy",
    "hidden",
    "template",
    "observation",
    "diary",
    "referral",
    "medication",
    "medicationIssue",
    "report",
    "orderHeader",
    "alert",
    "allergy",
    "annotations"
})
@XmlSeeAlso({
    OpenHR001HealthDomain.Event.class
})
public class OpenHR001Event
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String patient;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocEventType eventType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtDatePart effectiveTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar availabilityTimeStamp;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String authorisingUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String enteredByUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtCodeQualified code;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String displayTerm;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Event.AssociatedText> associatedText;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> organisation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String confidentialityPolicy;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", defaultValue = "false")
    protected Boolean hidden;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001TemplateIdentifier template;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Observation observation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Diary diary;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Referral referral;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Medication medication;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001MedicationIssue medicationIssue;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Report report;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001OrderHeader orderHeader;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Alert alert;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Allergy allergy;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Annotations annotations;
    @XmlAttribute(name = "unFiled")
    protected Boolean unFiled;

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
     * Gets the value of the patient property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatient() {
        return patient;
    }

    /**
     * Sets the value of the patient property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatient(String value) {
        this.patient = value;
    }

    /**
     * Gets the value of the eventType property.
     * 
     * @return
     *     possible object is
     *     {@link VocEventType }
     *     
     */
    public VocEventType getEventType() {
        return eventType;
    }

    /**
     * Sets the value of the eventType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocEventType }
     *     
     */
    public void setEventType(VocEventType value) {
        this.eventType = value;
    }

    /**
     * Gets the value of the effectiveTime property.
     * 
     * @return
     *     possible object is
     *     {@link DtDatePart }
     *     
     */
    public DtDatePart getEffectiveTime() {
        return effectiveTime;
    }

    /**
     * Sets the value of the effectiveTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtDatePart }
     *     
     */
    public void setEffectiveTime(DtDatePart value) {
        this.effectiveTime = value;
    }

    /**
     * Gets the value of the availabilityTimeStamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAvailabilityTimeStamp() {
        return availabilityTimeStamp;
    }

    /**
     * Sets the value of the availabilityTimeStamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAvailabilityTimeStamp(XMLGregorianCalendar value) {
        this.availabilityTimeStamp = value;
    }

    /**
     * Gets the value of the authorisingUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthorisingUserInRole() {
        return authorisingUserInRole;
    }

    /**
     * Sets the value of the authorisingUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthorisingUserInRole(String value) {
        this.authorisingUserInRole = value;
    }

    /**
     * Gets the value of the enteredByUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnteredByUserInRole() {
        return enteredByUserInRole;
    }

    /**
     * Sets the value of the enteredByUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnteredByUserInRole(String value) {
        this.enteredByUserInRole = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link DtCodeQualified }
     *     
     */
    public DtCodeQualified getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCodeQualified }
     *     
     */
    public void setCode(DtCodeQualified value) {
        this.code = value;
    }

    /**
     * Gets the value of the displayTerm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayTerm() {
        return displayTerm;
    }

    /**
     * Sets the value of the displayTerm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayTerm(String value) {
        this.displayTerm = value;
    }

    /**
     * Gets the value of the associatedText property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the associatedText property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAssociatedText().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Event.AssociatedText }
     * 
     * 
     */
    public List<OpenHR001Event.AssociatedText> getAssociatedText() {
        if (associatedText == null) {
            associatedText = new ArrayList<OpenHR001Event.AssociatedText>();
        }
        return this.associatedText;
    }

    /**
     * Gets the value of the organisation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the organisation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrganisation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOrganisation() {
        if (organisation == null) {
            organisation = new ArrayList<String>();
        }
        return this.organisation;
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
     * Gets the value of the hidden property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHidden() {
        return hidden;
    }

    /**
     * Sets the value of the hidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHidden(Boolean value) {
        this.hidden = value;
    }

    /**
     * Gets the value of the template property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001TemplateIdentifier }
     *     
     */
    public OpenHR001TemplateIdentifier getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001TemplateIdentifier }
     *     
     */
    public void setTemplate(OpenHR001TemplateIdentifier value) {
        this.template = value;
    }

    /**
     * Gets the value of the observation property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Observation }
     *     
     */
    public OpenHR001Observation getObservation() {
        return observation;
    }

    /**
     * Sets the value of the observation property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Observation }
     *     
     */
    public void setObservation(OpenHR001Observation value) {
        this.observation = value;
    }

    /**
     * Gets the value of the diary property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Diary }
     *     
     */
    public OpenHR001Diary getDiary() {
        return diary;
    }

    /**
     * Sets the value of the diary property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Diary }
     *     
     */
    public void setDiary(OpenHR001Diary value) {
        this.diary = value;
    }

    /**
     * Gets the value of the referral property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Referral }
     *     
     */
    public OpenHR001Referral getReferral() {
        return referral;
    }

    /**
     * Sets the value of the referral property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Referral }
     *     
     */
    public void setReferral(OpenHR001Referral value) {
        this.referral = value;
    }

    /**
     * Gets the value of the medication property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Medication }
     *     
     */
    public OpenHR001Medication getMedication() {
        return medication;
    }

    /**
     * Sets the value of the medication property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Medication }
     *     
     */
    public void setMedication(OpenHR001Medication value) {
        this.medication = value;
    }

    /**
     * Gets the value of the medicationIssue property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001MedicationIssue }
     *     
     */
    public OpenHR001MedicationIssue getMedicationIssue() {
        return medicationIssue;
    }

    /**
     * Sets the value of the medicationIssue property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001MedicationIssue }
     *     
     */
    public void setMedicationIssue(OpenHR001MedicationIssue value) {
        this.medicationIssue = value;
    }

    /**
     * Gets the value of the report property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Report }
     *     
     */
    public OpenHR001Report getReport() {
        return report;
    }

    /**
     * Sets the value of the report property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Report }
     *     
     */
    public void setReport(OpenHR001Report value) {
        this.report = value;
    }

    /**
     * Gets the value of the orderHeader property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001OrderHeader }
     *     
     */
    public OpenHR001OrderHeader getOrderHeader() {
        return orderHeader;
    }

    /**
     * Sets the value of the orderHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001OrderHeader }
     *     
     */
    public void setOrderHeader(OpenHR001OrderHeader value) {
        this.orderHeader = value;
    }

    /**
     * Gets the value of the alert property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Alert }
     *     
     */
    public OpenHR001Alert getAlert() {
        return alert;
    }

    /**
     * Sets the value of the alert property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Alert }
     *     
     */
    public void setAlert(OpenHR001Alert value) {
        this.alert = value;
    }

    /**
     * Gets the value of the allergy property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Allergy }
     *     
     */
    public OpenHR001Allergy getAllergy() {
        return allergy;
    }

    /**
     * Sets the value of the allergy property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Allergy }
     *     
     */
    public void setAllergy(OpenHR001Allergy value) {
        this.allergy = value;
    }

    /**
     * Gets the value of the annotations property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Annotations }
     *     
     */
    public OpenHR001Annotations getAnnotations() {
        return annotations;
    }

    /**
     * Sets the value of the annotations property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Annotations }
     *     
     */
    public void setAnnotations(OpenHR001Annotations value) {
        this.annotations = value;
    }

    /**
     * Gets the value of the unFiled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUnFiled() {
        return unFiled;
    }

    /**
     * Sets the value of the unFiled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUnFiled(Boolean value) {
        this.unFiled = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.e-mis.com/emisopen}OpenHR001.AssociatedText">
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class AssociatedText
        extends OpenHR001AssociatedText
    {


    }

}

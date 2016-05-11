
package org.endeavourhealth.core.transform.tpp.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Event complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Event">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Medication" type="{}Medication" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ClinicalCode" type="{}ClinicalCode" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Reminder" type="{}Reminder" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Recall" type="{}Recall" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Vaccination" type="{}Vaccination" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="RepeatMedication" type="{}RepeatMedication" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Letter" type="{}Letter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PatientPlan" type="{}PatientPlan" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="CarePlan" type="{}CarePlan" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Relationship" type="{}Relationship" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Report" type="{}Report" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Attachment" type="{}Attachment" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Activity" type="{}Activity" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Narrative" type="{}Narrative" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="DrugSensitivity" type="{}DrugSensitivity" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="LinkedProblemUID" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="DateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="UserName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="DoneBy" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="DoneAt" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Software" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Method" type="{}EventMethod" />
 *       &lt;attribute name="LinkedReferralUID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="EventUID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Event", propOrder = {
    "medication",
    "clinicalCode",
    "reminder",
    "recall",
    "vaccination",
    "repeatMedication",
    "letter",
    "patientPlan",
    "carePlan",
    "relationship",
    "report",
    "attachment",
    "activity",
    "narrative",
    "drugSensitivity",
    "linkedProblemUID"
})
public class Event {

    @XmlElement(name = "Medication")
    protected List<Medication> medication;
    @XmlElement(name = "ClinicalCode")
    protected List<ClinicalCode> clinicalCode;
    @XmlElement(name = "Reminder")
    protected List<Reminder> reminder;
    @XmlElement(name = "Recall")
    protected List<Recall> recall;
    @XmlElement(name = "Vaccination")
    protected List<Vaccination> vaccination;
    @XmlElement(name = "RepeatMedication")
    protected List<RepeatMedication> repeatMedication;
    @XmlElement(name = "Letter")
    protected List<Letter> letter;
    @XmlElement(name = "PatientPlan")
    protected List<PatientPlan> patientPlan;
    @XmlElement(name = "CarePlan")
    protected List<CarePlan> carePlan;
    @XmlElement(name = "Relationship")
    protected List<Relationship> relationship;
    @XmlElement(name = "Report")
    protected List<Report> report;
    @XmlElement(name = "Attachment")
    protected List<Attachment> attachment;
    @XmlElement(name = "Activity")
    protected List<Activity> activity;
    @XmlElement(name = "Narrative")
    protected List<Narrative> narrative;
    @XmlElement(name = "DrugSensitivity")
    protected List<DrugSensitivity> drugSensitivity;
    @XmlElement(name = "LinkedProblemUID")
    protected List<String> linkedProblemUID;
    @XmlAttribute(name = "DateTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTime;
    @XmlAttribute(name = "UserName")
    protected String userName;
    @XmlAttribute(name = "DoneBy")
    protected String doneBy;
    @XmlAttribute(name = "DoneAt")
    protected String doneAt;
    @XmlAttribute(name = "Software")
    protected String software;
    @XmlAttribute(name = "Method")
    protected EventMethod method;
    @XmlAttribute(name = "LinkedReferralUID")
    protected String linkedReferralUID;
    @XmlAttribute(name = "EventUID")
    protected String eventUID;

    /**
     * Gets the value of the medication property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the medication property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMedication().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Medication }
     * 
     * 
     */
    public List<Medication> getMedication() {
        if (medication == null) {
            medication = new ArrayList<Medication>();
        }
        return this.medication;
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
     * {@link ClinicalCode }
     * 
     * 
     */
    public List<ClinicalCode> getClinicalCode() {
        if (clinicalCode == null) {
            clinicalCode = new ArrayList<ClinicalCode>();
        }
        return this.clinicalCode;
    }

    /**
     * Gets the value of the reminder property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reminder property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReminder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Reminder }
     * 
     * 
     */
    public List<Reminder> getReminder() {
        if (reminder == null) {
            reminder = new ArrayList<Reminder>();
        }
        return this.reminder;
    }

    /**
     * Gets the value of the recall property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the recall property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRecall().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Recall }
     * 
     * 
     */
    public List<Recall> getRecall() {
        if (recall == null) {
            recall = new ArrayList<Recall>();
        }
        return this.recall;
    }

    /**
     * Gets the value of the vaccination property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vaccination property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVaccination().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Vaccination }
     * 
     * 
     */
    public List<Vaccination> getVaccination() {
        if (vaccination == null) {
            vaccination = new ArrayList<Vaccination>();
        }
        return this.vaccination;
    }

    /**
     * Gets the value of the repeatMedication property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the repeatMedication property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRepeatMedication().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RepeatMedication }
     * 
     * 
     */
    public List<RepeatMedication> getRepeatMedication() {
        if (repeatMedication == null) {
            repeatMedication = new ArrayList<RepeatMedication>();
        }
        return this.repeatMedication;
    }

    /**
     * Gets the value of the letter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the letter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLetter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Letter }
     * 
     * 
     */
    public List<Letter> getLetter() {
        if (letter == null) {
            letter = new ArrayList<Letter>();
        }
        return this.letter;
    }

    /**
     * Gets the value of the patientPlan property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patientPlan property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatientPlan().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PatientPlan }
     * 
     * 
     */
    public List<PatientPlan> getPatientPlan() {
        if (patientPlan == null) {
            patientPlan = new ArrayList<PatientPlan>();
        }
        return this.patientPlan;
    }

    /**
     * Gets the value of the carePlan property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the carePlan property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCarePlan().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CarePlan }
     * 
     * 
     */
    public List<CarePlan> getCarePlan() {
        if (carePlan == null) {
            carePlan = new ArrayList<CarePlan>();
        }
        return this.carePlan;
    }

    /**
     * Gets the value of the relationship property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relationship property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelationship().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Relationship }
     * 
     * 
     */
    public List<Relationship> getRelationship() {
        if (relationship == null) {
            relationship = new ArrayList<Relationship>();
        }
        return this.relationship;
    }

    /**
     * Gets the value of the report property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the report property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReport().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Report }
     * 
     * 
     */
    public List<Report> getReport() {
        if (report == null) {
            report = new ArrayList<Report>();
        }
        return this.report;
    }

    /**
     * Gets the value of the attachment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attachment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttachment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Attachment }
     * 
     * 
     */
    public List<Attachment> getAttachment() {
        if (attachment == null) {
            attachment = new ArrayList<Attachment>();
        }
        return this.attachment;
    }

    /**
     * Gets the value of the activity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the activity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActivity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Activity }
     * 
     * 
     */
    public List<Activity> getActivity() {
        if (activity == null) {
            activity = new ArrayList<Activity>();
        }
        return this.activity;
    }

    /**
     * Gets the value of the narrative property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the narrative property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNarrative().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Narrative }
     * 
     * 
     */
    public List<Narrative> getNarrative() {
        if (narrative == null) {
            narrative = new ArrayList<Narrative>();
        }
        return this.narrative;
    }

    /**
     * Gets the value of the drugSensitivity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the drugSensitivity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDrugSensitivity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DrugSensitivity }
     * 
     * 
     */
    public List<DrugSensitivity> getDrugSensitivity() {
        if (drugSensitivity == null) {
            drugSensitivity = new ArrayList<DrugSensitivity>();
        }
        return this.drugSensitivity;
    }

    /**
     * Gets the value of the linkedProblemUID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkedProblemUID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkedProblemUID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLinkedProblemUID() {
        if (linkedProblemUID == null) {
            linkedProblemUID = new ArrayList<String>();
        }
        return this.linkedProblemUID;
    }

    /**
     * Gets the value of the dateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTime() {
        return dateTime;
    }

    /**
     * Sets the value of the dateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTime(XMLGregorianCalendar value) {
        this.dateTime = value;
    }

    /**
     * Gets the value of the userName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of the doneBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDoneBy() {
        return doneBy;
    }

    /**
     * Sets the value of the doneBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDoneBy(String value) {
        this.doneBy = value;
    }

    /**
     * Gets the value of the doneAt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDoneAt() {
        return doneAt;
    }

    /**
     * Sets the value of the doneAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDoneAt(String value) {
        this.doneAt = value;
    }

    /**
     * Gets the value of the software property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSoftware() {
        return software;
    }

    /**
     * Sets the value of the software property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSoftware(String value) {
        this.software = value;
    }

    /**
     * Gets the value of the method property.
     * 
     * @return
     *     possible object is
     *     {@link EventMethod }
     *     
     */
    public EventMethod getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventMethod }
     *     
     */
    public void setMethod(EventMethod value) {
        this.method = value;
    }

    /**
     * Gets the value of the linkedReferralUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkedReferralUID() {
        return linkedReferralUID;
    }

    /**
     * Sets the value of the linkedReferralUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkedReferralUID(String value) {
        this.linkedReferralUID = value;
    }

    /**
     * Gets the value of the eventUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventUID() {
        return eventUID;
    }

    /**
     * Sets the value of the eventUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventUID(String value) {
        this.eventUID = value;
    }

}

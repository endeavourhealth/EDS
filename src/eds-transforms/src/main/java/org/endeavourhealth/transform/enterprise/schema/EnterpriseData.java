
package org.endeavourhealth.transform.enterprise.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for enterpriseData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="enterpriseData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="organisation" type="{}organisation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="practitioner" type="{}practitioner" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="schedule" type="{}schedule" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="allergy_intolerance" type="{}allergy_intolerance" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="appointment" type="{}appointment" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="condition" type="{}condition" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="diagnostic_order" type="{}diagnostic_order" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="encounter" type="{}encounter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="family_member_history" type="{}family_member_history" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="immunisation" type="{}immunisation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="medication_order" type="{}medication_order" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="medication_statement" type="{}medication_statement" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="observation" type="{}observation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="patient" type="{}patient" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="procedure" type="{}procedure" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="procedure_request" type="{}procedure_request" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="referral_request" type="{}referral_request" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "enterpriseData", propOrder = {
    "organisation",
    "practitioner",
    "schedule",
    "allergyIntolerance",
    "appointment",
    "condition",
    "diagnosticOrder",
    "encounter",
    "familyMemberHistory",
    "immunisation",
    "medicationOrder",
    "medicationStatement",
    "observation",
    "patient",
    "procedure",
    "procedureRequest",
    "referralRequest"
})
public class EnterpriseData {

    protected List<Organisation> organisation;
    protected List<Practitioner> practitioner;
    protected List<Schedule> schedule;
    @XmlElement(name = "allergy_intolerance")
    protected List<AllergyIntolerance> allergyIntolerance;
    protected List<Appointment> appointment;
    protected List<Condition> condition;
    @XmlElement(name = "diagnostic_order")
    protected List<DiagnosticOrder> diagnosticOrder;
    protected List<Encounter> encounter;
    @XmlElement(name = "family_member_history")
    protected List<FamilyMemberHistory> familyMemberHistory;
    protected List<Immunisation> immunisation;
    @XmlElement(name = "medication_order")
    protected List<MedicationOrder> medicationOrder;
    @XmlElement(name = "medication_statement")
    protected List<MedicationStatement> medicationStatement;
    protected List<Observation> observation;
    protected List<Patient> patient;
    protected List<Procedure> procedure;
    @XmlElement(name = "procedure_request")
    protected List<ProcedureRequest> procedureRequest;
    @XmlElement(name = "referral_request")
    protected List<ReferralRequest> referralRequest;

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
     * {@link Organisation }
     * 
     * 
     */
    public List<Organisation> getOrganisation() {
        if (organisation == null) {
            organisation = new ArrayList<Organisation>();
        }
        return this.organisation;
    }

    /**
     * Gets the value of the practitioner property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the practitioner property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPractitioner().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Practitioner }
     * 
     * 
     */
    public List<Practitioner> getPractitioner() {
        if (practitioner == null) {
            practitioner = new ArrayList<Practitioner>();
        }
        return this.practitioner;
    }

    /**
     * Gets the value of the schedule property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the schedule property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSchedule().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Schedule }
     * 
     * 
     */
    public List<Schedule> getSchedule() {
        if (schedule == null) {
            schedule = new ArrayList<Schedule>();
        }
        return this.schedule;
    }

    /**
     * Gets the value of the allergyIntolerance property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the allergyIntolerance property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAllergyIntolerance().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AllergyIntolerance }
     * 
     * 
     */
    public List<AllergyIntolerance> getAllergyIntolerance() {
        if (allergyIntolerance == null) {
            allergyIntolerance = new ArrayList<AllergyIntolerance>();
        }
        return this.allergyIntolerance;
    }

    /**
     * Gets the value of the appointment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the appointment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAppointment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Appointment }
     * 
     * 
     */
    public List<Appointment> getAppointment() {
        if (appointment == null) {
            appointment = new ArrayList<Appointment>();
        }
        return this.appointment;
    }

    /**
     * Gets the value of the condition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the condition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCondition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Condition }
     * 
     * 
     */
    public List<Condition> getCondition() {
        if (condition == null) {
            condition = new ArrayList<Condition>();
        }
        return this.condition;
    }

    /**
     * Gets the value of the diagnosticOrder property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the diagnosticOrder property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDiagnosticOrder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DiagnosticOrder }
     * 
     * 
     */
    public List<DiagnosticOrder> getDiagnosticOrder() {
        if (diagnosticOrder == null) {
            diagnosticOrder = new ArrayList<DiagnosticOrder>();
        }
        return this.diagnosticOrder;
    }

    /**
     * Gets the value of the encounter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the encounter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEncounter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Encounter }
     * 
     * 
     */
    public List<Encounter> getEncounter() {
        if (encounter == null) {
            encounter = new ArrayList<Encounter>();
        }
        return this.encounter;
    }

    /**
     * Gets the value of the familyMemberHistory property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the familyMemberHistory property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFamilyMemberHistory().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FamilyMemberHistory }
     * 
     * 
     */
    public List<FamilyMemberHistory> getFamilyMemberHistory() {
        if (familyMemberHistory == null) {
            familyMemberHistory = new ArrayList<FamilyMemberHistory>();
        }
        return this.familyMemberHistory;
    }

    /**
     * Gets the value of the immunisation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the immunisation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImmunisation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Immunisation }
     * 
     * 
     */
    public List<Immunisation> getImmunisation() {
        if (immunisation == null) {
            immunisation = new ArrayList<Immunisation>();
        }
        return this.immunisation;
    }

    /**
     * Gets the value of the medicationOrder property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the medicationOrder property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMedicationOrder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MedicationOrder }
     * 
     * 
     */
    public List<MedicationOrder> getMedicationOrder() {
        if (medicationOrder == null) {
            medicationOrder = new ArrayList<MedicationOrder>();
        }
        return this.medicationOrder;
    }

    /**
     * Gets the value of the medicationStatement property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the medicationStatement property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMedicationStatement().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MedicationStatement }
     * 
     * 
     */
    public List<MedicationStatement> getMedicationStatement() {
        if (medicationStatement == null) {
            medicationStatement = new ArrayList<MedicationStatement>();
        }
        return this.medicationStatement;
    }

    /**
     * Gets the value of the observation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the observation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObservation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Observation }
     * 
     * 
     */
    public List<Observation> getObservation() {
        if (observation == null) {
            observation = new ArrayList<Observation>();
        }
        return this.observation;
    }

    /**
     * Gets the value of the patient property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patient property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatient().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Patient }
     * 
     * 
     */
    public List<Patient> getPatient() {
        if (patient == null) {
            patient = new ArrayList<Patient>();
        }
        return this.patient;
    }

    /**
     * Gets the value of the procedure property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the procedure property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProcedure().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Procedure }
     * 
     * 
     */
    public List<Procedure> getProcedure() {
        if (procedure == null) {
            procedure = new ArrayList<Procedure>();
        }
        return this.procedure;
    }

    /**
     * Gets the value of the procedureRequest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the procedureRequest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProcedureRequest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProcedureRequest }
     * 
     * 
     */
    public List<ProcedureRequest> getProcedureRequest() {
        if (procedureRequest == null) {
            procedureRequest = new ArrayList<ProcedureRequest>();
        }
        return this.procedureRequest;
    }

    /**
     * Gets the value of the referralRequest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referralRequest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferralRequest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReferralRequest }
     * 
     * 
     */
    public List<ReferralRequest> getReferralRequest() {
        if (referralRequest == null) {
            referralRequest = new ArrayList<ReferralRequest>();
        }
        return this.referralRequest;
    }

}

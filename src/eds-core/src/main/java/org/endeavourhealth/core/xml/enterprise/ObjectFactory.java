
package org.endeavourhealth.core.xml.enterprise;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.core.xml.enterprise package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _EnterpriseData_QNAME = new QName("", "enterpriseData");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.core.xml.enterprise
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EnterpriseData }
     * 
     */
    public EnterpriseData createEnterpriseData() {
        return new EnterpriseData();
    }

    /**
     * Create an instance of {@link MedicationOrder }
     * 
     */
    public MedicationOrder createMedicationOrder() {
        return new MedicationOrder();
    }

    /**
     * Create an instance of {@link AllergyIntolerance }
     * 
     */
    public AllergyIntolerance createAllergyIntolerance() {
        return new AllergyIntolerance();
    }

    /**
     * Create an instance of {@link Practitioner }
     * 
     */
    public Practitioner createPractitioner() {
        return new Practitioner();
    }

    /**
     * Create an instance of {@link Observation }
     * 
     */
    public Observation createObservation() {
        return new Observation();
    }

    /**
     * Create an instance of {@link Appointment }
     * 
     */
    public Appointment createAppointment() {
        return new Appointment();
    }

    /**
     * Create an instance of {@link FamilyMemberHistory }
     * 
     */
    public FamilyMemberHistory createFamilyMemberHistory() {
        return new FamilyMemberHistory();
    }

    /**
     * Create an instance of {@link MedicationStatement }
     * 
     */
    public MedicationStatement createMedicationStatement() {
        return new MedicationStatement();
    }

    /**
     * Create an instance of {@link DiagnosticReport }
     * 
     */
    public DiagnosticReport createDiagnosticReport() {
        return new DiagnosticReport();
    }

    /**
     * Create an instance of {@link Encounter }
     * 
     */
    public Encounter createEncounter() {
        return new Encounter();
    }

    /**
     * Create an instance of {@link Procedure }
     * 
     */
    public Procedure createProcedure() {
        return new Procedure();
    }

    /**
     * Create an instance of {@link BaseRecord }
     * 
     */
    public BaseRecord createBaseRecord() {
        return new BaseRecord();
    }

    /**
     * Create an instance of {@link Schedule }
     * 
     */
    public Schedule createSchedule() {
        return new Schedule();
    }

    /**
     * Create an instance of {@link Condition }
     * 
     */
    public Condition createCondition() {
        return new Condition();
    }

    /**
     * Create an instance of {@link Immunization }
     * 
     */
    public Immunization createImmunization() {
        return new Immunization();
    }

    /**
     * Create an instance of {@link Patient }
     * 
     */
    public Patient createPatient() {
        return new Patient();
    }

    /**
     * Create an instance of {@link Organization }
     * 
     */
    public Organization createOrganization() {
        return new Organization();
    }

    /**
     * Create an instance of {@link ReferralRequest }
     * 
     */
    public ReferralRequest createReferralRequest() {
        return new ReferralRequest();
    }

    /**
     * Create an instance of {@link DiagnosticOrder }
     * 
     */
    public DiagnosticOrder createDiagnosticOrder() {
        return new DiagnosticOrder();
    }

    /**
     * Create an instance of {@link ProcedureRequest }
     * 
     */
    public ProcedureRequest createProcedureRequest() {
        return new ProcedureRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnterpriseData }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "enterpriseData")
    public JAXBElement<EnterpriseData> createEnterpriseData(EnterpriseData value) {
        return new JAXBElement<EnterpriseData>(_EnterpriseData_QNAME, EnterpriseData.class, null, value);
    }

}

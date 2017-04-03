
package org.endeavourhealth.transform.vitrucare.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.transform.vitrucare.model package. 
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

    private final static QName _PatientCreate_QNAME = new QName("", "patientCreate");
    private final static QName _PatientUpdate_QNAME = new QName("", "patientUpdate");
    private final static QName _PatientReplace_QNAME = new QName("", "patientReplace");
    private final static QName _PatientDataDelete_QNAME = new QName("", "patientDataDelete");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.transform.vitrucare.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Payload }
     * 
     */
    public Payload createPayload() {
        return new Payload();
    }

    /**
     * Create an instance of {@link ClinicalTerm }
     * 
     */
    public ClinicalTerm createClinicalTerm() {
        return new ClinicalTerm();
    }

    /**
     * Create an instance of {@link Medication }
     * 
     */
    public Medication createMedication() {
        return new Medication();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Payload }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "patientCreate")
    public JAXBElement<Payload> createPatientCreate(Payload value) {
        return new JAXBElement<Payload>(_PatientCreate_QNAME, Payload.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Payload }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "patientUpdate")
    public JAXBElement<Payload> createPatientUpdate(Payload value) {
        return new JAXBElement<Payload>(_PatientUpdate_QNAME, Payload.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Payload }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "patientReplace")
    public JAXBElement<Payload> createPatientReplace(Payload value) {
        return new JAXBElement<Payload>(_PatientReplace_QNAME, Payload.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Payload }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "patientDataDelete")
    public JAXBElement<Payload> createPatientDataDelete(Payload value) {
        return new JAXBElement<Payload>(_PatientDataDelete_QNAME, Payload.class, null, value);
    }

}

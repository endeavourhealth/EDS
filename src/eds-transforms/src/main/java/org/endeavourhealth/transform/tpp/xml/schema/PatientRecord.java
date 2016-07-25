
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PatientRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PatientRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MessageUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PatientUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="OrganisationID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CsvPatient" type="{}CsvPatient"/>
 *         &lt;element name="CsvMetadata" type="{}CsvMetadata"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PatientRecord", propOrder = {
    "messageUID",
    "patientUID",
    "organisationID",
    "patient",
    "metadata"
})
public class PatientRecord {

    @XmlElement(name = "MessageUID", required = true)
    protected String messageUID;
    @XmlElement(name = "PatientUID", required = true)
    protected String patientUID;
    @XmlElement(name = "OrganisationID", required = true)
    protected String organisationID;
    @XmlElement(name = "CsvPatient", required = true)
    protected Patient patient;
    @XmlElement(name = "CsvMetadata", required = true)
    protected Metadata metadata;

    /**
     * Gets the value of the messageUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageUID() {
        return messageUID;
    }

    /**
     * Sets the value of the messageUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageUID(String value) {
        this.messageUID = value;
    }

    /**
     * Gets the value of the patientUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientUID() {
        return patientUID;
    }

    /**
     * Sets the value of the patientUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientUID(String value) {
        this.patientUID = value;
    }

    /**
     * Gets the value of the organisationID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisationID() {
        return organisationID;
    }

    /**
     * Sets the value of the organisationID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisationID(String value) {
        this.organisationID = value;
    }

    /**
     * Gets the value of the patient property.
     * 
     * @return
     *     possible object is
     *     {@link Patient }
     *     
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * Sets the value of the patient property.
     * 
     * @param value
     *     allowed object is
     *     {@link Patient }
     *     
     */
    public void setPatient(Patient value) {
        this.patient = value;
    }

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link Metadata }
     *     
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link Metadata }
     *     
     */
    public void setMetadata(Metadata value) {
        this.metadata = value;
    }

}


package org.endeavourhealth.transform.emis.emisopen.schema.eomslotsforsession;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PatientListStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PatientListStruct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Patient" type="{}PatientStruct"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PatientListStruct", propOrder = {
    "patient"
})
public class PatientListStruct {

    @XmlElement(name = "Patient", required = true)
    protected PatientStruct patient;

    /**
     * Gets the value of the patient property.
     * 
     * @return
     *     possible object is
     *     {@link PatientStruct }
     *     
     */
    public PatientStruct getPatient() {
        return patient;
    }

    /**
     * Sets the value of the patient property.
     * 
     * @param value
     *     allowed object is
     *     {@link PatientStruct }
     *     
     */
    public void setPatient(PatientStruct value) {
        this.patient = value;
    }

}

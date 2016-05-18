
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegistrationHistoryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegistrationHistoryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DateRegistered" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DateDeregistered" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PatientType" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegistrationHistoryType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "dateRegistered",
    "dateDeregistered",
    "patientType"
})
public class RegistrationHistoryType {

    @XmlElement(name = "DateRegistered", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateRegistered;
    @XmlElement(name = "DateDeregistered", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateDeregistered;
    @XmlElement(name = "PatientType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IntegerCodeType patientType;

    /**
     * Gets the value of the dateRegistered property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateRegistered() {
        return dateRegistered;
    }

    /**
     * Sets the value of the dateRegistered property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateRegistered(String value) {
        this.dateRegistered = value;
    }

    /**
     * Gets the value of the dateDeregistered property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateDeregistered() {
        return dateDeregistered;
    }

    /**
     * Sets the value of the dateDeregistered property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateDeregistered(String value) {
        this.dateDeregistered = value;
    }

    /**
     * Gets the value of the patientType property.
     * 
     * @return
     *     possible object is
     *     {@link IntegerCodeType }
     *     
     */
    public IntegerCodeType getPatientType() {
        return patientType;
    }

    /**
     * Sets the value of the patientType property.
     * 
     * @param value
     *     allowed object is
     *     {@link IntegerCodeType }
     *     
     */
    public void setPatientType(IntegerCodeType value) {
        this.patientType = value;
    }

}

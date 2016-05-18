
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StatusType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StatusType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="Code" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType" minOccurs="0"/>
 *         &lt;element name="StatusDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PatientType" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatusType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "code",
    "statusDate",
    "patientType"
})
@XmlSeeAlso({
    org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.RegistrationStatusType.CurrentStatus.class
})
public class StatusType
    extends IdentType
{

    @XmlElement(name = "Code", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IntegerCodeType code;
    @XmlElement(name = "StatusDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String statusDate;
    @XmlElement(name = "PatientType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IntegerCodeType patientType;

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link IntegerCodeType }
     *     
     */
    public IntegerCodeType getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link IntegerCodeType }
     *     
     */
    public void setCode(IntegerCodeType value) {
        this.code = value;
    }

    /**
     * Gets the value of the statusDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusDate() {
        return statusDate;
    }

    /**
     * Sets the value of the statusDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusDate(String value) {
        this.statusDate = value;
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

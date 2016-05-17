
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.PatientStatus complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.PatientStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="status" type="{http://www.e-mis.com/emisopen}voc.PatientStatus"/>
 *         &lt;element name="caseloadStatus" type="{http://www.e-mis.com/emisopen}voc.CaseloadPatientStatus" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.PatientStatus", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "status",
    "caseloadStatus"
})
public class OpenHR001PatientStatus {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String status;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocCaseloadPatientStatus caseloadStatus;

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the caseloadStatus property.
     * 
     * @return
     *     possible object is
     *     {@link VocCaseloadPatientStatus }
     *     
     */
    public VocCaseloadPatientStatus getCaseloadStatus() {
        return caseloadStatus;
    }

    /**
     * Sets the value of the caseloadStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocCaseloadPatientStatus }
     *     
     */
    public void setCaseloadStatus(VocCaseloadPatientStatus value) {
        this.caseloadStatus = value;
    }

}

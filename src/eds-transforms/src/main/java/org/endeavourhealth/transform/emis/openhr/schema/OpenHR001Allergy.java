
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.Allergy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Allergy">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="targetDrug" type="{http://www.e-mis.com/emisopen}dt.Code" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="resultStatus" type="{http://www.e-mis.com/emisopen}voc.ObservationResultStatus" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Allergy", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "targetDrug"
})
public class OpenHR001Allergy {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtCode targetDrug;
    @XmlAttribute(name = "resultStatus")
    protected VocObservationResultStatus resultStatus;

    /**
     * Gets the value of the targetDrug property.
     * 
     * @return
     *     possible object is
     *     {@link DtCode }
     *     
     */
    public DtCode getTargetDrug() {
        return targetDrug;
    }

    /**
     * Sets the value of the targetDrug property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCode }
     *     
     */
    public void setTargetDrug(DtCode value) {
        this.targetDrug = value;
    }

    /**
     * Gets the value of the resultStatus property.
     * 
     * @return
     *     possible object is
     *     {@link VocObservationResultStatus }
     *     
     */
    public VocObservationResultStatus getResultStatus() {
        return resultStatus;
    }

    /**
     * Sets the value of the resultStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocObservationResultStatus }
     *     
     */
    public void setResultStatus(VocObservationResultStatus value) {
        this.resultStatus = value;
    }

}

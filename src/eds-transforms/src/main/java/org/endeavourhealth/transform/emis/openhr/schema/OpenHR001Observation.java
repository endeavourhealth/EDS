
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.Observation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Observation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="value" type="{http://www.e-mis.com/emisopen}OpenHR001.ObservationValue" minOccurs="0"/>
 *         &lt;element name="abnormal" type="{http://www.e-mis.com/emisopen}OpenHR001.Abnormal" minOccurs="0"/>
 *         &lt;element name="complexObservation" type="{http://www.e-mis.com/emisopen}OpenHR001.ComplexObservation" minOccurs="0"/>
 *         &lt;element name="episodicity" type="{http://www.e-mis.com/emisopen}voc.Episodicity" minOccurs="0"/>
 *         &lt;element name="referenceDocument" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
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
@XmlType(name = "OpenHR001.Observation", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "value",
    "abnormal",
    "complexObservation",
    "episodicity",
    "referenceDocument"
})
public class OpenHR001Observation {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001ObservationValue value;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Abnormal abnormal;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001ComplexObservation complexObservation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocEpisodicity episodicity;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String referenceDocument;
    @XmlAttribute(name = "resultStatus")
    protected VocObservationResultStatus resultStatus;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001ObservationValue }
     *     
     */
    public OpenHR001ObservationValue getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001ObservationValue }
     *     
     */
    public void setValue(OpenHR001ObservationValue value) {
        this.value = value;
    }

    /**
     * Gets the value of the abnormal property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Abnormal }
     *     
     */
    public OpenHR001Abnormal getAbnormal() {
        return abnormal;
    }

    /**
     * Sets the value of the abnormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Abnormal }
     *     
     */
    public void setAbnormal(OpenHR001Abnormal value) {
        this.abnormal = value;
    }

    /**
     * Gets the value of the complexObservation property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001ComplexObservation }
     *     
     */
    public OpenHR001ComplexObservation getComplexObservation() {
        return complexObservation;
    }

    /**
     * Sets the value of the complexObservation property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001ComplexObservation }
     *     
     */
    public void setComplexObservation(OpenHR001ComplexObservation value) {
        this.complexObservation = value;
    }

    /**
     * Gets the value of the episodicity property.
     * 
     * @return
     *     possible object is
     *     {@link VocEpisodicity }
     *     
     */
    public VocEpisodicity getEpisodicity() {
        return episodicity;
    }

    /**
     * Sets the value of the episodicity property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocEpisodicity }
     *     
     */
    public void setEpisodicity(VocEpisodicity value) {
        this.episodicity = value;
    }

    /**
     * Gets the value of the referenceDocument property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReferenceDocument() {
        return referenceDocument;
    }

    /**
     * Sets the value of the referenceDocument property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReferenceDocument(String value) {
        this.referenceDocument = value;
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

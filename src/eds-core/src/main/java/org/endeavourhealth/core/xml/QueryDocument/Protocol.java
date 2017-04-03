
package org.endeavourhealth.core.xml.QueryDocument;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for protocol complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="protocol">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="enabled" type="{}protocolEnabled"/>
 *         &lt;element name="patientConsent" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cohort" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataSet" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="serviceContract" type="{}serviceContract" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "protocol", propOrder = {
    "enabled",
    "patientConsent",
    "cohort",
    "dataSet",
    "serviceContract"
})
public class Protocol {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ProtocolEnabled enabled;
    @XmlElement(required = true)
    protected String patientConsent;
    protected String cohort;
    protected String dataSet;
    protected List<ServiceContract> serviceContract;

    /**
     * Gets the value of the enabled property.
     * 
     * @return
     *     possible object is
     *     {@link ProtocolEnabled }
     *     
     */
    public ProtocolEnabled getEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProtocolEnabled }
     *     
     */
    public void setEnabled(ProtocolEnabled value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the patientConsent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientConsent() {
        return patientConsent;
    }

    /**
     * Sets the value of the patientConsent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientConsent(String value) {
        this.patientConsent = value;
    }

    /**
     * Gets the value of the cohort property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCohort() {
        return cohort;
    }

    /**
     * Sets the value of the cohort property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCohort(String value) {
        this.cohort = value;
    }

    /**
     * Gets the value of the dataSet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataSet() {
        return dataSet;
    }

    /**
     * Sets the value of the dataSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataSet(String value) {
        this.dataSet = value;
    }

    /**
     * Gets the value of the serviceContract property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceContract property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceContract().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceContract }
     * 
     * 
     */
    public List<ServiceContract> getServiceContract() {
        if (serviceContract == null) {
            serviceContract = new ArrayList<ServiceContract>();
        }
        return this.serviceContract;
    }

}

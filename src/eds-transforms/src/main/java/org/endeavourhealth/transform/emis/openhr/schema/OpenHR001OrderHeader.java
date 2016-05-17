
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.OrderHeader complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.OrderHeader">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Urgent" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="inoculationRisk" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="fasted" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="clinicalDetails" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sampleWorkflowTaskId" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="sampledByUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="requestMode" type="{http://www.e-mis.com/emisopen}voc.RequestMode" minOccurs="0"/>
 *         &lt;element name="onlineTrackerGUID" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="orderBatch" type="{http://www.e-mis.com/emisopen}OpenHR001.OrderBatch" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.OrderHeader", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "urgent",
    "inoculationRisk",
    "fasted",
    "clinicalDetails",
    "sampleWorkflowTaskId",
    "sampledByUserInRole",
    "requestMode",
    "onlineTrackerGUID",
    "orderBatch"
})
public class OpenHR001OrderHeader {

    @XmlElement(name = "Urgent", namespace = "http://www.e-mis.com/emisopen")
    protected Boolean urgent;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean inoculationRisk;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean fasted;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String clinicalDetails;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String sampleWorkflowTaskId;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String sampledByUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocRequestMode requestMode;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String onlineTrackerGUID;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001OrderBatch> orderBatch;

    /**
     * Gets the value of the urgent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUrgent() {
        return urgent;
    }

    /**
     * Sets the value of the urgent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUrgent(Boolean value) {
        this.urgent = value;
    }

    /**
     * Gets the value of the inoculationRisk property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInoculationRisk() {
        return inoculationRisk;
    }

    /**
     * Sets the value of the inoculationRisk property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInoculationRisk(Boolean value) {
        this.inoculationRisk = value;
    }

    /**
     * Gets the value of the fasted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFasted() {
        return fasted;
    }

    /**
     * Sets the value of the fasted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFasted(Boolean value) {
        this.fasted = value;
    }

    /**
     * Gets the value of the clinicalDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClinicalDetails() {
        return clinicalDetails;
    }

    /**
     * Sets the value of the clinicalDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClinicalDetails(String value) {
        this.clinicalDetails = value;
    }

    /**
     * Gets the value of the sampleWorkflowTaskId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSampleWorkflowTaskId() {
        return sampleWorkflowTaskId;
    }

    /**
     * Sets the value of the sampleWorkflowTaskId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSampleWorkflowTaskId(String value) {
        this.sampleWorkflowTaskId = value;
    }

    /**
     * Gets the value of the sampledByUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSampledByUserInRole() {
        return sampledByUserInRole;
    }

    /**
     * Sets the value of the sampledByUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSampledByUserInRole(String value) {
        this.sampledByUserInRole = value;
    }

    /**
     * Gets the value of the requestMode property.
     * 
     * @return
     *     possible object is
     *     {@link VocRequestMode }
     *     
     */
    public VocRequestMode getRequestMode() {
        return requestMode;
    }

    /**
     * Sets the value of the requestMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocRequestMode }
     *     
     */
    public void setRequestMode(VocRequestMode value) {
        this.requestMode = value;
    }

    /**
     * Gets the value of the onlineTrackerGUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnlineTrackerGUID() {
        return onlineTrackerGUID;
    }

    /**
     * Sets the value of the onlineTrackerGUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnlineTrackerGUID(String value) {
        this.onlineTrackerGUID = value;
    }

    /**
     * Gets the value of the orderBatch property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the orderBatch property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrderBatch().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001OrderBatch }
     * 
     * 
     */
    public List<OpenHR001OrderBatch> getOrderBatch() {
        if (orderBatch == null) {
            orderBatch = new ArrayList<OpenHR001OrderBatch>();
        }
        return this.orderBatch;
    }

}

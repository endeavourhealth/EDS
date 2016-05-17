
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.OrderBatch complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.OrderBatch">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="observationGUID" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="orderReference" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="status" type="{http://www.e-mis.com/emisopen}voc.OrderBatchStatus" minOccurs="0"/>
 *         &lt;element name="testStatusDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="inboundReportGUID" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="labDisplayTerm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bottleCode" type="{http://www.e-mis.com/emisopen}dt.Code" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.OrderBatch", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "observationGUID",
    "orderReference",
    "status",
    "testStatusDate",
    "inboundReportGUID",
    "labDisplayTerm",
    "bottleCode"
})
public class OpenHR001OrderBatch
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String observationGUID;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String orderReference;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocOrderBatchStatus status;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar testStatusDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String inboundReportGUID;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String labDisplayTerm;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtCode bottleCode;

    /**
     * Gets the value of the observationGUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObservationGUID() {
        return observationGUID;
    }

    /**
     * Sets the value of the observationGUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObservationGUID(String value) {
        this.observationGUID = value;
    }

    /**
     * Gets the value of the orderReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderReference() {
        return orderReference;
    }

    /**
     * Sets the value of the orderReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderReference(String value) {
        this.orderReference = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link VocOrderBatchStatus }
     *     
     */
    public VocOrderBatchStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocOrderBatchStatus }
     *     
     */
    public void setStatus(VocOrderBatchStatus value) {
        this.status = value;
    }

    /**
     * Gets the value of the testStatusDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTestStatusDate() {
        return testStatusDate;
    }

    /**
     * Sets the value of the testStatusDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTestStatusDate(XMLGregorianCalendar value) {
        this.testStatusDate = value;
    }

    /**
     * Gets the value of the inboundReportGUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInboundReportGUID() {
        return inboundReportGUID;
    }

    /**
     * Sets the value of the inboundReportGUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInboundReportGUID(String value) {
        this.inboundReportGUID = value;
    }

    /**
     * Gets the value of the labDisplayTerm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabDisplayTerm() {
        return labDisplayTerm;
    }

    /**
     * Sets the value of the labDisplayTerm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabDisplayTerm(String value) {
        this.labDisplayTerm = value;
    }

    /**
     * Gets the value of the bottleCode property.
     * 
     * @return
     *     possible object is
     *     {@link DtCode }
     *     
     */
    public DtCode getBottleCode() {
        return bottleCode;
    }

    /**
     * Sets the value of the bottleCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCode }
     *     
     */
    public void setBottleCode(DtCode value) {
        this.bottleCode = value;
    }

}

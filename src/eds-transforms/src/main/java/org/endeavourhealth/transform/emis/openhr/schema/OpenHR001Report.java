
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
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.Report complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Report">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="abnormal" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="issueDate" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="receivedDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="dataType" type="{http://www.e-mis.com/emisopen}voc.PathReportDataType" minOccurs="0"/>
 *         &lt;element name="reportLabId" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="requestLabId" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="requestDateTimeLab" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="serviceOrganisation" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="serviceLocation" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="servicePerson" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="messageId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="messageInterchange" type="{http://www.e-mis.com/emisopen}OpenHR001.MessageInterchange" minOccurs="0"/>
 *         &lt;element name="orderHeader" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="reportSummary" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="viewerUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="patientIdentifier" type="{http://www.e-mis.com/emisopen}OpenHR001.ReportPatientIdentifier" minOccurs="0"/>
 *         &lt;element name="component" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="specimen" type="{http://www.e-mis.com/emisopen}OpenHR001.Specimen"/>
 *                   &lt;element name="battery" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *                   &lt;element name="test" type="{http://www.e-mis.com/emisopen}dt.url"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Report", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "abnormal",
    "issueDate",
    "receivedDate",
    "dataType",
    "reportLabId",
    "requestLabId",
    "requestDateTimeLab",
    "serviceOrganisation",
    "serviceLocation",
    "servicePerson",
    "messageId",
    "messageInterchange",
    "orderHeader",
    "reportSummary",
    "viewerUserInRole",
    "patientIdentifier",
    "component"
})
public class OpenHR001Report {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean abnormal;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar issueDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar receivedDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocPathReportDataType dataType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String reportLabId;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String requestLabId;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar requestDateTimeLab;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String serviceOrganisation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String serviceLocation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String servicePerson;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Integer messageId;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001MessageInterchange messageInterchange;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String orderHeader;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<String> reportSummary;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> viewerUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001ReportPatientIdentifier patientIdentifier;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Report.Component> component;

    /**
     * Gets the value of the abnormal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAbnormal() {
        return abnormal;
    }

    /**
     * Sets the value of the abnormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAbnormal(Boolean value) {
        this.abnormal = value;
    }

    /**
     * Gets the value of the issueDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getIssueDate() {
        return issueDate;
    }

    /**
     * Sets the value of the issueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setIssueDate(XMLGregorianCalendar value) {
        this.issueDate = value;
    }

    /**
     * Gets the value of the receivedDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getReceivedDate() {
        return receivedDate;
    }

    /**
     * Sets the value of the receivedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setReceivedDate(XMLGregorianCalendar value) {
        this.receivedDate = value;
    }

    /**
     * Gets the value of the dataType property.
     * 
     * @return
     *     possible object is
     *     {@link VocPathReportDataType }
     *     
     */
    public VocPathReportDataType getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the dataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocPathReportDataType }
     *     
     */
    public void setDataType(VocPathReportDataType value) {
        this.dataType = value;
    }

    /**
     * Gets the value of the reportLabId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportLabId() {
        return reportLabId;
    }

    /**
     * Sets the value of the reportLabId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportLabId(String value) {
        this.reportLabId = value;
    }

    /**
     * Gets the value of the requestLabId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestLabId() {
        return requestLabId;
    }

    /**
     * Sets the value of the requestLabId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestLabId(String value) {
        this.requestLabId = value;
    }

    /**
     * Gets the value of the requestDateTimeLab property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRequestDateTimeLab() {
        return requestDateTimeLab;
    }

    /**
     * Sets the value of the requestDateTimeLab property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRequestDateTimeLab(XMLGregorianCalendar value) {
        this.requestDateTimeLab = value;
    }

    /**
     * Gets the value of the serviceOrganisation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceOrganisation() {
        return serviceOrganisation;
    }

    /**
     * Sets the value of the serviceOrganisation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceOrganisation(String value) {
        this.serviceOrganisation = value;
    }

    /**
     * Gets the value of the serviceLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceLocation() {
        return serviceLocation;
    }

    /**
     * Sets the value of the serviceLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceLocation(String value) {
        this.serviceLocation = value;
    }

    /**
     * Gets the value of the servicePerson property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServicePerson() {
        return servicePerson;
    }

    /**
     * Sets the value of the servicePerson property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServicePerson(String value) {
        this.servicePerson = value;
    }

    /**
     * Gets the value of the messageId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMessageId() {
        return messageId;
    }

    /**
     * Sets the value of the messageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMessageId(Integer value) {
        this.messageId = value;
    }

    /**
     * Gets the value of the messageInterchange property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001MessageInterchange }
     *     
     */
    public OpenHR001MessageInterchange getMessageInterchange() {
        return messageInterchange;
    }

    /**
     * Sets the value of the messageInterchange property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001MessageInterchange }
     *     
     */
    public void setMessageInterchange(OpenHR001MessageInterchange value) {
        this.messageInterchange = value;
    }

    /**
     * Gets the value of the orderHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderHeader() {
        return orderHeader;
    }

    /**
     * Sets the value of the orderHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderHeader(String value) {
        this.orderHeader = value;
    }

    /**
     * Gets the value of the reportSummary property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reportSummary property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReportSummary().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getReportSummary() {
        if (reportSummary == null) {
            reportSummary = new ArrayList<String>();
        }
        return this.reportSummary;
    }

    /**
     * Gets the value of the viewerUserInRole property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the viewerUserInRole property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getViewerUserInRole().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getViewerUserInRole() {
        if (viewerUserInRole == null) {
            viewerUserInRole = new ArrayList<String>();
        }
        return this.viewerUserInRole;
    }

    /**
     * Gets the value of the patientIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001ReportPatientIdentifier }
     *     
     */
    public OpenHR001ReportPatientIdentifier getPatientIdentifier() {
        return patientIdentifier;
    }

    /**
     * Sets the value of the patientIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001ReportPatientIdentifier }
     *     
     */
    public void setPatientIdentifier(OpenHR001ReportPatientIdentifier value) {
        this.patientIdentifier = value;
    }

    /**
     * Gets the value of the component property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the component property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComponent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Report.Component }
     * 
     * 
     */
    public List<OpenHR001Report.Component> getComponent() {
        if (component == null) {
            component = new ArrayList<OpenHR001Report.Component>();
        }
        return this.component;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice>
     *         &lt;element name="specimen" type="{http://www.e-mis.com/emisopen}OpenHR001.Specimen"/>
     *         &lt;element name="battery" type="{http://www.e-mis.com/emisopen}dt.uid"/>
     *         &lt;element name="test" type="{http://www.e-mis.com/emisopen}dt.url"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "specimen",
        "battery",
        "test"
    })
    public static class Component {

        @XmlElement(namespace = "http://www.e-mis.com/emisopen")
        protected OpenHR001Specimen specimen;
        @XmlElement(namespace = "http://www.e-mis.com/emisopen")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "token")
        protected String battery;
        @XmlElement(namespace = "http://www.e-mis.com/emisopen")
        @XmlSchemaType(name = "anyURI")
        protected String test;

        /**
         * Gets the value of the specimen property.
         * 
         * @return
         *     possible object is
         *     {@link OpenHR001Specimen }
         *     
         */
        public OpenHR001Specimen getSpecimen() {
            return specimen;
        }

        /**
         * Sets the value of the specimen property.
         * 
         * @param value
         *     allowed object is
         *     {@link OpenHR001Specimen }
         *     
         */
        public void setSpecimen(OpenHR001Specimen value) {
            this.specimen = value;
        }

        /**
         * Gets the value of the battery property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getBattery() {
            return battery;
        }

        /**
         * Sets the value of the battery property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBattery(String value) {
            this.battery = value;
        }

        /**
         * Gets the value of the test property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTest() {
            return test;
        }

        /**
         * Sets the value of the test property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTest(String value) {
            this.test = value;
        }

    }

}

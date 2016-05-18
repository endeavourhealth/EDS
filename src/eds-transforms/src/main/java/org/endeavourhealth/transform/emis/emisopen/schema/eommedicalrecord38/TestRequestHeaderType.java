
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TestRequestHeaderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TestRequestHeaderType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="Requestor" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="ConsultationID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="DateCreated" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DateForTest" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DateLastXRay" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Status" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="LastStatusDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CopyTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NHS" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Priority" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="InnoculationRisk" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Fasted" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="LastMenstualPeriod" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ClinicalInformation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Pregnant" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="EDIOrderList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="EDIOrder" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDIOrderType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestRequestHeaderType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "requestor",
    "consultationID",
    "dateCreated",
    "dateForTest",
    "dateLastXRay",
    "status",
    "lastStatusDate",
    "copyTo",
    "nhs",
    "priority",
    "innoculationRisk",
    "fasted",
    "lastMenstualPeriod",
    "clinicalInformation",
    "pregnant",
    "ediOrderList"
})
public class TestRequestHeaderType
    extends IdentType
{

    @XmlElement(name = "Requestor", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType requestor;
    @XmlElement(name = "ConsultationID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType consultationID;
    @XmlElement(name = "DateCreated", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateCreated;
    @XmlElement(name = "DateForTest", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateForTest;
    @XmlElement(name = "DateLastXRay", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateLastXRay;
    @XmlElement(name = "Status", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected Object status;
    @XmlElement(name = "LastStatusDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String lastStatusDate;
    @XmlElement(name = "CopyTo", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String copyTo;
    @XmlElement(name = "NHS", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger nhs;
    @XmlElement(name = "Priority", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger priority;
    @XmlElement(name = "InnoculationRisk", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger innoculationRisk;
    @XmlElement(name = "Fasted", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger fasted;
    @XmlElement(name = "LastMenstualPeriod", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String lastMenstualPeriod;
    @XmlElement(name = "ClinicalInformation", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String clinicalInformation;
    @XmlElement(name = "Pregnant", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger pregnant;
    @XmlElement(name = "EDIOrderList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected TestRequestHeaderType.EDIOrderList ediOrderList;

    /**
     * Gets the value of the requestor property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getRequestor() {
        return requestor;
    }

    /**
     * Sets the value of the requestor property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setRequestor(IdentType value) {
        this.requestor = value;
    }

    /**
     * Gets the value of the consultationID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getConsultationID() {
        return consultationID;
    }

    /**
     * Sets the value of the consultationID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setConsultationID(IdentType value) {
        this.consultationID = value;
    }

    /**
     * Gets the value of the dateCreated property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateCreated() {
        return dateCreated;
    }

    /**
     * Sets the value of the dateCreated property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateCreated(String value) {
        this.dateCreated = value;
    }

    /**
     * Gets the value of the dateForTest property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateForTest() {
        return dateForTest;
    }

    /**
     * Sets the value of the dateForTest property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateForTest(String value) {
        this.dateForTest = value;
    }

    /**
     * Gets the value of the dateLastXRay property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateLastXRay() {
        return dateLastXRay;
    }

    /**
     * Sets the value of the dateLastXRay property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateLastXRay(String value) {
        this.dateLastXRay = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setStatus(Object value) {
        this.status = value;
    }

    /**
     * Gets the value of the lastStatusDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastStatusDate() {
        return lastStatusDate;
    }

    /**
     * Sets the value of the lastStatusDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastStatusDate(String value) {
        this.lastStatusDate = value;
    }

    /**
     * Gets the value of the copyTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCopyTo() {
        return copyTo;
    }

    /**
     * Sets the value of the copyTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCopyTo(String value) {
        this.copyTo = value;
    }

    /**
     * Gets the value of the nhs property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNHS() {
        return nhs;
    }

    /**
     * Sets the value of the nhs property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNHS(BigInteger value) {
        this.nhs = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPriority(BigInteger value) {
        this.priority = value;
    }

    /**
     * Gets the value of the innoculationRisk property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getInnoculationRisk() {
        return innoculationRisk;
    }

    /**
     * Sets the value of the innoculationRisk property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setInnoculationRisk(BigInteger value) {
        this.innoculationRisk = value;
    }

    /**
     * Gets the value of the fasted property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFasted() {
        return fasted;
    }

    /**
     * Sets the value of the fasted property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFasted(BigInteger value) {
        this.fasted = value;
    }

    /**
     * Gets the value of the lastMenstualPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastMenstualPeriod() {
        return lastMenstualPeriod;
    }

    /**
     * Sets the value of the lastMenstualPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastMenstualPeriod(String value) {
        this.lastMenstualPeriod = value;
    }

    /**
     * Gets the value of the clinicalInformation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClinicalInformation() {
        return clinicalInformation;
    }

    /**
     * Sets the value of the clinicalInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClinicalInformation(String value) {
        this.clinicalInformation = value;
    }

    /**
     * Gets the value of the pregnant property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPregnant() {
        return pregnant;
    }

    /**
     * Sets the value of the pregnant property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPregnant(BigInteger value) {
        this.pregnant = value;
    }

    /**
     * Gets the value of the ediOrderList property.
     * 
     * @return
     *     possible object is
     *     {@link TestRequestHeaderType.EDIOrderList }
     *     
     */
    public TestRequestHeaderType.EDIOrderList getEDIOrderList() {
        return ediOrderList;
    }

    /**
     * Sets the value of the ediOrderList property.
     * 
     * @param value
     *     allowed object is
     *     {@link TestRequestHeaderType.EDIOrderList }
     *     
     */
    public void setEDIOrderList(TestRequestHeaderType.EDIOrderList value) {
        this.ediOrderList = value;
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
     *       &lt;sequence>
     *         &lt;element name="EDIOrder" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDIOrderType" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "ediOrder"
    })
    public static class EDIOrderList {

        @XmlElement(name = "EDIOrder", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected List<EDIOrderType> ediOrder;

        /**
         * Gets the value of the ediOrder property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the ediOrder property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEDIOrder().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EDIOrderType }
         * 
         * 
         */
        public List<EDIOrderType> getEDIOrder() {
            if (ediOrder == null) {
                ediOrder = new ArrayList<EDIOrderType>();
            }
            return this.ediOrder;
        }

    }

}

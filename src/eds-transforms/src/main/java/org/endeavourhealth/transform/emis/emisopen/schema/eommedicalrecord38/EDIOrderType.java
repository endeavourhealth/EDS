
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EDIOrderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EDIOrderType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="HeaderID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType"/>
 *         &lt;element name="Provider" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="FormDestination" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *                 &lt;sequence>
 *                   &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="OrderCategory">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Status" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="S"/>
 *               &lt;enumeration value="R"/>
 *               &lt;enumeration value="D"/>
 *               &lt;enumeration value="P"/>
 *               &lt;enumeration value="F"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="LastStatusDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TestRequestList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="TestRequest" type="{http://www.e-mis.com/emisopen/MedicalRecord}TestRequestType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "EDIOrderType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "headerID",
    "provider",
    "formDestination",
    "orderCategory",
    "status",
    "lastStatusDate",
    "testRequestList"
})
public class EDIOrderType
    extends IdentType
{

    @XmlElement(name = "HeaderID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected IdentType headerID;
    @XmlElement(name = "Provider", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType provider;
    @XmlElement(name = "FormDestination", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected EDIOrderType.FormDestination formDestination;
    @XmlElement(name = "OrderCategory", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected BigInteger orderCategory;
    @XmlElement(name = "Status", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String status;
    @XmlElement(name = "LastStatusDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String lastStatusDate;
    @XmlElement(name = "TestRequestList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected EDIOrderType.TestRequestList testRequestList;

    /**
     * Gets the value of the headerID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getHeaderID() {
        return headerID;
    }

    /**
     * Sets the value of the headerID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setHeaderID(IdentType value) {
        this.headerID = value;
    }

    /**
     * Gets the value of the provider property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getProvider() {
        return provider;
    }

    /**
     * Sets the value of the provider property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setProvider(IdentType value) {
        this.provider = value;
    }

    /**
     * Gets the value of the formDestination property.
     * 
     * @return
     *     possible object is
     *     {@link EDIOrderType.FormDestination }
     *     
     */
    public EDIOrderType.FormDestination getFormDestination() {
        return formDestination;
    }

    /**
     * Sets the value of the formDestination property.
     * 
     * @param value
     *     allowed object is
     *     {@link EDIOrderType.FormDestination }
     *     
     */
    public void setFormDestination(EDIOrderType.FormDestination value) {
        this.formDestination = value;
    }

    /**
     * Gets the value of the orderCategory property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getOrderCategory() {
        return orderCategory;
    }

    /**
     * Sets the value of the orderCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setOrderCategory(BigInteger value) {
        this.orderCategory = value;
    }

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
     * Gets the value of the testRequestList property.
     * 
     * @return
     *     possible object is
     *     {@link EDIOrderType.TestRequestList }
     *     
     */
    public EDIOrderType.TestRequestList getTestRequestList() {
        return testRequestList;
    }

    /**
     * Sets the value of the testRequestList property.
     * 
     * @param value
     *     allowed object is
     *     {@link EDIOrderType.TestRequestList }
     *     
     */
    public void setTestRequestList(EDIOrderType.TestRequestList value) {
        this.testRequestList = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
     *       &lt;sequence>
     *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "description"
    })
    public static class FormDestination
        extends IdentType
    {

        @XmlElement(name = "Description", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
        protected String description;

        /**
         * Gets the value of the description property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the value of the description property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDescription(String value) {
            this.description = value;
        }

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
     *         &lt;element name="TestRequest" type="{http://www.e-mis.com/emisopen/MedicalRecord}TestRequestType" maxOccurs="unbounded" minOccurs="0"/>
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
        "testRequest"
    })
    public static class TestRequestList {

        @XmlElement(name = "TestRequest", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected List<TestRequestType> testRequest;

        /**
         * Gets the value of the testRequest property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the testRequest property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTestRequest().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link TestRequestType }
         * 
         * 
         */
        public List<TestRequestType> getTestRequest() {
            if (testRequest == null) {
                testRequest = new ArrayList<TestRequestType>();
            }
            return this.testRequest;
        }

    }

}

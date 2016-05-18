
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TestRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TestRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}CodedItemBaseType">
 *       &lt;sequence>
 *         &lt;element name="Reference" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RequestHeaderID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="SpecimenType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SpecimenTypeCode" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="CollectionDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Status" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="S"/>
 *               &lt;enumeration value="R"/>
 *               &lt;enumeration value="D"/>
 *               &lt;enumeration value="C"/>
 *               &lt;enumeration value="F"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="LastStatusDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ResultsRef" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="InvestigationRef" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ResultsDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestRequestType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "reference",
    "requestHeaderID",
    "specimenType",
    "specimenTypeCode",
    "collectionDate",
    "status",
    "lastStatusDate",
    "resultsRef",
    "investigationRef",
    "resultsDate"
})
public class TestRequestType
    extends CodedItemBaseType
{

    @XmlElement(name = "Reference", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String reference;
    @XmlElement(name = "RequestHeaderID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType requestHeaderID;
    @XmlElement(name = "SpecimenType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String specimenType;
    @XmlElement(name = "SpecimenTypeCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType specimenTypeCode;
    @XmlElement(name = "CollectionDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String collectionDate;
    @XmlElement(name = "Status", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String status;
    @XmlElement(name = "LastStatusDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String lastStatusDate;
    @XmlElement(name = "ResultsRef", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType resultsRef;
    @XmlElement(name = "InvestigationRef", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<IdentType> investigationRef;
    @XmlElement(name = "ResultsDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String resultsDate;

    /**
     * Gets the value of the reference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the value of the reference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReference(String value) {
        this.reference = value;
    }

    /**
     * Gets the value of the requestHeaderID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getRequestHeaderID() {
        return requestHeaderID;
    }

    /**
     * Sets the value of the requestHeaderID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setRequestHeaderID(IdentType value) {
        this.requestHeaderID = value;
    }

    /**
     * Gets the value of the specimenType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecimenType() {
        return specimenType;
    }

    /**
     * Sets the value of the specimenType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecimenType(String value) {
        this.specimenType = value;
    }

    /**
     * Gets the value of the specimenTypeCode property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getSpecimenTypeCode() {
        return specimenTypeCode;
    }

    /**
     * Sets the value of the specimenTypeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setSpecimenTypeCode(StringCodeType value) {
        this.specimenTypeCode = value;
    }

    /**
     * Gets the value of the collectionDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionDate() {
        return collectionDate;
    }

    /**
     * Sets the value of the collectionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionDate(String value) {
        this.collectionDate = value;
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
     * Gets the value of the resultsRef property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getResultsRef() {
        return resultsRef;
    }

    /**
     * Sets the value of the resultsRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setResultsRef(IdentType value) {
        this.resultsRef = value;
    }

    /**
     * Gets the value of the investigationRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the investigationRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvestigationRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IdentType }
     * 
     * 
     */
    public List<IdentType> getInvestigationRef() {
        if (investigationRef == null) {
            investigationRef = new ArrayList<IdentType>();
        }
        return this.investigationRef;
    }

    /**
     * Gets the value of the resultsDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResultsDate() {
        return resultsDate;
    }

    /**
     * Sets the value of the resultsDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResultsDate(String value) {
        this.resultsDate = value;
    }

}

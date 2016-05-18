
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Common elements for "manual" and "EDI/Patholoy" Investigation entries
 * 
 * <p>Java class for InvestigationTypeBase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InvestigationTypeBase">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="DisplayTerm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataSource" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="Code" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="TermID" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="Abnormal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataType" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="ReportID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SpecimenID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SpecimenType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NumberOfTests" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="RepInvNumber" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="UserComment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InvestigationTypeBase", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "displayTerm",
    "dataSource",
    "code",
    "termID",
    "abnormal",
    "dataType",
    "reportID",
    "specimenID",
    "specimenType",
    "numberOfTests",
    "repInvNumber",
    "userComment"
})
@XmlSeeAlso({
    PathologyInvestigationType.class,
    InvestigationType.class
})
public class InvestigationTypeBase
    extends IdentType
{

    @XmlElement(name = "DisplayTerm", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String displayTerm;
    @XmlElement(name = "DataSource", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger dataSource;
    @XmlElement(name = "Code", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType code;
    @XmlElement(name = "TermID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType termID;
    @XmlElement(name = "Abnormal", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String abnormal;
    @XmlElement(name = "DataType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected Object dataType;
    @XmlElement(name = "ReportID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String reportID;
    @XmlElement(name = "SpecimenID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String specimenID;
    @XmlElement(name = "SpecimenType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String specimenType;
    @XmlElement(name = "NumberOfTests", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger numberOfTests;
    @XmlElement(name = "RepInvNumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger repInvNumber;
    @XmlElement(name = "UserComment", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String userComment;

    /**
     * Gets the value of the displayTerm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayTerm() {
        return displayTerm;
    }

    /**
     * Sets the value of the displayTerm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayTerm(String value) {
        this.displayTerm = value;
    }

    /**
     * Gets the value of the dataSource property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDataSource() {
        return dataSource;
    }

    /**
     * Sets the value of the dataSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDataSource(BigInteger value) {
        this.dataSource = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setCode(StringCodeType value) {
        this.code = value;
    }

    /**
     * Gets the value of the termID property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getTermID() {
        return termID;
    }

    /**
     * Sets the value of the termID property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setTermID(StringCodeType value) {
        this.termID = value;
    }

    /**
     * Gets the value of the abnormal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAbnormal() {
        return abnormal;
    }

    /**
     * Sets the value of the abnormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAbnormal(String value) {
        this.abnormal = value;
    }

    /**
     * Gets the value of the dataType property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the dataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setDataType(Object value) {
        this.dataType = value;
    }

    /**
     * Gets the value of the reportID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportID() {
        return reportID;
    }

    /**
     * Sets the value of the reportID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportID(String value) {
        this.reportID = value;
    }

    /**
     * Gets the value of the specimenID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecimenID() {
        return specimenID;
    }

    /**
     * Sets the value of the specimenID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecimenID(String value) {
        this.specimenID = value;
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
     * Gets the value of the numberOfTests property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfTests() {
        return numberOfTests;
    }

    /**
     * Sets the value of the numberOfTests property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfTests(BigInteger value) {
        this.numberOfTests = value;
    }

    /**
     * Gets the value of the repInvNumber property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRepInvNumber() {
        return repInvNumber;
    }

    /**
     * Sets the value of the repInvNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRepInvNumber(BigInteger value) {
        this.repInvNumber = value;
    }

    /**
     * Gets the value of the userComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserComment() {
        return userComment;
    }

    /**
     * Sets the value of the userComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserComment(String value) {
        this.userComment = value;
    }

}

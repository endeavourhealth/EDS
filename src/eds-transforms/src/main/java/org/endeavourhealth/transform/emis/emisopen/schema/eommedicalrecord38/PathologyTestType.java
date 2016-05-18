
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PathologyTestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PathologyTestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="ReportID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SpecimenID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DisplayTerm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Code" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="TermID" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="NumericValue" type="{http://www.e-mis.com/emisopen/MedicalRecord}NumericValueType" minOccurs="0"/>
 *         &lt;element name="TextValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AbnormalIndicator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EDIResultStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LabSpecifiedCommentList" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDICommentListType" minOccurs="0"/>
 *         &lt;element name="RangeInformationList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="RangeInformation" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="MinRange" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="MaxRange" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="RangeFTXList" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDICommentListType" minOccurs="0"/>
 *                             &lt;element name="RangeQualifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="FiledCode" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="TestNumber" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PathologyTestType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "reportID",
    "specimenID",
    "displayTerm",
    "code",
    "termID",
    "numericValue",
    "textValue",
    "abnormalIndicator",
    "ediResultStatus",
    "labSpecifiedCommentList",
    "rangeInformationList",
    "filedCode",
    "testNumber"
})
public class PathologyTestType
    extends IdentType
{

    @XmlElement(name = "ReportID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String reportID;
    @XmlElement(name = "SpecimenID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String specimenID;
    @XmlElement(name = "DisplayTerm", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String displayTerm;
    @XmlElement(name = "Code", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType code;
    @XmlElement(name = "TermID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType termID;
    @XmlElement(name = "NumericValue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected NumericValueType numericValue;
    @XmlElement(name = "TextValue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String textValue;
    @XmlElement(name = "AbnormalIndicator", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String abnormalIndicator;
    @XmlElement(name = "EDIResultStatus", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String ediResultStatus;
    @XmlElement(name = "LabSpecifiedCommentList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected EDICommentListType labSpecifiedCommentList;
    @XmlElement(name = "RangeInformationList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected PathologyTestType.RangeInformationList rangeInformationList;
    @XmlElement(name = "FiledCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType filedCode;
    @XmlElement(name = "TestNumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger testNumber;

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
     * Gets the value of the numericValue property.
     * 
     * @return
     *     possible object is
     *     {@link NumericValueType }
     *     
     */
    public NumericValueType getNumericValue() {
        return numericValue;
    }

    /**
     * Sets the value of the numericValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link NumericValueType }
     *     
     */
    public void setNumericValue(NumericValueType value) {
        this.numericValue = value;
    }

    /**
     * Gets the value of the textValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextValue() {
        return textValue;
    }

    /**
     * Sets the value of the textValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextValue(String value) {
        this.textValue = value;
    }

    /**
     * Gets the value of the abnormalIndicator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAbnormalIndicator() {
        return abnormalIndicator;
    }

    /**
     * Sets the value of the abnormalIndicator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAbnormalIndicator(String value) {
        this.abnormalIndicator = value;
    }

    /**
     * Gets the value of the ediResultStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEDIResultStatus() {
        return ediResultStatus;
    }

    /**
     * Sets the value of the ediResultStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEDIResultStatus(String value) {
        this.ediResultStatus = value;
    }

    /**
     * Gets the value of the labSpecifiedCommentList property.
     * 
     * @return
     *     possible object is
     *     {@link EDICommentListType }
     *     
     */
    public EDICommentListType getLabSpecifiedCommentList() {
        return labSpecifiedCommentList;
    }

    /**
     * Sets the value of the labSpecifiedCommentList property.
     * 
     * @param value
     *     allowed object is
     *     {@link EDICommentListType }
     *     
     */
    public void setLabSpecifiedCommentList(EDICommentListType value) {
        this.labSpecifiedCommentList = value;
    }

    /**
     * Gets the value of the rangeInformationList property.
     * 
     * @return
     *     possible object is
     *     {@link PathologyTestType.RangeInformationList }
     *     
     */
    public PathologyTestType.RangeInformationList getRangeInformationList() {
        return rangeInformationList;
    }

    /**
     * Sets the value of the rangeInformationList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathologyTestType.RangeInformationList }
     *     
     */
    public void setRangeInformationList(PathologyTestType.RangeInformationList value) {
        this.rangeInformationList = value;
    }

    /**
     * Gets the value of the filedCode property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getFiledCode() {
        return filedCode;
    }

    /**
     * Sets the value of the filedCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setFiledCode(StringCodeType value) {
        this.filedCode = value;
    }

    /**
     * Gets the value of the testNumber property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTestNumber() {
        return testNumber;
    }

    /**
     * Sets the value of the testNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTestNumber(BigInteger value) {
        this.testNumber = value;
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
     *         &lt;element name="RangeInformation" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="MinRange" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="MaxRange" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="RangeFTXList" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDICommentListType" minOccurs="0"/>
     *                   &lt;element name="RangeQualifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                 &lt;/sequence>
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
    @XmlType(name = "", propOrder = {
        "rangeInformation"
    })
    public static class RangeInformationList {

        @XmlElement(name = "RangeInformation", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected List<PathologyTestType.RangeInformationList.RangeInformation> rangeInformation;

        /**
         * Gets the value of the rangeInformation property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the rangeInformation property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRangeInformation().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link PathologyTestType.RangeInformationList.RangeInformation }
         * 
         * 
         */
        public List<PathologyTestType.RangeInformationList.RangeInformation> getRangeInformation() {
            if (rangeInformation == null) {
                rangeInformation = new ArrayList<PathologyTestType.RangeInformationList.RangeInformation>();
            }
            return this.rangeInformation;
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
         *         &lt;element name="MinRange" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="MaxRange" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="RangeFTXList" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDICommentListType" minOccurs="0"/>
         *         &lt;element name="RangeQualifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
            "minRange",
            "maxRange",
            "rangeFTXList",
            "rangeQualifier"
        })
        public static class RangeInformation {

            @XmlElement(name = "MinRange", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String minRange;
            @XmlElement(name = "MaxRange", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String maxRange;
            @XmlElement(name = "RangeFTXList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected EDICommentListType rangeFTXList;
            @XmlElement(name = "RangeQualifier", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected String rangeQualifier;

            /**
             * Gets the value of the minRange property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getMinRange() {
                return minRange;
            }

            /**
             * Sets the value of the minRange property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMinRange(String value) {
                this.minRange = value;
            }

            /**
             * Gets the value of the maxRange property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getMaxRange() {
                return maxRange;
            }

            /**
             * Sets the value of the maxRange property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMaxRange(String value) {
                this.maxRange = value;
            }

            /**
             * Gets the value of the rangeFTXList property.
             * 
             * @return
             *     possible object is
             *     {@link EDICommentListType }
             *     
             */
            public EDICommentListType getRangeFTXList() {
                return rangeFTXList;
            }

            /**
             * Sets the value of the rangeFTXList property.
             * 
             * @param value
             *     allowed object is
             *     {@link EDICommentListType }
             *     
             */
            public void setRangeFTXList(EDICommentListType value) {
                this.rangeFTXList = value;
            }

            /**
             * Gets the value of the rangeQualifier property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getRangeQualifier() {
                return rangeQualifier;
            }

            /**
             * Sets the value of the rangeQualifier property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setRangeQualifier(String value) {
                this.rangeQualifier = value;
            }

        }

    }

}

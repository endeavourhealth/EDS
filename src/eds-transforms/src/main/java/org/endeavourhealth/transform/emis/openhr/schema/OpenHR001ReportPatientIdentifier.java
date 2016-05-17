
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.ReportPatientIdentifier complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.ReportPatientIdentifier">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="originalDetails" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.e-mis.com/emisopen}OpenHR001.ReportPatientIdentifierDetail">
 *                 &lt;sequence>
 *                   &lt;element name="labSubjectId" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="matchedDetails" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.e-mis.com/emisopen}OpenHR001.ReportPatientIdentifierDetail">
 *                 &lt;sequence>
 *                   &lt;element name="patient" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/extension>
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
@XmlType(name = "OpenHR001.ReportPatientIdentifier", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "originalDetails",
    "matchedDetails"
})
public class OpenHR001ReportPatientIdentifier {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001ReportPatientIdentifier.OriginalDetails originalDetails;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001ReportPatientIdentifier.MatchedDetails matchedDetails;

    /**
     * Gets the value of the originalDetails property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001ReportPatientIdentifier.OriginalDetails }
     *     
     */
    public OpenHR001ReportPatientIdentifier.OriginalDetails getOriginalDetails() {
        return originalDetails;
    }

    /**
     * Sets the value of the originalDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001ReportPatientIdentifier.OriginalDetails }
     *     
     */
    public void setOriginalDetails(OpenHR001ReportPatientIdentifier.OriginalDetails value) {
        this.originalDetails = value;
    }

    /**
     * Gets the value of the matchedDetails property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001ReportPatientIdentifier.MatchedDetails }
     *     
     */
    public OpenHR001ReportPatientIdentifier.MatchedDetails getMatchedDetails() {
        return matchedDetails;
    }

    /**
     * Sets the value of the matchedDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001ReportPatientIdentifier.MatchedDetails }
     *     
     */
    public void setMatchedDetails(OpenHR001ReportPatientIdentifier.MatchedDetails value) {
        this.matchedDetails = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.e-mis.com/emisopen}OpenHR001.ReportPatientIdentifierDetail">
     *       &lt;sequence>
     *         &lt;element name="patient" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
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
        "patient"
    })
    public static class MatchedDetails
        extends OpenHR001ReportPatientIdentifierDetail
    {

        @XmlElement(namespace = "http://www.e-mis.com/emisopen")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "token")
        protected String patient;

        /**
         * Gets the value of the patient property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPatient() {
            return patient;
        }

        /**
         * Sets the value of the patient property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPatient(String value) {
            this.patient = value;
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
     *     &lt;extension base="{http://www.e-mis.com/emisopen}OpenHR001.ReportPatientIdentifierDetail">
     *       &lt;sequence>
     *         &lt;element name="labSubjectId" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
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
        "labSubjectId"
    })
    public static class OriginalDetails
        extends OpenHR001ReportPatientIdentifierDetail
    {

        @XmlElement(namespace = "http://www.e-mis.com/emisopen")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "token")
        protected String labSubjectId;

        /**
         * Gets the value of the labSubjectId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLabSubjectId() {
            return labSubjectId;
        }

        /**
         * Sets the value of the labSubjectId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLabSubjectId(String value) {
            this.labSubjectId = value;
        }

    }

}

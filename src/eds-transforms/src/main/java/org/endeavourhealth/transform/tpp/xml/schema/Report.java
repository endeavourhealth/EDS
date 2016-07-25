
package org.endeavourhealth.transform.tpp.xml.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Report complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Report">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BatteryHeaders" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="IssueDate" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="Result" type="{}ReportResult"/>
 *         &lt;element name="Comments" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PatientToBeInformed" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="CollectionDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="Type" type="{}ReportType"/>
 *         &lt;element name="FollowUpActions">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="FollowUpAction" type="{}FollowUpAction"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="LinkedProblemUID" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Report", propOrder = {
    "batteryHeaders",
    "issueDate",
    "result",
    "comments",
    "patientToBeInformed",
    "collectionDate",
    "type",
    "followUpActions",
    "linkedProblemUID"
})
public class Report {

    @XmlElement(name = "BatteryHeaders", required = true)
    protected String batteryHeaders;
    @XmlElement(name = "IssueDate", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar issueDate;
    @XmlElement(name = "Result", required = true)
    @XmlSchemaType(name = "string")
    protected ReportResult result;
    @XmlElement(name = "Comments")
    protected String comments;
    @XmlElement(name = "PatientToBeInformed")
    protected Report.PatientToBeInformed patientToBeInformed;
    @XmlElement(name = "CollectionDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar collectionDate;
    @XmlElement(name = "Type", required = true)
    @XmlSchemaType(name = "string")
    protected ReportType type;
    @XmlElement(name = "FollowUpActions", required = true)
    protected Report.FollowUpActions followUpActions;
    @XmlElement(name = "LinkedProblemUID")
    protected List<String> linkedProblemUID;

    /**
     * Gets the value of the batteryHeaders property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBatteryHeaders() {
        return batteryHeaders;
    }

    /**
     * Sets the value of the batteryHeaders property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBatteryHeaders(String value) {
        this.batteryHeaders = value;
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
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link ReportResult }
     *     
     */
    public ReportResult getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReportResult }
     *     
     */
    public void setResult(ReportResult value) {
        this.result = value;
    }

    /**
     * Gets the value of the comments property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the value of the comments property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComments(String value) {
        this.comments = value;
    }

    /**
     * Gets the value of the patientToBeInformed property.
     * 
     * @return
     *     possible object is
     *     {@link Report.PatientToBeInformed }
     *     
     */
    public Report.PatientToBeInformed getPatientToBeInformed() {
        return patientToBeInformed;
    }

    /**
     * Sets the value of the patientToBeInformed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Report.PatientToBeInformed }
     *     
     */
    public void setPatientToBeInformed(Report.PatientToBeInformed value) {
        this.patientToBeInformed = value;
    }

    /**
     * Gets the value of the collectionDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCollectionDate() {
        return collectionDate;
    }

    /**
     * Sets the value of the collectionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCollectionDate(XMLGregorianCalendar value) {
        this.collectionDate = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link ReportType }
     *     
     */
    public ReportType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReportType }
     *     
     */
    public void setType(ReportType value) {
        this.type = value;
    }

    /**
     * Gets the value of the followUpActions property.
     * 
     * @return
     *     possible object is
     *     {@link Report.FollowUpActions }
     *     
     */
    public Report.FollowUpActions getFollowUpActions() {
        return followUpActions;
    }

    /**
     * Sets the value of the followUpActions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Report.FollowUpActions }
     *     
     */
    public void setFollowUpActions(Report.FollowUpActions value) {
        this.followUpActions = value;
    }

    /**
     * Gets the value of the linkedProblemUID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkedProblemUID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkedProblemUID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLinkedProblemUID() {
        if (linkedProblemUID == null) {
            linkedProblemUID = new ArrayList<String>();
        }
        return this.linkedProblemUID;
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
     *         &lt;element name="FollowUpAction" type="{}FollowUpAction"/>
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
        "followUpAction"
    })
    public static class FollowUpActions {

        @XmlElement(name = "FollowUpAction", required = true)
        protected FollowUpAction followUpAction;

        /**
         * Gets the value of the followUpAction property.
         * 
         * @return
         *     possible object is
         *     {@link FollowUpAction }
         *     
         */
        public FollowUpAction getFollowUpAction() {
            return followUpAction;
        }

        /**
         * Sets the value of the followUpAction property.
         * 
         * @param value
         *     allowed object is
         *     {@link FollowUpAction }
         *     
         */
        public void setFollowUpAction(FollowUpAction value) {
            this.followUpAction = value;
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
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class PatientToBeInformed {


    }

}

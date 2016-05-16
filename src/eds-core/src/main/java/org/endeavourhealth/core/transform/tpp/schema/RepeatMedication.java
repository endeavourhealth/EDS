
package org.endeavourhealth.core.transform.tpp.schema;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for RepeatMedication complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RepeatMedication">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Drug" type="{}Drug"/>
 *         &lt;element name="Dose" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="StartDate" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="EndDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="EndReason" type="{}MedicationEndReason" minOccurs="0"/>
 *         &lt;element name="MaxIssues" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="ReviewDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="IssueDurationDays" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="MedicationType" type="{}MedicationType"/>
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
@XmlType(name = "RepeatMedication", propOrder = {
    "drug",
    "dose",
    "quantity",
    "startDate",
    "endDate",
    "endReason",
    "maxIssues",
    "reviewDate",
    "issueDurationDays",
    "medicationType",
    "linkedProblemUID"
})
public class RepeatMedication {

    @XmlElement(name = "Drug", required = true)
    protected Drug drug;
    @XmlElement(name = "Dose", required = true)
    protected String dose;
    @XmlElement(name = "Quantity", required = true)
    protected String quantity;
    @XmlElement(name = "StartDate", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar startDate;
    @XmlElement(name = "EndDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar endDate;
    @XmlElement(name = "EndReason")
    @XmlSchemaType(name = "string")
    protected MedicationEndReason endReason;
    @XmlElement(name = "MaxIssues")
    protected BigInteger maxIssues;
    @XmlElement(name = "ReviewDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar reviewDate;
    @XmlElement(name = "IssueDurationDays", required = true)
    protected BigInteger issueDurationDays;
    @XmlElement(name = "MedicationType", required = true)
    @XmlSchemaType(name = "string")
    protected MedicationType medicationType;
    @XmlElement(name = "LinkedProblemUID")
    protected List<String> linkedProblemUID;

    /**
     * Gets the value of the drug property.
     * 
     * @return
     *     possible object is
     *     {@link Drug }
     *     
     */
    public Drug getDrug() {
        return drug;
    }

    /**
     * Sets the value of the drug property.
     * 
     * @param value
     *     allowed object is
     *     {@link Drug }
     *     
     */
    public void setDrug(Drug value) {
        this.drug = value;
    }

    /**
     * Gets the value of the dose property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDose() {
        return dose;
    }

    /**
     * Sets the value of the dose property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDose(String value) {
        this.dose = value;
    }

    /**
     * Gets the value of the quantity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * Sets the value of the quantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuantity(String value) {
        this.quantity = value;
    }

    /**
     * Gets the value of the startDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartDate(XMLGregorianCalendar value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the endDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEndDate() {
        return endDate;
    }

    /**
     * Sets the value of the endDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEndDate(XMLGregorianCalendar value) {
        this.endDate = value;
    }

    /**
     * Gets the value of the endReason property.
     * 
     * @return
     *     possible object is
     *     {@link MedicationEndReason }
     *     
     */
    public MedicationEndReason getEndReason() {
        return endReason;
    }

    /**
     * Sets the value of the endReason property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicationEndReason }
     *     
     */
    public void setEndReason(MedicationEndReason value) {
        this.endReason = value;
    }

    /**
     * Gets the value of the maxIssues property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxIssues() {
        return maxIssues;
    }

    /**
     * Sets the value of the maxIssues property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxIssues(BigInteger value) {
        this.maxIssues = value;
    }

    /**
     * Gets the value of the reviewDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getReviewDate() {
        return reviewDate;
    }

    /**
     * Sets the value of the reviewDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setReviewDate(XMLGregorianCalendar value) {
        this.reviewDate = value;
    }

    /**
     * Gets the value of the issueDurationDays property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getIssueDurationDays() {
        return issueDurationDays;
    }

    /**
     * Sets the value of the issueDurationDays property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setIssueDurationDays(BigInteger value) {
        this.issueDurationDays = value;
    }

    /**
     * Gets the value of the medicationType property.
     * 
     * @return
     *     possible object is
     *     {@link MedicationType }
     *     
     */
    public MedicationType getMedicationType() {
        return medicationType;
    }

    /**
     * Sets the value of the medicationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicationType }
     *     
     */
    public void setMedicationType(MedicationType value) {
        this.medicationType = value;
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

}


package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IssueType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IssueType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}MedicationType">
 *       &lt;sequence>
 *         &lt;element name="EstimatedCost" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="Comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IssuerID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="DateTimePrint" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DateTimeDispense" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DispenserID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="BatchNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MedicationLink" type="{http://www.e-mis.com/emisopen/MedicalRecord}MedicationLinkType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IssueType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "estimatedCost",
    "comment",
    "issuerID",
    "dateTimePrint",
    "dateTimeDispense",
    "dispenserID",
    "batchNumber",
    "medicationLink"
})
public class IssueType
    extends MedicationType
{

    @XmlElement(name = "EstimatedCost", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Double estimatedCost;
    @XmlElement(name = "Comment", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String comment;
    @XmlElement(name = "IssuerID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType issuerID;
    @XmlElement(name = "DateTimePrint", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateTimePrint;
    @XmlElement(name = "DateTimeDispense", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateTimeDispense;
    @XmlElement(name = "DispenserID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType dispenserID;
    @XmlElement(name = "BatchNumber", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String batchNumber;
    @XmlElement(name = "MedicationLink", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected MedicationLinkType medicationLink;

    /**
     * Gets the value of the estimatedCost property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getEstimatedCost() {
        return estimatedCost;
    }

    /**
     * Sets the value of the estimatedCost property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setEstimatedCost(Double value) {
        this.estimatedCost = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the issuerID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getIssuerID() {
        return issuerID;
    }

    /**
     * Sets the value of the issuerID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setIssuerID(IdentType value) {
        this.issuerID = value;
    }

    /**
     * Gets the value of the dateTimePrint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateTimePrint() {
        return dateTimePrint;
    }

    /**
     * Sets the value of the dateTimePrint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateTimePrint(String value) {
        this.dateTimePrint = value;
    }

    /**
     * Gets the value of the dateTimeDispense property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateTimeDispense() {
        return dateTimeDispense;
    }

    /**
     * Sets the value of the dateTimeDispense property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateTimeDispense(String value) {
        this.dateTimeDispense = value;
    }

    /**
     * Gets the value of the dispenserID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getDispenserID() {
        return dispenserID;
    }

    /**
     * Sets the value of the dispenserID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setDispenserID(IdentType value) {
        this.dispenserID = value;
    }

    /**
     * Gets the value of the batchNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBatchNumber() {
        return batchNumber;
    }

    /**
     * Sets the value of the batchNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBatchNumber(String value) {
        this.batchNumber = value;
    }

    /**
     * Gets the value of the medicationLink property.
     * 
     * @return
     *     possible object is
     *     {@link MedicationLinkType }
     *     
     */
    public MedicationLinkType getMedicationLink() {
        return medicationLink;
    }

    /**
     * Sets the value of the medicationLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicationLinkType }
     *     
     */
    public void setMedicationLink(MedicationLinkType value) {
        this.medicationLink = value;
    }

}

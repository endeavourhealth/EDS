
package org.endeavourhealth.transform.emis.openhr.schema;

import java.math.BigDecimal;
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
 * <p>Java class for OpenHR001.Medication complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Medication">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="reviewDate" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="expiryDate" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="localMixture" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="dosage">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="quantity" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="quantityUnit">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="60"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="quantityRepresentation" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="durationOfIssue" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="prescriptionType" type="{http://www.e-mis.com/emisopen}voc.PrescriptionType"/>
 *         &lt;element name="pharmacyText" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="patientText" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="drugStatus" type="{http://www.e-mis.com/emisopen}voc.DrugStatus"/>
 *         &lt;element name="private" type="{http://www.e-mis.com/emisopen}dt.bool"/>
 *         &lt;element name="minNextIssueDays" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="maxNextIssueDays" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="firstIssueDate" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="mostRecentIssueDate" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="numberOfIssues" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="authorisedIssues" type="{http://www.e-mis.com/emisopen}OpenHR001.AuthorisedIssue" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="averageCompliance" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="mostRecentIssueMethod" type="{http://www.e-mis.com/emisopen}voc.DrugIssueType" minOccurs="0"/>
 *         &lt;element name="prescribedAsContraceptive" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="cancellation" type="{http://www.e-mis.com/emisopen}OpenHR001.MedicationCancel" minOccurs="0"/>
 *         &lt;element name="dmdUnitOfMeasure" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Medication", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "reviewDate",
    "expiryDate",
    "localMixture",
    "dosage",
    "quantity",
    "quantityUnit",
    "quantityRepresentation",
    "durationOfIssue",
    "prescriptionType",
    "pharmacyText",
    "patientText",
    "drugStatus",
    "_private",
    "minNextIssueDays",
    "maxNextIssueDays",
    "firstIssueDate",
    "mostRecentIssueDate",
    "numberOfIssues",
    "authorisedIssues",
    "averageCompliance",
    "mostRecentIssueMethod",
    "prescribedAsContraceptive",
    "cancellation",
    "dmdUnitOfMeasure"
})
public class OpenHR001Medication {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar reviewDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar expiryDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String localMixture;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String dosage;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected BigDecimal quantity;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String quantityUnit;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String quantityRepresentation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected int durationOfIssue;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocPrescriptionType prescriptionType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String pharmacyText;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String patientText;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocDrugStatus drugStatus;
    @XmlElement(name = "private", namespace = "http://www.e-mis.com/emisopen")
    protected boolean _private;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Integer minNextIssueDays;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Integer maxNextIssueDays;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar firstIssueDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar mostRecentIssueDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Integer numberOfIssues;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001AuthorisedIssue> authorisedIssues;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Integer averageCompliance;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocDrugIssueType mostRecentIssueMethod;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean prescribedAsContraceptive;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001MedicationCancel cancellation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Long dmdUnitOfMeasure;

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
     * Gets the value of the expiryDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the value of the expiryDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setExpiryDate(XMLGregorianCalendar value) {
        this.expiryDate = value;
    }

    /**
     * Gets the value of the localMixture property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalMixture() {
        return localMixture;
    }

    /**
     * Sets the value of the localMixture property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalMixture(String value) {
        this.localMixture = value;
    }

    /**
     * Gets the value of the dosage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDosage() {
        return dosage;
    }

    /**
     * Sets the value of the dosage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDosage(String value) {
        this.dosage = value;
    }

    /**
     * Gets the value of the quantity property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Sets the value of the quantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setQuantity(BigDecimal value) {
        this.quantity = value;
    }

    /**
     * Gets the value of the quantityUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuantityUnit() {
        return quantityUnit;
    }

    /**
     * Sets the value of the quantityUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuantityUnit(String value) {
        this.quantityUnit = value;
    }

    /**
     * Gets the value of the quantityRepresentation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuantityRepresentation() {
        return quantityRepresentation;
    }

    /**
     * Sets the value of the quantityRepresentation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuantityRepresentation(String value) {
        this.quantityRepresentation = value;
    }

    /**
     * Gets the value of the durationOfIssue property.
     * 
     */
    public int getDurationOfIssue() {
        return durationOfIssue;
    }

    /**
     * Sets the value of the durationOfIssue property.
     * 
     */
    public void setDurationOfIssue(int value) {
        this.durationOfIssue = value;
    }

    /**
     * Gets the value of the prescriptionType property.
     * 
     * @return
     *     possible object is
     *     {@link VocPrescriptionType }
     *     
     */
    public VocPrescriptionType getPrescriptionType() {
        return prescriptionType;
    }

    /**
     * Sets the value of the prescriptionType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocPrescriptionType }
     *     
     */
    public void setPrescriptionType(VocPrescriptionType value) {
        this.prescriptionType = value;
    }

    /**
     * Gets the value of the pharmacyText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPharmacyText() {
        return pharmacyText;
    }

    /**
     * Sets the value of the pharmacyText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPharmacyText(String value) {
        this.pharmacyText = value;
    }

    /**
     * Gets the value of the patientText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientText() {
        return patientText;
    }

    /**
     * Sets the value of the patientText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientText(String value) {
        this.patientText = value;
    }

    /**
     * Gets the value of the drugStatus property.
     * 
     * @return
     *     possible object is
     *     {@link VocDrugStatus }
     *     
     */
    public VocDrugStatus getDrugStatus() {
        return drugStatus;
    }

    /**
     * Sets the value of the drugStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocDrugStatus }
     *     
     */
    public void setDrugStatus(VocDrugStatus value) {
        this.drugStatus = value;
    }

    /**
     * Gets the value of the private property.
     * 
     */
    public boolean isPrivate() {
        return _private;
    }

    /**
     * Sets the value of the private property.
     * 
     */
    public void setPrivate(boolean value) {
        this._private = value;
    }

    /**
     * Gets the value of the minNextIssueDays property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMinNextIssueDays() {
        return minNextIssueDays;
    }

    /**
     * Sets the value of the minNextIssueDays property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMinNextIssueDays(Integer value) {
        this.minNextIssueDays = value;
    }

    /**
     * Gets the value of the maxNextIssueDays property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxNextIssueDays() {
        return maxNextIssueDays;
    }

    /**
     * Sets the value of the maxNextIssueDays property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxNextIssueDays(Integer value) {
        this.maxNextIssueDays = value;
    }

    /**
     * Gets the value of the firstIssueDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFirstIssueDate() {
        return firstIssueDate;
    }

    /**
     * Sets the value of the firstIssueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFirstIssueDate(XMLGregorianCalendar value) {
        this.firstIssueDate = value;
    }

    /**
     * Gets the value of the mostRecentIssueDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMostRecentIssueDate() {
        return mostRecentIssueDate;
    }

    /**
     * Sets the value of the mostRecentIssueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMostRecentIssueDate(XMLGregorianCalendar value) {
        this.mostRecentIssueDate = value;
    }

    /**
     * Gets the value of the numberOfIssues property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfIssues() {
        return numberOfIssues;
    }

    /**
     * Sets the value of the numberOfIssues property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfIssues(Integer value) {
        this.numberOfIssues = value;
    }

    /**
     * Gets the value of the authorisedIssues property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the authorisedIssues property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAuthorisedIssues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001AuthorisedIssue }
     * 
     * 
     */
    public List<OpenHR001AuthorisedIssue> getAuthorisedIssues() {
        if (authorisedIssues == null) {
            authorisedIssues = new ArrayList<OpenHR001AuthorisedIssue>();
        }
        return this.authorisedIssues;
    }

    /**
     * Gets the value of the averageCompliance property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAverageCompliance() {
        return averageCompliance;
    }

    /**
     * Sets the value of the averageCompliance property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAverageCompliance(Integer value) {
        this.averageCompliance = value;
    }

    /**
     * Gets the value of the mostRecentIssueMethod property.
     * 
     * @return
     *     possible object is
     *     {@link VocDrugIssueType }
     *     
     */
    public VocDrugIssueType getMostRecentIssueMethod() {
        return mostRecentIssueMethod;
    }

    /**
     * Sets the value of the mostRecentIssueMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocDrugIssueType }
     *     
     */
    public void setMostRecentIssueMethod(VocDrugIssueType value) {
        this.mostRecentIssueMethod = value;
    }

    /**
     * Gets the value of the prescribedAsContraceptive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPrescribedAsContraceptive() {
        return prescribedAsContraceptive;
    }

    /**
     * Sets the value of the prescribedAsContraceptive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPrescribedAsContraceptive(Boolean value) {
        this.prescribedAsContraceptive = value;
    }

    /**
     * Gets the value of the cancellation property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001MedicationCancel }
     *     
     */
    public OpenHR001MedicationCancel getCancellation() {
        return cancellation;
    }

    /**
     * Sets the value of the cancellation property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001MedicationCancel }
     *     
     */
    public void setCancellation(OpenHR001MedicationCancel value) {
        this.cancellation = value;
    }

    /**
     * Gets the value of the dmdUnitOfMeasure property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDmdUnitOfMeasure() {
        return dmdUnitOfMeasure;
    }

    /**
     * Sets the value of the dmdUnitOfMeasure property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDmdUnitOfMeasure(Long value) {
        this.dmdUnitOfMeasure = value;
    }

}

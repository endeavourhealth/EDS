
package org.endeavourhealth.transform.emis.openhr.schema;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.MedicationIssue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.MedicationIssue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="medication" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
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
 *         &lt;element name="compliance" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}short">
 *               &lt;minInclusive value="0"/>
 *               &lt;maxInclusive value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="prescriptionType" type="{http://www.e-mis.com/emisopen}voc.PrescriptionType"/>
 *         &lt;element name="estimatedNHSCost" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="pharmacyStamp" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="issueMethod" type="{http://www.e-mis.com/emisopen}voc.DrugIssueType"/>
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
 *         &lt;element name="pharmacyMessage" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="patientMessage" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="private" type="{http://www.e-mis.com/emisopen}dt.bool"/>
 *         &lt;element name="cancelled" type="{http://www.e-mis.com/emisopen}dt.bool"/>
 *         &lt;element name="prescribedAsContraceptive" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="dmdUnitOfMeasure" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="repeatDispensingInterval" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="repeatDispensingIssueCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="batchNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.MedicationIssue", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "medication",
    "localMixture",
    "dosage",
    "quantity",
    "quantityUnit",
    "quantityRepresentation",
    "durationOfIssue",
    "compliance",
    "prescriptionType",
    "estimatedNHSCost",
    "pharmacyStamp",
    "issueMethod",
    "pharmacyText",
    "patientText",
    "pharmacyMessage",
    "patientMessage",
    "_private",
    "cancelled",
    "prescribedAsContraceptive",
    "dmdUnitOfMeasure",
    "repeatDispensingInterval",
    "repeatDispensingIssueCount",
    "batchNumber"
})
public class OpenHR001MedicationIssue {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String medication;
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
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Short compliance;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocPrescriptionType prescriptionType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected BigDecimal estimatedNHSCost;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String pharmacyStamp;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocDrugIssueType issueMethod;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String pharmacyText;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String patientText;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String pharmacyMessage;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String patientMessage;
    @XmlElement(name = "private", namespace = "http://www.e-mis.com/emisopen")
    protected boolean _private;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected boolean cancelled;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean prescribedAsContraceptive;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Long dmdUnitOfMeasure;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Integer repeatDispensingInterval;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Integer repeatDispensingIssueCount;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String batchNumber;

    /**
     * Gets the value of the medication property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMedication() {
        return medication;
    }

    /**
     * Sets the value of the medication property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMedication(String value) {
        this.medication = value;
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
     * Gets the value of the compliance property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getCompliance() {
        return compliance;
    }

    /**
     * Sets the value of the compliance property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setCompliance(Short value) {
        this.compliance = value;
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
     * Gets the value of the estimatedNHSCost property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getEstimatedNHSCost() {
        return estimatedNHSCost;
    }

    /**
     * Sets the value of the estimatedNHSCost property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setEstimatedNHSCost(BigDecimal value) {
        this.estimatedNHSCost = value;
    }

    /**
     * Gets the value of the pharmacyStamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPharmacyStamp() {
        return pharmacyStamp;
    }

    /**
     * Sets the value of the pharmacyStamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPharmacyStamp(String value) {
        this.pharmacyStamp = value;
    }

    /**
     * Gets the value of the issueMethod property.
     * 
     * @return
     *     possible object is
     *     {@link VocDrugIssueType }
     *     
     */
    public VocDrugIssueType getIssueMethod() {
        return issueMethod;
    }

    /**
     * Sets the value of the issueMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocDrugIssueType }
     *     
     */
    public void setIssueMethod(VocDrugIssueType value) {
        this.issueMethod = value;
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
     * Gets the value of the pharmacyMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPharmacyMessage() {
        return pharmacyMessage;
    }

    /**
     * Sets the value of the pharmacyMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPharmacyMessage(String value) {
        this.pharmacyMessage = value;
    }

    /**
     * Gets the value of the patientMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientMessage() {
        return patientMessage;
    }

    /**
     * Sets the value of the patientMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientMessage(String value) {
        this.patientMessage = value;
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
     * Gets the value of the cancelled property.
     * 
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the value of the cancelled property.
     * 
     */
    public void setCancelled(boolean value) {
        this.cancelled = value;
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

    /**
     * Gets the value of the repeatDispensingInterval property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRepeatDispensingInterval() {
        return repeatDispensingInterval;
    }

    /**
     * Sets the value of the repeatDispensingInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRepeatDispensingInterval(Integer value) {
        this.repeatDispensingInterval = value;
    }

    /**
     * Gets the value of the repeatDispensingIssueCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRepeatDispensingIssueCount() {
        return repeatDispensingIssueCount;
    }

    /**
     * Sets the value of the repeatDispensingIssueCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRepeatDispensingIssueCount(Integer value) {
        this.repeatDispensingIssueCount = value;
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

}

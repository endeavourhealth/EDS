
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * A drug within a record, used for both drug records and issue records
 * 
 * <p>Java class for MedicationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MedicationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="AuthorisedUserID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="OriginalAuthor" type="{http://www.e-mis.com/emisopen/MedicalRecord}AuthorType" minOccurs="0"/>
 *         &lt;element name="AssignedDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DateLastIssue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IssueMethod" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="S"/>
 *               &lt;enumeration value="Q"/>
 *               &lt;enumeration value="H"/>
 *               &lt;enumeration value="X"/>
 *               &lt;enumeration value="O"/>
 *               &lt;enumeration value="P"/>
 *               &lt;enumeration value="N"/>
 *               &lt;enumeration value="F"/>
 *               &lt;enumeration value="D"/>
 *               &lt;enumeration value="B"/>
 *               &lt;enumeration value="R"/>
 *               &lt;enumeration value="E"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="DateRxExpire" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DoseAbbreviation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Dosage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="QuantityUnits" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="QuantityRepresentation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Duration" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="MixtureIndicator" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="Drug">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="PreparationID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType" minOccurs="0"/>
 *                   &lt;element name="Mixture" type="{http://www.e-mis.com/emisopen/MedicalRecord}MixtureType"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PrescriptionType" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="ACUTE"/>
 *               &lt;enumeration value="REPEAT"/>
 *               &lt;enumeration value="AUTOMATIC"/>
 *               &lt;enumeration value=""/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="DrugSource" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RxReviewDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PharmacyText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RxText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AuthorisedIssue" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="IssueCount" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="Status" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="C"/>
 *               &lt;enumeration value="N"/>
 *               &lt;enumeration value="P"/>
 *               &lt;enumeration value="A"/>
 *               &lt;enumeration value="R"/>
 *               &lt;enumeration value="G"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Advice" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Template" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TemplateComponent" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TemplateInstance" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="DrugDelivery" type="{http://www.e-mis.com/emisopen/MedicalRecord}DrugDeliveryType" minOccurs="0"/>
 *         &lt;element name="ControlledDrug" type="{http://www.e-mis.com/emisopen/MedicalRecord}ControlledDrugInfoType" minOccurs="0"/>
 *         &lt;element name="ProblemLinkList" type="{http://www.e-mis.com/emisopen/MedicalRecord}LinkListType" minOccurs="0"/>
 *         &lt;element name="ServiceType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DispensingInterval" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="RepeatIssueCount" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="RepeatInterval" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="ETPIssue" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="ContraceptiveIssue" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="PolicyId" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MedicationType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "authorisedUserID",
    "originalAuthor",
    "assignedDate",
    "dateLastIssue",
    "issueMethod",
    "dateRxExpire",
    "doseAbbreviation",
    "dosage",
    "quantity",
    "quantityUnits",
    "quantityRepresentation",
    "duration",
    "mixtureIndicator",
    "drug",
    "prescriptionType",
    "drugSource",
    "rxReviewDate",
    "pharmacyText",
    "rxText",
    "authorisedIssue",
    "issueCount",
    "status",
    "advice",
    "template",
    "templateComponent",
    "templateInstance",
    "drugDelivery",
    "controlledDrug",
    "problemLinkList",
    "serviceType",
    "dispensingInterval",
    "repeatIssueCount",
    "repeatInterval",
    "etpIssue",
    "contraceptiveIssue",
    "policyId"
})
@XmlSeeAlso({
    IssueType.class
})
public class MedicationType
    extends IdentType
{

    @XmlElement(name = "AuthorisedUserID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType authorisedUserID;
    @XmlElement(name = "OriginalAuthor", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected AuthorType originalAuthor;
    @XmlElement(name = "AssignedDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String assignedDate;
    @XmlElement(name = "DateLastIssue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateLastIssue;
    @XmlElement(name = "IssueMethod", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String issueMethod;
    @XmlElement(name = "DateRxExpire", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dateRxExpire;
    @XmlElement(name = "DoseAbbreviation", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String doseAbbreviation;
    @XmlElement(name = "Dosage", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dosage;
    @XmlElement(name = "Quantity", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Float quantity;
    @XmlElement(name = "QuantityUnits", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String quantityUnits;
    @XmlElement(name = "QuantityRepresentation", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String quantityRepresentation;
    @XmlElement(name = "Duration", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger duration;
    @XmlElement(name = "MixtureIndicator", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger mixtureIndicator;
    @XmlElement(name = "Drug", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected MedicationType.Drug drug;
    @XmlElement(name = "PrescriptionType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String prescriptionType;
    @XmlElement(name = "DrugSource", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String drugSource;
    @XmlElement(name = "RxReviewDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String rxReviewDate;
    @XmlElement(name = "PharmacyText", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String pharmacyText;
    @XmlElement(name = "RxText", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String rxText;
    @XmlElement(name = "AuthorisedIssue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger authorisedIssue;
    @XmlElement(name = "IssueCount", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger issueCount;
    @XmlElement(name = "Status", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String status;
    @XmlElement(name = "Advice", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String advice;
    @XmlElement(name = "Template", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String template;
    @XmlElement(name = "TemplateComponent", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String templateComponent;
    @XmlElement(name = "TemplateInstance", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger templateInstance;
    @XmlElement(name = "DrugDelivery", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected DrugDeliveryType drugDelivery;
    @XmlElement(name = "ControlledDrug", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected ControlledDrugInfoType controlledDrug;
    @XmlElement(name = "ProblemLinkList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected LinkListType problemLinkList;
    @XmlElement(name = "ServiceType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String serviceType;
    @XmlElement(name = "DispensingInterval", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger dispensingInterval;
    @XmlElement(name = "RepeatIssueCount", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger repeatIssueCount;
    @XmlElement(name = "RepeatInterval", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger repeatInterval;
    @XmlElement(name = "ETPIssue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger etpIssue;
    @XmlElement(name = "ContraceptiveIssue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger contraceptiveIssue;
    @XmlElement(name = "PolicyId", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger policyId;

    /**
     * Gets the value of the authorisedUserID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getAuthorisedUserID() {
        return authorisedUserID;
    }

    /**
     * Sets the value of the authorisedUserID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setAuthorisedUserID(IdentType value) {
        this.authorisedUserID = value;
    }

    /**
     * Gets the value of the originalAuthor property.
     * 
     * @return
     *     possible object is
     *     {@link AuthorType }
     *     
     */
    public AuthorType getOriginalAuthor() {
        return originalAuthor;
    }

    /**
     * Sets the value of the originalAuthor property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthorType }
     *     
     */
    public void setOriginalAuthor(AuthorType value) {
        this.originalAuthor = value;
    }

    /**
     * Gets the value of the assignedDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssignedDate() {
        return assignedDate;
    }

    /**
     * Sets the value of the assignedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssignedDate(String value) {
        this.assignedDate = value;
    }

    /**
     * Gets the value of the dateLastIssue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateLastIssue() {
        return dateLastIssue;
    }

    /**
     * Sets the value of the dateLastIssue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateLastIssue(String value) {
        this.dateLastIssue = value;
    }

    /**
     * Gets the value of the issueMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIssueMethod() {
        return issueMethod;
    }

    /**
     * Sets the value of the issueMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIssueMethod(String value) {
        this.issueMethod = value;
    }

    /**
     * Gets the value of the dateRxExpire property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateRxExpire() {
        return dateRxExpire;
    }

    /**
     * Sets the value of the dateRxExpire property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateRxExpire(String value) {
        this.dateRxExpire = value;
    }

    /**
     * Gets the value of the doseAbbreviation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDoseAbbreviation() {
        return doseAbbreviation;
    }

    /**
     * Sets the value of the doseAbbreviation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDoseAbbreviation(String value) {
        this.doseAbbreviation = value;
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
     *     {@link Float }
     *     
     */
    public Float getQuantity() {
        return quantity;
    }

    /**
     * Sets the value of the quantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setQuantity(Float value) {
        this.quantity = value;
    }

    /**
     * Gets the value of the quantityUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuantityUnits() {
        return quantityUnits;
    }

    /**
     * Sets the value of the quantityUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuantityUnits(String value) {
        this.quantityUnits = value;
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
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDuration(BigInteger value) {
        this.duration = value;
    }

    /**
     * Gets the value of the mixtureIndicator property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMixtureIndicator() {
        return mixtureIndicator;
    }

    /**
     * Sets the value of the mixtureIndicator property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMixtureIndicator(BigInteger value) {
        this.mixtureIndicator = value;
    }

    /**
     * Gets the value of the drug property.
     * 
     * @return
     *     possible object is
     *     {@link MedicationType.Drug }
     *     
     */
    public MedicationType.Drug getDrug() {
        return drug;
    }

    /**
     * Sets the value of the drug property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicationType.Drug }
     *     
     */
    public void setDrug(MedicationType.Drug value) {
        this.drug = value;
    }

    /**
     * Gets the value of the prescriptionType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrescriptionType() {
        return prescriptionType;
    }

    /**
     * Sets the value of the prescriptionType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrescriptionType(String value) {
        this.prescriptionType = value;
    }

    /**
     * Gets the value of the drugSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDrugSource() {
        return drugSource;
    }

    /**
     * Sets the value of the drugSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDrugSource(String value) {
        this.drugSource = value;
    }

    /**
     * Gets the value of the rxReviewDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRxReviewDate() {
        return rxReviewDate;
    }

    /**
     * Sets the value of the rxReviewDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRxReviewDate(String value) {
        this.rxReviewDate = value;
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
     * Gets the value of the rxText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRxText() {
        return rxText;
    }

    /**
     * Sets the value of the rxText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRxText(String value) {
        this.rxText = value;
    }

    /**
     * Gets the value of the authorisedIssue property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAuthorisedIssue() {
        return authorisedIssue;
    }

    /**
     * Sets the value of the authorisedIssue property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAuthorisedIssue(BigInteger value) {
        this.authorisedIssue = value;
    }

    /**
     * Gets the value of the issueCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getIssueCount() {
        return issueCount;
    }

    /**
     * Sets the value of the issueCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setIssueCount(BigInteger value) {
        this.issueCount = value;
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
     * Gets the value of the advice property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdvice() {
        return advice;
    }

    /**
     * Sets the value of the advice property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdvice(String value) {
        this.advice = value;
    }

    /**
     * Gets the value of the template property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplate(String value) {
        this.template = value;
    }

    /**
     * Gets the value of the templateComponent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateComponent() {
        return templateComponent;
    }

    /**
     * Sets the value of the templateComponent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateComponent(String value) {
        this.templateComponent = value;
    }

    /**
     * Gets the value of the templateInstance property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTemplateInstance() {
        return templateInstance;
    }

    /**
     * Sets the value of the templateInstance property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTemplateInstance(BigInteger value) {
        this.templateInstance = value;
    }

    /**
     * Gets the value of the drugDelivery property.
     * 
     * @return
     *     possible object is
     *     {@link DrugDeliveryType }
     *     
     */
    public DrugDeliveryType getDrugDelivery() {
        return drugDelivery;
    }

    /**
     * Sets the value of the drugDelivery property.
     * 
     * @param value
     *     allowed object is
     *     {@link DrugDeliveryType }
     *     
     */
    public void setDrugDelivery(DrugDeliveryType value) {
        this.drugDelivery = value;
    }

    /**
     * Gets the value of the controlledDrug property.
     * 
     * @return
     *     possible object is
     *     {@link ControlledDrugInfoType }
     *     
     */
    public ControlledDrugInfoType getControlledDrug() {
        return controlledDrug;
    }

    /**
     * Sets the value of the controlledDrug property.
     * 
     * @param value
     *     allowed object is
     *     {@link ControlledDrugInfoType }
     *     
     */
    public void setControlledDrug(ControlledDrugInfoType value) {
        this.controlledDrug = value;
    }

    /**
     * Gets the value of the problemLinkList property.
     * 
     * @return
     *     possible object is
     *     {@link LinkListType }
     *     
     */
    public LinkListType getProblemLinkList() {
        return problemLinkList;
    }

    /**
     * Sets the value of the problemLinkList property.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkListType }
     *     
     */
    public void setProblemLinkList(LinkListType value) {
        this.problemLinkList = value;
    }

    /**
     * Gets the value of the serviceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Sets the value of the serviceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceType(String value) {
        this.serviceType = value;
    }

    /**
     * Gets the value of the dispensingInterval property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDispensingInterval() {
        return dispensingInterval;
    }

    /**
     * Sets the value of the dispensingInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDispensingInterval(BigInteger value) {
        this.dispensingInterval = value;
    }

    /**
     * Gets the value of the repeatIssueCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRepeatIssueCount() {
        return repeatIssueCount;
    }

    /**
     * Sets the value of the repeatIssueCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRepeatIssueCount(BigInteger value) {
        this.repeatIssueCount = value;
    }

    /**
     * Gets the value of the repeatInterval property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRepeatInterval() {
        return repeatInterval;
    }

    /**
     * Sets the value of the repeatInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRepeatInterval(BigInteger value) {
        this.repeatInterval = value;
    }

    /**
     * Gets the value of the etpIssue property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getETPIssue() {
        return etpIssue;
    }

    /**
     * Sets the value of the etpIssue property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setETPIssue(BigInteger value) {
        this.etpIssue = value;
    }

    /**
     * Gets the value of the contraceptiveIssue property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getContraceptiveIssue() {
        return contraceptiveIssue;
    }

    /**
     * Sets the value of the contraceptiveIssue property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setContraceptiveIssue(BigInteger value) {
        this.contraceptiveIssue = value;
    }

    /**
     * Gets the value of the policyId property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPolicyId() {
        return policyId;
    }

    /**
     * Sets the value of the policyId property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPolicyId(BigInteger value) {
        this.policyId = value;
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
     *       &lt;choice>
     *         &lt;element name="PreparationID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType" minOccurs="0"/>
     *         &lt;element name="Mixture" type="{http://www.e-mis.com/emisopen/MedicalRecord}MixtureType"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "preparationID",
        "mixture"
    })
    public static class Drug {

        @XmlElement(name = "PreparationID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected IntegerCodeType preparationID;
        @XmlElement(name = "Mixture", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected MixtureType mixture;

        /**
         * Gets the value of the preparationID property.
         * 
         * @return
         *     possible object is
         *     {@link IntegerCodeType }
         *     
         */
        public IntegerCodeType getPreparationID() {
            return preparationID;
        }

        /**
         * Sets the value of the preparationID property.
         * 
         * @param value
         *     allowed object is
         *     {@link IntegerCodeType }
         *     
         */
        public void setPreparationID(IntegerCodeType value) {
            this.preparationID = value;
        }

        /**
         * Gets the value of the mixture property.
         * 
         * @return
         *     possible object is
         *     {@link MixtureType }
         *     
         */
        public MixtureType getMixture() {
            return mixture;
        }

        /**
         * Sets the value of the mixture property.
         * 
         * @param value
         *     allowed object is
         *     {@link MixtureType }
         *     
         */
        public void setMixture(MixtureType value) {
            this.mixture = value;
        }

    }

}

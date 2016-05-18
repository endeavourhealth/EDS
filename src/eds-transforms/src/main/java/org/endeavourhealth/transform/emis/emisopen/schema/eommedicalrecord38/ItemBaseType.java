
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Base Item Type . Used  as a base class for coded entries of various kinds
 * 
 * <p>Java class for ItemBaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ItemBaseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="AssignedDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DatePart" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}short">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *               &lt;enumeration value="3"/>
 *               &lt;enumeration value="4"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="AssignedTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Sequence" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="AuthorID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="OriginalAuthor" type="{http://www.e-mis.com/emisopen/MedicalRecord}AuthorType" minOccurs="0"/>
 *         &lt;element name="ExternalConsultant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DisplayTerm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DescriptiveText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataSource" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="PolicyID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemBaseType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "assignedDate",
    "datePart",
    "assignedTime",
    "sequence",
    "authorID",
    "originalAuthor",
    "externalConsultant",
    "displayTerm",
    "descriptiveText",
    "dataSource",
    "policyID"
})
@XmlSeeAlso({
    CodedItemBaseType.class
})
public class ItemBaseType
    extends IdentType
{

    @XmlElement(name = "AssignedDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String assignedDate;
    @XmlElement(name = "DatePart", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Short datePart;
    @XmlElement(name = "AssignedTime", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String assignedTime;
    @XmlElement(name = "Sequence", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger sequence;
    @XmlElement(name = "AuthorID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType authorID;
    @XmlElement(name = "OriginalAuthor", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected AuthorType originalAuthor;
    @XmlElement(name = "ExternalConsultant", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String externalConsultant;
    @XmlElement(name = "DisplayTerm", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String displayTerm;
    @XmlElement(name = "DescriptiveText", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String descriptiveText;
    @XmlElement(name = "DataSource", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger dataSource;
    @XmlElement(name = "PolicyID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger policyID;

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
     * Gets the value of the datePart property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getDatePart() {
        return datePart;
    }

    /**
     * Sets the value of the datePart property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setDatePart(Short value) {
        this.datePart = value;
    }

    /**
     * Gets the value of the assignedTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssignedTime() {
        return assignedTime;
    }

    /**
     * Sets the value of the assignedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssignedTime(String value) {
        this.assignedTime = value;
    }

    /**
     * Gets the value of the sequence property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSequence() {
        return sequence;
    }

    /**
     * Sets the value of the sequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSequence(BigInteger value) {
        this.sequence = value;
    }

    /**
     * Gets the value of the authorID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getAuthorID() {
        return authorID;
    }

    /**
     * Sets the value of the authorID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setAuthorID(IdentType value) {
        this.authorID = value;
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
     * Gets the value of the externalConsultant property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalConsultant() {
        return externalConsultant;
    }

    /**
     * Sets the value of the externalConsultant property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalConsultant(String value) {
        this.externalConsultant = value;
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
     * Gets the value of the descriptiveText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescriptiveText() {
        return descriptiveText;
    }

    /**
     * Sets the value of the descriptiveText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescriptiveText(String value) {
        this.descriptiveText = value;
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
     * Gets the value of the policyID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPolicyID() {
        return policyID;
    }

    /**
     * Sets the value of the policyID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPolicyID(BigInteger value) {
        this.policyID = value;
    }

}

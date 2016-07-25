
package org.endeavourhealth.transform.tpp.xml.schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ClinicalCode complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ClinicalCode">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Code" type="{}Code"/>
 *         &lt;element name="Episodicity" type="{}Episodicity" minOccurs="0"/>
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="Units" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ProblemSeverity" type="{}ProblemSeverity" minOccurs="0"/>
 *         &lt;element name="ProblemEndDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="FreeText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LinkedProblemUID" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="IsAllergy" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="IsFamilyHistory" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
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
@XmlType(name = "ClinicalCode", propOrder = {
    "code",
    "episodicity",
    "value",
    "units",
    "problemSeverity",
    "problemEndDate",
    "freeText",
    "linkedProblemUID",
    "isAllergy",
    "isFamilyHistory"
})
public class ClinicalCode {

    @XmlElement(name = "Code", required = true)
    protected Code code;
    @XmlElement(name = "Episodicity")
    @XmlSchemaType(name = "string")
    protected Episodicity episodicity;
    @XmlElement(name = "Value")
    protected BigDecimal value;
    @XmlElement(name = "Units")
    protected String units;
    @XmlElement(name = "ProblemSeverity")
    @XmlSchemaType(name = "string")
    protected ProblemSeverity problemSeverity;
    @XmlElement(name = "ProblemEndDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar problemEndDate;
    @XmlElement(name = "FreeText")
    protected String freeText;
    @XmlElement(name = "LinkedProblemUID")
    protected List<String> linkedProblemUID;
    @XmlElement(name = "IsAllergy")
    protected ClinicalCode.IsAllergy isAllergy;
    @XmlElement(name = "IsFamilyHistory")
    protected ClinicalCode.IsFamilyHistory isFamilyHistory;

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link Code }
     *     
     */
    public Code getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link Code }
     *     
     */
    public void setCode(Code value) {
        this.code = value;
    }

    /**
     * Gets the value of the episodicity property.
     * 
     * @return
     *     possible object is
     *     {@link Episodicity }
     *     
     */
    public Episodicity getEpisodicity() {
        return episodicity;
    }

    /**
     * Sets the value of the episodicity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Episodicity }
     *     
     */
    public void setEpisodicity(Episodicity value) {
        this.episodicity = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Gets the value of the units property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnits(String value) {
        this.units = value;
    }

    /**
     * Gets the value of the problemSeverity property.
     * 
     * @return
     *     possible object is
     *     {@link ProblemSeverity }
     *     
     */
    public ProblemSeverity getProblemSeverity() {
        return problemSeverity;
    }

    /**
     * Sets the value of the problemSeverity property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProblemSeverity }
     *     
     */
    public void setProblemSeverity(ProblemSeverity value) {
        this.problemSeverity = value;
    }

    /**
     * Gets the value of the problemEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getProblemEndDate() {
        return problemEndDate;
    }

    /**
     * Sets the value of the problemEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setProblemEndDate(XMLGregorianCalendar value) {
        this.problemEndDate = value;
    }

    /**
     * Gets the value of the freeText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFreeText() {
        return freeText;
    }

    /**
     * Sets the value of the freeText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFreeText(String value) {
        this.freeText = value;
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
     * Gets the value of the isAllergy property.
     * 
     * @return
     *     possible object is
     *     {@link ClinicalCode.IsAllergy }
     *     
     */
    public ClinicalCode.IsAllergy getIsAllergy() {
        return isAllergy;
    }

    /**
     * Sets the value of the isAllergy property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClinicalCode.IsAllergy }
     *     
     */
    public void setIsAllergy(ClinicalCode.IsAllergy value) {
        this.isAllergy = value;
    }

    /**
     * Gets the value of the isFamilyHistory property.
     * 
     * @return
     *     possible object is
     *     {@link ClinicalCode.IsFamilyHistory }
     *     
     */
    public ClinicalCode.IsFamilyHistory getIsFamilyHistory() {
        return isFamilyHistory;
    }

    /**
     * Sets the value of the isFamilyHistory property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClinicalCode.IsFamilyHistory }
     *     
     */
    public void setIsFamilyHistory(ClinicalCode.IsFamilyHistory value) {
        this.isFamilyHistory = value;
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
    public static class IsAllergy {


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
    public static class IsFamilyHistory {


    }

}

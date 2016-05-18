
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Contains problem details of a clinical problem or community episode
 * Problems may be linked in a heirarchy
 * 
 * <p>Java class for ProblemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProblemType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ProblemStatus" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}byte">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *               &lt;enumeration value="3"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="GroupingStatus" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}byte">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="EndDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EndDatePart" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
 *         &lt;element name="ProblemType" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}byte">
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *               &lt;enumeration value="3"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Significance" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}byte">
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *               &lt;enumeration value="3"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ExpectedDuration" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="ParentProblem" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="Owner" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
 *         &lt;element name="CareAimList" type="{http://www.e-mis.com/emisopen/MedicalRecord}CareAimListType" minOccurs="0"/>
 *         &lt;element name="CarePlanList" type="{http://www.e-mis.com/emisopen/MedicalRecord}CarePlanListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProblemType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "problemStatus",
    "groupingStatus",
    "endDate",
    "endDatePart",
    "problemType",
    "significance",
    "expectedDuration",
    "parentProblem",
    "owner",
    "careAimList",
    "carePlanList"
})
public class ProblemType {

    @XmlElement(name = "ProblemStatus", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte problemStatus;
    @XmlElement(name = "GroupingStatus", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte groupingStatus;
    @XmlElement(name = "EndDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String endDate;
    @XmlElement(name = "EndDatePart", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte endDatePart;
    @XmlElement(name = "ProblemType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte problemType;
    @XmlElement(name = "Significance", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte significance;
    @XmlElement(name = "ExpectedDuration", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger expectedDuration;
    @XmlElement(name = "ParentProblem", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType parentProblem;
    @XmlElement(name = "Owner", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte owner;
    @XmlElement(name = "CareAimList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected CareAimListType careAimList;
    @XmlElement(name = "CarePlanList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected CarePlanListType carePlanList;

    /**
     * Gets the value of the problemStatus property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getProblemStatus() {
        return problemStatus;
    }

    /**
     * Sets the value of the problemStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setProblemStatus(Byte value) {
        this.problemStatus = value;
    }

    /**
     * Gets the value of the groupingStatus property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getGroupingStatus() {
        return groupingStatus;
    }

    /**
     * Sets the value of the groupingStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setGroupingStatus(Byte value) {
        this.groupingStatus = value;
    }

    /**
     * Gets the value of the endDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Sets the value of the endDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndDate(String value) {
        this.endDate = value;
    }

    /**
     * Gets the value of the endDatePart property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getEndDatePart() {
        return endDatePart;
    }

    /**
     * Sets the value of the endDatePart property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setEndDatePart(Byte value) {
        this.endDatePart = value;
    }

    /**
     * Gets the value of the problemType property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getProblemType() {
        return problemType;
    }

    /**
     * Sets the value of the problemType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setProblemType(Byte value) {
        this.problemType = value;
    }

    /**
     * Gets the value of the significance property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getSignificance() {
        return significance;
    }

    /**
     * Sets the value of the significance property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setSignificance(Byte value) {
        this.significance = value;
    }

    /**
     * Gets the value of the expectedDuration property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getExpectedDuration() {
        return expectedDuration;
    }

    /**
     * Sets the value of the expectedDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setExpectedDuration(BigInteger value) {
        this.expectedDuration = value;
    }

    /**
     * Gets the value of the parentProblem property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getParentProblem() {
        return parentProblem;
    }

    /**
     * Sets the value of the parentProblem property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setParentProblem(IdentType value) {
        this.parentProblem = value;
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setOwner(Byte value) {
        this.owner = value;
    }

    /**
     * Gets the value of the careAimList property.
     * 
     * @return
     *     possible object is
     *     {@link CareAimListType }
     *     
     */
    public CareAimListType getCareAimList() {
        return careAimList;
    }

    /**
     * Sets the value of the careAimList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CareAimListType }
     *     
     */
    public void setCareAimList(CareAimListType value) {
        this.careAimList = value;
    }

    /**
     * Gets the value of the carePlanList property.
     * 
     * @return
     *     possible object is
     *     {@link CarePlanListType }
     *     
     */
    public CarePlanListType getCarePlanList() {
        return carePlanList;
    }

    /**
     * Sets the value of the carePlanList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CarePlanListType }
     *     
     */
    public void setCarePlanList(CarePlanListType value) {
        this.carePlanList = value;
    }

}

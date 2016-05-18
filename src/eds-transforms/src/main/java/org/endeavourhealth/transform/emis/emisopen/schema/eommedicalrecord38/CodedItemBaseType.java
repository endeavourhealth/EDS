
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Base Coded Item Type. Extends the base item type with a code and term ID
 * 
 * <p>Java class for CodedItemBaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CodedItemBaseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}ItemBaseType">
 *       &lt;sequence>
 *         &lt;element name="Code" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="TermID" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="QualifierList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Qualifier" type="{http://www.e-mis.com/emisopen/MedicalRecord}QualifierType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Problem" type="{http://www.e-mis.com/emisopen/MedicalRecord}ProblemType" minOccurs="0"/>
 *         &lt;element name="ProblemLinkList" type="{http://www.e-mis.com/emisopen/MedicalRecord}LinkListType" minOccurs="0"/>
 *         &lt;element name="ReferralLinkList" type="{http://www.e-mis.com/emisopen/MedicalRecord}LinkListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CodedItemBaseType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "code",
    "termID",
    "qualifierList",
    "problem",
    "problemLinkList",
    "referralLinkList"
})
@XmlSeeAlso({
    EventType.class,
    TestRequestType.class,
    ReferralType.class,
    AlertType.class,
    AttachmentType.class,
    DiaryType.class,
    AllergyType.class
})
public class CodedItemBaseType
    extends ItemBaseType
{

    @XmlElement(name = "Code", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType code;
    @XmlElement(name = "TermID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType termID;
    @XmlElement(name = "QualifierList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected CodedItemBaseType.QualifierList qualifierList;
    @XmlElement(name = "Problem", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected ProblemType problem;
    @XmlElement(name = "ProblemLinkList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected LinkListType problemLinkList;
    @XmlElement(name = "ReferralLinkList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected LinkListType referralLinkList;

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setCode(StringCodeType value) {
        this.code = value;
    }

    /**
     * Gets the value of the termID property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getTermID() {
        return termID;
    }

    /**
     * Sets the value of the termID property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setTermID(StringCodeType value) {
        this.termID = value;
    }

    /**
     * Gets the value of the qualifierList property.
     * 
     * @return
     *     possible object is
     *     {@link CodedItemBaseType.QualifierList }
     *     
     */
    public CodedItemBaseType.QualifierList getQualifierList() {
        return qualifierList;
    }

    /**
     * Sets the value of the qualifierList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CodedItemBaseType.QualifierList }
     *     
     */
    public void setQualifierList(CodedItemBaseType.QualifierList value) {
        this.qualifierList = value;
    }

    /**
     * Gets the value of the problem property.
     * 
     * @return
     *     possible object is
     *     {@link ProblemType }
     *     
     */
    public ProblemType getProblem() {
        return problem;
    }

    /**
     * Sets the value of the problem property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProblemType }
     *     
     */
    public void setProblem(ProblemType value) {
        this.problem = value;
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
     * Gets the value of the referralLinkList property.
     * 
     * @return
     *     possible object is
     *     {@link LinkListType }
     *     
     */
    public LinkListType getReferralLinkList() {
        return referralLinkList;
    }

    /**
     * Sets the value of the referralLinkList property.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkListType }
     *     
     */
    public void setReferralLinkList(LinkListType value) {
        this.referralLinkList = value;
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
     *         &lt;element name="Qualifier" type="{http://www.e-mis.com/emisopen/MedicalRecord}QualifierType" maxOccurs="unbounded" minOccurs="0"/>
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
        "qualifier"
    })
    public static class QualifierList {

        @XmlElement(name = "Qualifier", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected List<QualifierType> qualifier;

        /**
         * Gets the value of the qualifier property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the qualifier property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getQualifier().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link QualifierType }
         * 
         * 
         */
        public List<QualifierType> getQualifier() {
            if (qualifier == null) {
                qualifier = new ArrayList<QualifierType>();
            }
            return this.qualifier;
        }

    }

}

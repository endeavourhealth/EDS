
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PathologyInvestigationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PathologyInvestigationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}InvestigationTypeBase">
 *       &lt;sequence>
 *         &lt;element name="UserCommentAppliedBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LabSpecifiedCommentList" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDICommentListType" minOccurs="0"/>
 *         &lt;element name="EDIResultStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="InvestigationPerformedDateTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FiledCode" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="InvContainsStandAloneTest" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PathologyEventList" type="{http://www.e-mis.com/emisopen/MedicalRecord}PathologyTestListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PathologyInvestigationType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "userCommentAppliedBy",
    "labSpecifiedCommentList",
    "ediResultStatus",
    "investigationPerformedDateTime",
    "filedCode",
    "invContainsStandAloneTest",
    "pathologyEventList"
})
public class PathologyInvestigationType
    extends InvestigationTypeBase
{

    @XmlElement(name = "UserCommentAppliedBy", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String userCommentAppliedBy;
    @XmlElement(name = "LabSpecifiedCommentList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected EDICommentListType labSpecifiedCommentList;
    @XmlElement(name = "EDIResultStatus", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String ediResultStatus;
    @XmlElement(name = "InvestigationPerformedDateTime", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String investigationPerformedDateTime;
    @XmlElement(name = "FiledCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType filedCode;
    @XmlElement(name = "InvContainsStandAloneTest", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String invContainsStandAloneTest;
    @XmlElement(name = "PathologyEventList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected PathologyTestListType pathologyEventList;

    /**
     * Gets the value of the userCommentAppliedBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserCommentAppliedBy() {
        return userCommentAppliedBy;
    }

    /**
     * Sets the value of the userCommentAppliedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserCommentAppliedBy(String value) {
        this.userCommentAppliedBy = value;
    }

    /**
     * Gets the value of the labSpecifiedCommentList property.
     * 
     * @return
     *     possible object is
     *     {@link EDICommentListType }
     *     
     */
    public EDICommentListType getLabSpecifiedCommentList() {
        return labSpecifiedCommentList;
    }

    /**
     * Sets the value of the labSpecifiedCommentList property.
     * 
     * @param value
     *     allowed object is
     *     {@link EDICommentListType }
     *     
     */
    public void setLabSpecifiedCommentList(EDICommentListType value) {
        this.labSpecifiedCommentList = value;
    }

    /**
     * Gets the value of the ediResultStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEDIResultStatus() {
        return ediResultStatus;
    }

    /**
     * Sets the value of the ediResultStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEDIResultStatus(String value) {
        this.ediResultStatus = value;
    }

    /**
     * Gets the value of the investigationPerformedDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvestigationPerformedDateTime() {
        return investigationPerformedDateTime;
    }

    /**
     * Sets the value of the investigationPerformedDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvestigationPerformedDateTime(String value) {
        this.investigationPerformedDateTime = value;
    }

    /**
     * Gets the value of the filedCode property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getFiledCode() {
        return filedCode;
    }

    /**
     * Sets the value of the filedCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setFiledCode(StringCodeType value) {
        this.filedCode = value;
    }

    /**
     * Gets the value of the invContainsStandAloneTest property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvContainsStandAloneTest() {
        return invContainsStandAloneTest;
    }

    /**
     * Sets the value of the invContainsStandAloneTest property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvContainsStandAloneTest(String value) {
        this.invContainsStandAloneTest = value;
    }

    /**
     * Gets the value of the pathologyEventList property.
     * 
     * @return
     *     possible object is
     *     {@link PathologyTestListType }
     *     
     */
    public PathologyTestListType getPathologyEventList() {
        return pathologyEventList;
    }

    /**
     * Sets the value of the pathologyEventList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathologyTestListType }
     *     
     */
    public void setPathologyEventList(PathologyTestListType value) {
        this.pathologyEventList = value;
    }

}

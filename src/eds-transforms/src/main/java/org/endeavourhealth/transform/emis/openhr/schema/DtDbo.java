
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Database Object
 * 
 * <p>Java class for dt.dbo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.dbo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="updateMode" type="{http://www.e-mis.com/emisopen}voc.UpdateMode" default="none" />
 *       &lt;attribute name="auditDeleteDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" />
 *       &lt;attribute name="auditDeleteUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" />
 *       &lt;attribute name="auditDeleteInfo" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dt.dbo", namespace = "http://www.e-mis.com/emisopen")
@XmlSeeAlso({
    OpenHR001Document.class,
    OpenHR001UserInRole.class,
    OpenHR001User.class,
    OpenHR001CaseloadPatientProperty.class,
    OpenHR001LocalMixture.class,
    OpenHR001Event.class,
    OpenHR001Role.class,
    OpenHR001Attachment.class,
    OpenHR001Component.class,
    OpenHR001PatientCarer.class,
    OpenHR001EpisodeProblemLink.class,
    OpenHR001DiaryReminder.class,
    OpenHR001AuditTrail.class,
    OpenHR001AuthorisedIssue.class,
    OpenHR001Caseload.class,
    OpenHR001DocumentFolder.class,
    OpenHR001LocalMixtureConstituent.class,
    OpenHR001ProblemHierarchy.class,
    OpenHR001CustomRegistration.class,
    OpenHR001CaseloadPatient.class,
    OpenHR001Problem.class,
    OpenHR001OrderBatch.class,
    OpenHR001Person.class,
    OpenHR001Patient.class,
    OpenHR001PatientTask.class,
    DtContact.class,
    DtUserIdentifier.class,
    OpenHR001Organisation.Locations.class,
    OpenHR001Organisation.class,
    OpenHR001ConfidentialityPolicy.class,
    OpenHR001ProblemEventLink.class,
    OpenHR001Location.class,
    OpenHR001Encounter.class,
    DtPatientIdentifier.class,
    OpenHR001Specimen.class
})
public class DtDbo {

    @XmlAttribute(name = "updateMode")
    protected VocUpdateMode updateMode;
    @XmlAttribute(name = "auditDeleteDate")
    protected XMLGregorianCalendar auditDeleteDate;
    @XmlAttribute(name = "auditDeleteUserInRole")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String auditDeleteUserInRole;
    @XmlAttribute(name = "auditDeleteInfo")
    protected String auditDeleteInfo;

    /**
     * Gets the value of the updateMode property.
     * 
     * @return
     *     possible object is
     *     {@link VocUpdateMode }
     *     
     */
    public VocUpdateMode getUpdateMode() {
        if (updateMode == null) {
            return VocUpdateMode.NONE;
        } else {
            return updateMode;
        }
    }

    /**
     * Sets the value of the updateMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocUpdateMode }
     *     
     */
    public void setUpdateMode(VocUpdateMode value) {
        this.updateMode = value;
    }

    /**
     * Gets the value of the auditDeleteDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAuditDeleteDate() {
        return auditDeleteDate;
    }

    /**
     * Sets the value of the auditDeleteDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAuditDeleteDate(XMLGregorianCalendar value) {
        this.auditDeleteDate = value;
    }

    /**
     * Gets the value of the auditDeleteUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuditDeleteUserInRole() {
        return auditDeleteUserInRole;
    }

    /**
     * Sets the value of the auditDeleteUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuditDeleteUserInRole(String value) {
        this.auditDeleteUserInRole = value;
    }

    /**
     * Gets the value of the auditDeleteInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuditDeleteInfo() {
        return auditDeleteInfo;
    }

    /**
     * Sets the value of the auditDeleteInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuditDeleteInfo(String value) {
        this.auditDeleteInfo = value;
    }

}

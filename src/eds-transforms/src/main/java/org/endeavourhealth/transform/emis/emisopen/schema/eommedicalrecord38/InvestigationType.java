
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for InvestigationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InvestigationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}InvestigationTypeBase">
 *       &lt;sequence>
 *         &lt;element name="AssignedDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DatePart" type="{http://www.w3.org/2001/XMLSchema}short" minOccurs="0"/>
 *         &lt;element name="AssignedTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DescriptiveText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OriginalAuthor" type="{http://www.e-mis.com/emisopen/MedicalRecord}AuthorType" minOccurs="0"/>
 *         &lt;element name="AuthorID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="PolicyID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="EventList" type="{http://www.e-mis.com/emisopen/MedicalRecord}EventListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InvestigationType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "assignedDate",
    "datePart",
    "assignedTime",
    "descriptiveText",
    "originalAuthor",
    "authorID",
    "policyID",
    "eventList"
})
public class InvestigationType
    extends InvestigationTypeBase
{

    @XmlElement(name = "AssignedDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String assignedDate;
    @XmlElement(name = "DatePart", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Short datePart;
    @XmlElement(name = "AssignedTime", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String assignedTime;
    @XmlElement(name = "DescriptiveText", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String descriptiveText;
    @XmlElement(name = "OriginalAuthor", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected AuthorType originalAuthor;
    @XmlElement(name = "AuthorID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType authorID;
    @XmlElement(name = "PolicyID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger policyID;
    @XmlElement(name = "EventList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected EventListType eventList;

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

    /**
     * Gets the value of the eventList property.
     * 
     * @return
     *     possible object is
     *     {@link EventListType }
     *     
     */
    public EventListType getEventList() {
        return eventList;
    }

    /**
     * Sets the value of the eventList property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventListType }
     *     
     */
    public void setEventList(EventListType value) {
        this.eventList = value;
    }

}

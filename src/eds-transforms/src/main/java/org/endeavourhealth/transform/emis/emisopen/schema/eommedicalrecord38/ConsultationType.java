
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ConsultationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConsultationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="AssignedDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
 *         &lt;element name="UserID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="ExternalConsultant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LocationID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="LocationTypeID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="AccompanyingHCPID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="ConsultationType" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
 *         &lt;element name="Duration" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="TravelTime" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="AppointmentSlotID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="DataSource" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="PolicyID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="OriginalAuthor" type="{http://www.e-mis.com/emisopen/MedicalRecord}AuthorType" minOccurs="0"/>
 *         &lt;element name="ElementList" type="{http://www.e-mis.com/emisopen/MedicalRecord}ElementListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConsultationType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "assignedDate",
    "datePart",
    "userID",
    "externalConsultant",
    "locationID",
    "locationTypeID",
    "accompanyingHCPID",
    "consultationType",
    "duration",
    "travelTime",
    "appointmentSlotID",
    "dataSource",
    "policyID",
    "originalAuthor",
    "elementList"
})
public class ConsultationType
    extends IdentType
{

    @XmlElement(name = "AssignedDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String assignedDate;
    @XmlElement(name = "DatePart", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Short datePart;
    @XmlElement(name = "UserID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType userID;
    @XmlElement(name = "ExternalConsultant", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String externalConsultant;
    @XmlElement(name = "LocationID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType locationID;
    @XmlElement(name = "LocationTypeID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType locationTypeID;
    @XmlElement(name = "AccompanyingHCPID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType accompanyingHCPID;
    @XmlElement(name = "ConsultationType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte consultationType;
    @XmlElement(name = "Duration", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger duration;
    @XmlElement(name = "TravelTime", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger travelTime;
    @XmlElement(name = "AppointmentSlotID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger appointmentSlotID;
    @XmlElement(name = "DataSource", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger dataSource;
    @XmlElement(name = "PolicyID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger policyID;
    @XmlElement(name = "OriginalAuthor", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected AuthorType originalAuthor;
    @XmlElement(name = "ElementList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected ElementListType elementList;

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
     * Gets the value of the userID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getUserID() {
        return userID;
    }

    /**
     * Sets the value of the userID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setUserID(IdentType value) {
        this.userID = value;
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
     * Gets the value of the locationID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getLocationID() {
        return locationID;
    }

    /**
     * Sets the value of the locationID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setLocationID(IdentType value) {
        this.locationID = value;
    }

    /**
     * Gets the value of the locationTypeID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getLocationTypeID() {
        return locationTypeID;
    }

    /**
     * Sets the value of the locationTypeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setLocationTypeID(IdentType value) {
        this.locationTypeID = value;
    }

    /**
     * Gets the value of the accompanyingHCPID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getAccompanyingHCPID() {
        return accompanyingHCPID;
    }

    /**
     * Sets the value of the accompanyingHCPID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setAccompanyingHCPID(IdentType value) {
        this.accompanyingHCPID = value;
    }

    /**
     * Gets the value of the consultationType property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getConsultationType() {
        return consultationType;
    }

    /**
     * Sets the value of the consultationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setConsultationType(Byte value) {
        this.consultationType = value;
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
     * Gets the value of the travelTime property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTravelTime() {
        return travelTime;
    }

    /**
     * Sets the value of the travelTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTravelTime(BigInteger value) {
        this.travelTime = value;
    }

    /**
     * Gets the value of the appointmentSlotID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAppointmentSlotID() {
        return appointmentSlotID;
    }

    /**
     * Sets the value of the appointmentSlotID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAppointmentSlotID(BigInteger value) {
        this.appointmentSlotID = value;
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
     * Gets the value of the elementList property.
     * 
     * @return
     *     possible object is
     *     {@link ElementListType }
     *     
     */
    public ElementListType getElementList() {
        return elementList;
    }

    /**
     * Sets the value of the elementList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElementListType }
     *     
     */
    public void setElementList(ElementListType value) {
        this.elementList = value;
    }

}


package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ReferralType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReferralType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}CodedItemBaseType">
 *       &lt;sequence>
 *         &lt;element name="Consultant" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="Provider" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="Speciality" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="RequestType" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *               &lt;enumeration value="3"/>
 *               &lt;enumeration value="4"/>
 *               &lt;enumeration value="5"/>
 *               &lt;enumeration value="6"/>
 *               &lt;enumeration value="7"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Team" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="ReferralReason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Community" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="0"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Urgency" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *               &lt;enumeration value="3"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="NHS" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Transport" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ReferralRef" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SourceType" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="Direction" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="SourceLocation" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="DatedReferral" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Rejected" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
 *         &lt;element name="RejectionReason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Accepted" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
 *         &lt;element name="Assessed" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
 *         &lt;element name="ReasonTerm" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *         &lt;element name="SourceDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SourceSpeciality" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="ReferralMode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferralType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "consultant",
    "provider",
    "speciality",
    "requestType",
    "team",
    "referralReason",
    "community",
    "urgency",
    "nhs",
    "transport",
    "referralRef",
    "sourceType",
    "direction",
    "sourceLocation",
    "datedReferral",
    "rejected",
    "rejectionReason",
    "accepted",
    "assessed",
    "reasonTerm",
    "sourceDescription",
    "sourceSpeciality",
    "referralMode"
})
public class ReferralType
    extends CodedItemBaseType
{

    @XmlElement(name = "Consultant", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType consultant;
    @XmlElement(name = "Provider", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType provider;
    @XmlElement(name = "Speciality", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType speciality;
    @XmlElement(name = "RequestType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger requestType;
    @XmlElement(name = "Team", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType team;
    @XmlElement(name = "ReferralReason", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String referralReason;
    @XmlElement(name = "Community", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger community;
    @XmlElement(name = "Urgency", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger urgency;
    @XmlElement(name = "NHS", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger nhs;
    @XmlElement(name = "Transport", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger transport;
    @XmlElement(name = "ReferralRef", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String referralRef;
    @XmlElement(name = "SourceType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType sourceType;
    @XmlElement(name = "Direction", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger direction;
    @XmlElement(name = "SourceLocation", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType sourceLocation;
    @XmlElement(name = "DatedReferral", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar datedReferral;
    @XmlElement(name = "Rejected", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte rejected;
    @XmlElement(name = "RejectionReason", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String rejectionReason;
    @XmlElement(name = "Accepted", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte accepted;
    @XmlElement(name = "Assessed", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte assessed;
    @XmlElement(name = "ReasonTerm", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType reasonTerm;
    @XmlElement(name = "SourceDescription", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String sourceDescription;
    @XmlElement(name = "SourceSpeciality", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType sourceSpeciality;
    @XmlElement(name = "ReferralMode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String referralMode;

    /**
     * Gets the value of the consultant property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getConsultant() {
        return consultant;
    }

    /**
     * Sets the value of the consultant property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setConsultant(IdentType value) {
        this.consultant = value;
    }

    /**
     * Gets the value of the provider property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getProvider() {
        return provider;
    }

    /**
     * Sets the value of the provider property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setProvider(IdentType value) {
        this.provider = value;
    }

    /**
     * Gets the value of the speciality property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getSpeciality() {
        return speciality;
    }

    /**
     * Sets the value of the speciality property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setSpeciality(StringCodeType value) {
        this.speciality = value;
    }

    /**
     * Gets the value of the requestType property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRequestType() {
        return requestType;
    }

    /**
     * Sets the value of the requestType property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRequestType(BigInteger value) {
        this.requestType = value;
    }

    /**
     * Gets the value of the team property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getTeam() {
        return team;
    }

    /**
     * Sets the value of the team property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setTeam(IdentType value) {
        this.team = value;
    }

    /**
     * Gets the value of the referralReason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReferralReason() {
        return referralReason;
    }

    /**
     * Sets the value of the referralReason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReferralReason(String value) {
        this.referralReason = value;
    }

    /**
     * Gets the value of the community property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCommunity() {
        return community;
    }

    /**
     * Sets the value of the community property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCommunity(BigInteger value) {
        this.community = value;
    }

    /**
     * Gets the value of the urgency property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getUrgency() {
        return urgency;
    }

    /**
     * Sets the value of the urgency property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setUrgency(BigInteger value) {
        this.urgency = value;
    }

    /**
     * Gets the value of the nhs property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNHS() {
        return nhs;
    }

    /**
     * Sets the value of the nhs property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNHS(BigInteger value) {
        this.nhs = value;
    }

    /**
     * Gets the value of the transport property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTransport() {
        return transport;
    }

    /**
     * Sets the value of the transport property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTransport(BigInteger value) {
        this.transport = value;
    }

    /**
     * Gets the value of the referralRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReferralRef() {
        return referralRef;
    }

    /**
     * Sets the value of the referralRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReferralRef(String value) {
        this.referralRef = value;
    }

    /**
     * Gets the value of the sourceType property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getSourceType() {
        return sourceType;
    }

    /**
     * Sets the value of the sourceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setSourceType(IdentType value) {
        this.sourceType = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDirection(BigInteger value) {
        this.direction = value;
    }

    /**
     * Gets the value of the sourceLocation property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getSourceLocation() {
        return sourceLocation;
    }

    /**
     * Sets the value of the sourceLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setSourceLocation(IdentType value) {
        this.sourceLocation = value;
    }

    /**
     * Gets the value of the datedReferral property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDatedReferral() {
        return datedReferral;
    }

    /**
     * Sets the value of the datedReferral property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDatedReferral(XMLGregorianCalendar value) {
        this.datedReferral = value;
    }

    /**
     * Gets the value of the rejected property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getRejected() {
        return rejected;
    }

    /**
     * Sets the value of the rejected property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setRejected(Byte value) {
        this.rejected = value;
    }

    /**
     * Gets the value of the rejectionReason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRejectionReason() {
        return rejectionReason;
    }

    /**
     * Sets the value of the rejectionReason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRejectionReason(String value) {
        this.rejectionReason = value;
    }

    /**
     * Gets the value of the accepted property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getAccepted() {
        return accepted;
    }

    /**
     * Sets the value of the accepted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setAccepted(Byte value) {
        this.accepted = value;
    }

    /**
     * Gets the value of the assessed property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getAssessed() {
        return assessed;
    }

    /**
     * Sets the value of the assessed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setAssessed(Byte value) {
        this.assessed = value;
    }

    /**
     * Gets the value of the reasonTerm property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getReasonTerm() {
        return reasonTerm;
    }

    /**
     * Sets the value of the reasonTerm property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setReasonTerm(StringCodeType value) {
        this.reasonTerm = value;
    }

    /**
     * Gets the value of the sourceDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceDescription() {
        return sourceDescription;
    }

    /**
     * Sets the value of the sourceDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceDescription(String value) {
        this.sourceDescription = value;
    }

    /**
     * Gets the value of the sourceSpeciality property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getSourceSpeciality() {
        return sourceSpeciality;
    }

    /**
     * Sets the value of the sourceSpeciality property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setSourceSpeciality(IdentType value) {
        this.sourceSpeciality = value;
    }

    /**
     * Gets the value of the referralMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReferralMode() {
        return referralMode;
    }

    /**
     * Sets the value of the referralMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReferralMode(String value) {
        this.referralMode = value;
    }

}


package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.Referral complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Referral">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="speciality" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.e-mis.com/emisopen}dt.Code">
 *                 &lt;attribute name="nationalCode" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                 &lt;attribute name="specialityAbbreviation">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;maxLength value="10"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="targetUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="targetOrganisation" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="requestType" type="{http://www.e-mis.com/emisopen}voc.ReferralRequestType" minOccurs="0"/>
 *         &lt;element name="reason" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="community" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="urgency" type="{http://www.e-mis.com/emisopen}voc.ReferralUrgency" minOccurs="0"/>
 *         &lt;element name="NHS" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="transport" type="{http://www.e-mis.com/emisopen}voc.ReferralTransport" minOccurs="0"/>
 *         &lt;element name="referralRef" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="direction" type="{http://www.e-mis.com/emisopen}voc.ReferralDirection" minOccurs="0"/>
 *         &lt;element name="sourceOrganisation" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="datedReferral" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="rejectedReason" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="200"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="referralMode" type="{http://www.e-mis.com/emisopen}voc.ReferralMode" minOccurs="0"/>
 *         &lt;element name="UBRN" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="referralActivity" type="{http://www.e-mis.com/emisopen}dt.uid" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Referral", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "speciality",
    "targetUserInRole",
    "targetOrganisation",
    "requestType",
    "reason",
    "community",
    "urgency",
    "nhs",
    "transport",
    "referralRef",
    "direction",
    "sourceOrganisation",
    "datedReferral",
    "rejectedReason",
    "referralMode",
    "ubrn",
    "referralActivity"
})
public class OpenHR001Referral {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Referral.Speciality speciality;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String targetUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String targetOrganisation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocReferralRequestType requestType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String reason;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean community;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocReferralUrgency urgency;
    @XmlElement(name = "NHS", namespace = "http://www.e-mis.com/emisopen")
    protected Boolean nhs;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocReferralTransport transport;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String referralRef;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocReferralDirection direction;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String sourceOrganisation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar datedReferral;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String rejectedReason;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocReferralMode referralMode;
    @XmlElement(name = "UBRN", namespace = "http://www.e-mis.com/emisopen")
    protected String ubrn;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> referralActivity;

    /**
     * Gets the value of the speciality property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Referral.Speciality }
     *     
     */
    public OpenHR001Referral.Speciality getSpeciality() {
        return speciality;
    }

    /**
     * Sets the value of the speciality property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Referral.Speciality }
     *     
     */
    public void setSpeciality(OpenHR001Referral.Speciality value) {
        this.speciality = value;
    }

    /**
     * Gets the value of the targetUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetUserInRole() {
        return targetUserInRole;
    }

    /**
     * Sets the value of the targetUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetUserInRole(String value) {
        this.targetUserInRole = value;
    }

    /**
     * Gets the value of the targetOrganisation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetOrganisation() {
        return targetOrganisation;
    }

    /**
     * Sets the value of the targetOrganisation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetOrganisation(String value) {
        this.targetOrganisation = value;
    }

    /**
     * Gets the value of the requestType property.
     * 
     * @return
     *     possible object is
     *     {@link VocReferralRequestType }
     *     
     */
    public VocReferralRequestType getRequestType() {
        return requestType;
    }

    /**
     * Sets the value of the requestType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocReferralRequestType }
     *     
     */
    public void setRequestType(VocReferralRequestType value) {
        this.requestType = value;
    }

    /**
     * Gets the value of the reason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReason(String value) {
        this.reason = value;
    }

    /**
     * Gets the value of the community property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCommunity() {
        return community;
    }

    /**
     * Sets the value of the community property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCommunity(Boolean value) {
        this.community = value;
    }

    /**
     * Gets the value of the urgency property.
     * 
     * @return
     *     possible object is
     *     {@link VocReferralUrgency }
     *     
     */
    public VocReferralUrgency getUrgency() {
        return urgency;
    }

    /**
     * Sets the value of the urgency property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocReferralUrgency }
     *     
     */
    public void setUrgency(VocReferralUrgency value) {
        this.urgency = value;
    }

    /**
     * Gets the value of the nhs property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNHS() {
        return nhs;
    }

    /**
     * Sets the value of the nhs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNHS(Boolean value) {
        this.nhs = value;
    }

    /**
     * Gets the value of the transport property.
     * 
     * @return
     *     possible object is
     *     {@link VocReferralTransport }
     *     
     */
    public VocReferralTransport getTransport() {
        return transport;
    }

    /**
     * Sets the value of the transport property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocReferralTransport }
     *     
     */
    public void setTransport(VocReferralTransport value) {
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
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link VocReferralDirection }
     *     
     */
    public VocReferralDirection getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocReferralDirection }
     *     
     */
    public void setDirection(VocReferralDirection value) {
        this.direction = value;
    }

    /**
     * Gets the value of the sourceOrganisation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceOrganisation() {
        return sourceOrganisation;
    }

    /**
     * Sets the value of the sourceOrganisation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceOrganisation(String value) {
        this.sourceOrganisation = value;
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
     * Gets the value of the rejectedReason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRejectedReason() {
        return rejectedReason;
    }

    /**
     * Sets the value of the rejectedReason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRejectedReason(String value) {
        this.rejectedReason = value;
    }

    /**
     * Gets the value of the referralMode property.
     * 
     * @return
     *     possible object is
     *     {@link VocReferralMode }
     *     
     */
    public VocReferralMode getReferralMode() {
        return referralMode;
    }

    /**
     * Sets the value of the referralMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocReferralMode }
     *     
     */
    public void setReferralMode(VocReferralMode value) {
        this.referralMode = value;
    }

    /**
     * Gets the value of the ubrn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUBRN() {
        return ubrn;
    }

    /**
     * Sets the value of the ubrn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUBRN(String value) {
        this.ubrn = value;
    }

    /**
     * Gets the value of the referralActivity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referralActivity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferralActivity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getReferralActivity() {
        if (referralActivity == null) {
            referralActivity = new ArrayList<String>();
        }
        return this.referralActivity;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.Code">
     *       &lt;attribute name="nationalCode" type="{http://www.w3.org/2001/XMLSchema}int" />
     *       &lt;attribute name="specialityAbbreviation">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;maxLength value="10"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Speciality
        extends DtCode
    {

        @XmlAttribute(name = "nationalCode")
        protected Integer nationalCode;
        @XmlAttribute(name = "specialityAbbreviation")
        protected String specialityAbbreviation;

        /**
         * Gets the value of the nationalCode property.
         * 
         * @return
         *     possible object is
         *     {@link Integer }
         *     
         */
        public Integer getNationalCode() {
            return nationalCode;
        }

        /**
         * Sets the value of the nationalCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link Integer }
         *     
         */
        public void setNationalCode(Integer value) {
            this.nationalCode = value;
        }

        /**
         * Gets the value of the specialityAbbreviation property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSpecialityAbbreviation() {
            return specialityAbbreviation;
        }

        /**
         * Sets the value of the specialityAbbreviation property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSpecialityAbbreviation(String value) {
            this.specialityAbbreviation = value;
        }

    }

}

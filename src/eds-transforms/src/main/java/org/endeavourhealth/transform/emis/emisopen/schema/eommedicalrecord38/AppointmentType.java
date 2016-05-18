
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AppointmentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AppointmentType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="SessionHolder" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="LocationID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="AssignedDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Time" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Site">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *                   &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="SessionID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="Reason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Duration" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="RUB" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AppointmentType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "sessionHolder",
    "locationID",
    "assignedDate",
    "time",
    "site",
    "sessionID",
    "reason",
    "duration",
    "rub"
})
public class AppointmentType
    extends IdentType
{

    @XmlElement(name = "SessionHolder", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType sessionHolder;
    @XmlElement(name = "LocationID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType locationID;
    @XmlElement(name = "AssignedDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String assignedDate;
    @XmlElement(name = "Time", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String time;
    @XmlElement(name = "Site", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected AppointmentType.Site site;
    @XmlElement(name = "SessionID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger sessionID;
    @XmlElement(name = "Reason", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String reason;
    @XmlElement(name = "Duration", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Float duration;
    @XmlElement(name = "RUB", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String rub;

    /**
     * Gets the value of the sessionHolder property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getSessionHolder() {
        return sessionHolder;
    }

    /**
     * Sets the value of the sessionHolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setSessionHolder(IdentType value) {
        this.sessionHolder = value;
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
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTime(String value) {
        this.time = value;
    }

    /**
     * Gets the value of the site property.
     * 
     * @return
     *     possible object is
     *     {@link AppointmentType.Site }
     *     
     */
    public AppointmentType.Site getSite() {
        return site;
    }

    /**
     * Sets the value of the site property.
     * 
     * @param value
     *     allowed object is
     *     {@link AppointmentType.Site }
     *     
     */
    public void setSite(AppointmentType.Site value) {
        this.site = value;
    }

    /**
     * Gets the value of the sessionID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSessionID() {
        return sessionID;
    }

    /**
     * Sets the value of the sessionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSessionID(BigInteger value) {
        this.sessionID = value;
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
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setDuration(Float value) {
        this.duration = value;
    }

    /**
     * Gets the value of the rub property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRUB() {
        return rub;
    }

    /**
     * Sets the value of the rub property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRUB(String value) {
        this.rub = value;
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
     *         &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
     *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
        "id",
        "name"
    })
    public static class Site {

        @XmlElement(name = "ID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected BigInteger id;
        @XmlElement(name = "Name", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String name;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getID() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setID(BigInteger value) {
            this.id = value;
        }

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }

    }

}

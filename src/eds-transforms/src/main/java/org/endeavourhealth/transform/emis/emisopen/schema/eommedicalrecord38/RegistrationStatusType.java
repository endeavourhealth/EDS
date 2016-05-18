
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegistrationStatusType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegistrationStatusType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CurrentStatus" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}StatusType">
 *                 &lt;sequence>
 *                   &lt;element name="PracticeRegistered" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *                   &lt;element name="HARegistered" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="StatusHistoryList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="StatusHistory" type="{http://www.e-mis.com/emisopen/MedicalRecord}StatusType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="RegistrationHistoryList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="RegistrationHistory" type="{http://www.e-mis.com/emisopen/MedicalRecord}RegistrationHistoryType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
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
@XmlType(name = "RegistrationStatusType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "currentStatus",
    "statusHistoryList",
    "registrationHistoryList"
})
public class RegistrationStatusType {

    @XmlElement(name = "CurrentStatus", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected RegistrationStatusType.CurrentStatus currentStatus;
    @XmlElement(name = "StatusHistoryList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected RegistrationStatusType.StatusHistoryList statusHistoryList;
    @XmlElement(name = "RegistrationHistoryList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected RegistrationStatusType.RegistrationHistoryList registrationHistoryList;

    /**
     * Gets the value of the currentStatus property.
     * 
     * @return
     *     possible object is
     *     {@link RegistrationStatusType.CurrentStatus }
     *     
     */
    public RegistrationStatusType.CurrentStatus getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Sets the value of the currentStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistrationStatusType.CurrentStatus }
     *     
     */
    public void setCurrentStatus(RegistrationStatusType.CurrentStatus value) {
        this.currentStatus = value;
    }

    /**
     * Gets the value of the statusHistoryList property.
     * 
     * @return
     *     possible object is
     *     {@link RegistrationStatusType.StatusHistoryList }
     *     
     */
    public RegistrationStatusType.StatusHistoryList getStatusHistoryList() {
        return statusHistoryList;
    }

    /**
     * Sets the value of the statusHistoryList property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistrationStatusType.StatusHistoryList }
     *     
     */
    public void setStatusHistoryList(RegistrationStatusType.StatusHistoryList value) {
        this.statusHistoryList = value;
    }

    /**
     * Gets the value of the registrationHistoryList property.
     * 
     * @return
     *     possible object is
     *     {@link RegistrationStatusType.RegistrationHistoryList }
     *     
     */
    public RegistrationStatusType.RegistrationHistoryList getRegistrationHistoryList() {
        return registrationHistoryList;
    }

    /**
     * Sets the value of the registrationHistoryList property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistrationStatusType.RegistrationHistoryList }
     *     
     */
    public void setRegistrationHistoryList(RegistrationStatusType.RegistrationHistoryList value) {
        this.registrationHistoryList = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}StatusType">
     *       &lt;sequence>
     *         &lt;element name="PracticeRegistered" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
     *         &lt;element name="HARegistered" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "practiceRegistered",
        "haRegistered"
    })
    public static class CurrentStatus
        extends StatusType
    {

        @XmlElement(name = "PracticeRegistered", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected BigInteger practiceRegistered;
        @XmlElement(name = "HARegistered", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected BigInteger haRegistered;

        /**
         * Gets the value of the practiceRegistered property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getPracticeRegistered() {
            return practiceRegistered;
        }

        /**
         * Sets the value of the practiceRegistered property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setPracticeRegistered(BigInteger value) {
            this.practiceRegistered = value;
        }

        /**
         * Gets the value of the haRegistered property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getHARegistered() {
            return haRegistered;
        }

        /**
         * Sets the value of the haRegistered property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setHARegistered(BigInteger value) {
            this.haRegistered = value;
        }

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
     *         &lt;element name="RegistrationHistory" type="{http://www.e-mis.com/emisopen/MedicalRecord}RegistrationHistoryType" maxOccurs="unbounded" minOccurs="0"/>
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
        "registrationHistory"
    })
    public static class RegistrationHistoryList {

        @XmlElement(name = "RegistrationHistory", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected List<RegistrationHistoryType> registrationHistory;

        /**
         * Gets the value of the registrationHistory property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the registrationHistory property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRegistrationHistory().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link RegistrationHistoryType }
         * 
         * 
         */
        public List<RegistrationHistoryType> getRegistrationHistory() {
            if (registrationHistory == null) {
                registrationHistory = new ArrayList<RegistrationHistoryType>();
            }
            return this.registrationHistory;
        }

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
     *         &lt;element name="StatusHistory" type="{http://www.e-mis.com/emisopen/MedicalRecord}StatusType" maxOccurs="unbounded" minOccurs="0"/>
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
        "statusHistory"
    })
    public static class StatusHistoryList {

        @XmlElement(name = "StatusHistory", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected List<StatusType> statusHistory;

        /**
         * Gets the value of the statusHistory property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the statusHistory property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getStatusHistory().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link StatusType }
         * 
         * 
         */
        public List<StatusType> getStatusHistory() {
            if (statusHistory == null) {
                statusHistory = new ArrayList<StatusType>();
            }
            return this.statusHistory;
        }

    }

}

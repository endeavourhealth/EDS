
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Any physical location such as a provider (hospital) or trading partner (HA) etc
 * 
 * <p>Java class for LocationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LocationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="LocationName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NationalCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LocationTypeID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="Address" type="{http://www.e-mis.com/emisopen/MedicalRecord}AddressType" minOccurs="0"/>
 *         &lt;element name="Telephone1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Telephone2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Fax" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Contact" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Cypher" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Interchange" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="LinkCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SeniorPartner" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PCGCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PCTCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="GPLinkCodeList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="GPLinkCode" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *                           &lt;sequence>
 *                             &lt;element name="LinkCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                           &lt;/sequence>
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocationType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "locationName",
    "nationalCode",
    "locationTypeID",
    "address",
    "telephone1",
    "telephone2",
    "fax",
    "email",
    "contact",
    "cypher",
    "interchange",
    "linkCode",
    "seniorPartner",
    "pcgCode",
    "pctCode",
    "gpLinkCodeList"
})
public class LocationType
    extends IdentType
{

    @XmlElement(name = "LocationName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String locationName;
    @XmlElement(name = "NationalCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String nationalCode;
    @XmlElement(name = "LocationTypeID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType locationTypeID;
    @XmlElement(name = "Address", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected AddressType address;
    @XmlElement(name = "Telephone1", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String telephone1;
    @XmlElement(name = "Telephone2", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String telephone2;
    @XmlElement(name = "Fax", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String fax;
    @XmlElement(name = "Email", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String email;
    @XmlElement(name = "Contact", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String contact;
    @XmlElement(name = "Cypher", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String cypher;
    @XmlElement(name = "Interchange", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger interchange;
    @XmlElement(name = "LinkCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String linkCode;
    @XmlElement(name = "SeniorPartner", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String seniorPartner;
    @XmlElement(name = "PCGCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String pcgCode;
    @XmlElement(name = "PCTCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String pctCode;
    @XmlElement(name = "GPLinkCodeList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected LocationType.GPLinkCodeList gpLinkCodeList;

    /**
     * Gets the value of the locationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * Sets the value of the locationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocationName(String value) {
        this.locationName = value;
    }

    /**
     * Gets the value of the nationalCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNationalCode() {
        return nationalCode;
    }

    /**
     * Sets the value of the nationalCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNationalCode(String value) {
        this.nationalCode = value;
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
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link AddressType }
     *     
     */
    public AddressType getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressType }
     *     
     */
    public void setAddress(AddressType value) {
        this.address = value;
    }

    /**
     * Gets the value of the telephone1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTelephone1() {
        return telephone1;
    }

    /**
     * Sets the value of the telephone1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTelephone1(String value) {
        this.telephone1 = value;
    }

    /**
     * Gets the value of the telephone2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTelephone2() {
        return telephone2;
    }

    /**
     * Sets the value of the telephone2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTelephone2(String value) {
        this.telephone2 = value;
    }

    /**
     * Gets the value of the fax property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFax() {
        return fax;
    }

    /**
     * Sets the value of the fax property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFax(String value) {
        this.fax = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the contact property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContact() {
        return contact;
    }

    /**
     * Sets the value of the contact property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContact(String value) {
        this.contact = value;
    }

    /**
     * Gets the value of the cypher property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCypher() {
        return cypher;
    }

    /**
     * Sets the value of the cypher property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCypher(String value) {
        this.cypher = value;
    }

    /**
     * Gets the value of the interchange property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getInterchange() {
        return interchange;
    }

    /**
     * Sets the value of the interchange property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setInterchange(BigInteger value) {
        this.interchange = value;
    }

    /**
     * Gets the value of the linkCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkCode() {
        return linkCode;
    }

    /**
     * Sets the value of the linkCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkCode(String value) {
        this.linkCode = value;
    }

    /**
     * Gets the value of the seniorPartner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSeniorPartner() {
        return seniorPartner;
    }

    /**
     * Sets the value of the seniorPartner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSeniorPartner(String value) {
        this.seniorPartner = value;
    }

    /**
     * Gets the value of the pcgCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPCGCode() {
        return pcgCode;
    }

    /**
     * Sets the value of the pcgCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPCGCode(String value) {
        this.pcgCode = value;
    }

    /**
     * Gets the value of the pctCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPCTCode() {
        return pctCode;
    }

    /**
     * Sets the value of the pctCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPCTCode(String value) {
        this.pctCode = value;
    }

    /**
     * Gets the value of the gpLinkCodeList property.
     * 
     * @return
     *     possible object is
     *     {@link LocationType.GPLinkCodeList }
     *     
     */
    public LocationType.GPLinkCodeList getGPLinkCodeList() {
        return gpLinkCodeList;
    }

    /**
     * Sets the value of the gpLinkCodeList property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationType.GPLinkCodeList }
     *     
     */
    public void setGPLinkCodeList(LocationType.GPLinkCodeList value) {
        this.gpLinkCodeList = value;
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
     *         &lt;element name="GPLinkCode" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
     *                 &lt;sequence>
     *                   &lt;element name="LinkCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                 &lt;/sequence>
     *               &lt;/extension>
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
    @XmlType(name = "", propOrder = {
        "gpLinkCode"
    })
    public static class GPLinkCodeList {

        @XmlElement(name = "GPLinkCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected List<LocationType.GPLinkCodeList.GPLinkCode> gpLinkCode;

        /**
         * Gets the value of the gpLinkCode property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the gpLinkCode property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getGPLinkCode().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link LocationType.GPLinkCodeList.GPLinkCode }
         * 
         * 
         */
        public List<LocationType.GPLinkCodeList.GPLinkCode> getGPLinkCode() {
            if (gpLinkCode == null) {
                gpLinkCode = new ArrayList<LocationType.GPLinkCodeList.GPLinkCode>();
            }
            return this.gpLinkCode;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
         *       &lt;sequence>
         *         &lt;element name="LinkCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
            "linkCode"
        })
        public static class GPLinkCode
            extends IdentType
        {

            @XmlElement(name = "LinkCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
            protected String linkCode;

            /**
             * Gets the value of the linkCode property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getLinkCode() {
                return linkCode;
            }

            /**
             * Sets the value of the linkCode property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setLinkCode(String value) {
                this.linkCode = value;
            }

        }

    }

}

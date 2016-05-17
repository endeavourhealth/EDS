
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for dt.Address complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.Address">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="addressType" type="{http://www.e-mis.com/emisopen}voc.AddressType" minOccurs="0"/>
 *         &lt;element name="addressKey" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="postcodeData" type="{http://www.e-mis.com/emisopen}dt.PostcodeData" minOccurs="0"/>
 *         &lt;element name="postCode">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="8"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="houseNameFlat">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="35"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="street">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="35"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="village">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="35"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="town">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="35"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="county">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="35"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
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
@XmlType(name = "dt.Address", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "addressType",
    "addressKey",
    "postcodeData",
    "postCode",
    "houseNameFlat",
    "street",
    "village",
    "town",
    "county"
})
@XmlSeeAlso({
    OpenHR001Person.Address.class
})
public class DtAddress {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocAddressType addressType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String addressKey;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtPostcodeData postcodeData;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String postCode;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String houseNameFlat;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String street;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String village;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String town;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String county;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the addressType property.
     * 
     * @return
     *     possible object is
     *     {@link VocAddressType }
     *     
     */
    public VocAddressType getAddressType() {
        return addressType;
    }

    /**
     * Sets the value of the addressType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocAddressType }
     *     
     */
    public void setAddressType(VocAddressType value) {
        this.addressType = value;
    }

    /**
     * Gets the value of the addressKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddressKey() {
        return addressKey;
    }

    /**
     * Sets the value of the addressKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddressKey(String value) {
        this.addressKey = value;
    }

    /**
     * Gets the value of the postcodeData property.
     * 
     * @return
     *     possible object is
     *     {@link DtPostcodeData }
     *     
     */
    public DtPostcodeData getPostcodeData() {
        return postcodeData;
    }

    /**
     * Sets the value of the postcodeData property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtPostcodeData }
     *     
     */
    public void setPostcodeData(DtPostcodeData value) {
        this.postcodeData = value;
    }

    /**
     * Gets the value of the postCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostCode() {
        return postCode;
    }

    /**
     * Sets the value of the postCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostCode(String value) {
        this.postCode = value;
    }

    /**
     * Gets the value of the houseNameFlat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHouseNameFlat() {
        return houseNameFlat;
    }

    /**
     * Sets the value of the houseNameFlat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHouseNameFlat(String value) {
        this.houseNameFlat = value;
    }

    /**
     * Gets the value of the street property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the value of the street property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStreet(String value) {
        this.street = value;
    }

    /**
     * Gets the value of the village property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVillage() {
        return village;
    }

    /**
     * Sets the value of the village property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVillage(String value) {
        this.village = value;
    }

    /**
     * Gets the value of the town property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTown() {
        return town;
    }

    /**
     * Sets the value of the town property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTown(String value) {
        this.town = value;
    }

    /**
     * Gets the value of the county property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCounty() {
        return county;
    }

    /**
     * Sets the value of the county property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCounty(String value) {
        this.county = value;
    }

}

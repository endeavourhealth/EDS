
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Address complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Address">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="HouseName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HouseNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Road" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Locality" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Town" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="County" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Postcode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Address", propOrder = {
    "houseName",
    "houseNumber",
    "road",
    "locality",
    "town",
    "county",
    "postcode"
})
public class Address {

    @XmlElement(name = "HouseName")
    protected String houseName;
    @XmlElement(name = "HouseNumber")
    protected String houseNumber;
    @XmlElement(name = "Road", required = true)
    protected String road;
    @XmlElement(name = "Locality")
    protected String locality;
    @XmlElement(name = "Town", required = true)
    protected String town;
    @XmlElement(name = "County")
    protected String county;
    @XmlElement(name = "Postcode", required = true)
    protected String postcode;

    /**
     * Gets the value of the houseName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHouseName() {
        return houseName;
    }

    /**
     * Sets the value of the houseName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHouseName(String value) {
        this.houseName = value;
    }

    /**
     * Gets the value of the houseNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * Sets the value of the houseNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHouseNumber(String value) {
        this.houseNumber = value;
    }

    /**
     * Gets the value of the road property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoad() {
        return road;
    }

    /**
     * Sets the value of the road property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoad(String value) {
        this.road = value;
    }

    /**
     * Gets the value of the locality property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocality() {
        return locality;
    }

    /**
     * Sets the value of the locality property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocality(String value) {
        this.locality = value;
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

    /**
     * Gets the value of the postcode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets the value of the postcode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostcode(String value) {
        this.postcode = value;
    }

}

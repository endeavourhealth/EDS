
package org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SlotsStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SlotsStruct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TypeID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Total" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Booked" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Blocked" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Embargoed" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Available" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SlotsStruct", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "typeID",
    "description",
    "total",
    "booked",
    "blocked",
    "embargoed",
    "available"
})
public class SlotsStruct {

    @XmlElement(name = "TypeID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String typeID;
    @XmlElement(name = "Description", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String description;
    @XmlElement(name = "Total", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected int total;
    @XmlElement(name = "Booked", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected int booked;
    @XmlElement(name = "Blocked", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected int blocked;
    @XmlElement(name = "Embargoed", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected int embargoed;
    @XmlElement(name = "Available", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected int available;

    /**
     * Gets the value of the typeID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeID() {
        return typeID;
    }

    /**
     * Sets the value of the typeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeID(String value) {
        this.typeID = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the total property.
     * 
     */
    public int getTotal() {
        return total;
    }

    /**
     * Sets the value of the total property.
     * 
     */
    public void setTotal(int value) {
        this.total = value;
    }

    /**
     * Gets the value of the booked property.
     * 
     */
    public int getBooked() {
        return booked;
    }

    /**
     * Sets the value of the booked property.
     * 
     */
    public void setBooked(int value) {
        this.booked = value;
    }

    /**
     * Gets the value of the blocked property.
     * 
     */
    public int getBlocked() {
        return blocked;
    }

    /**
     * Sets the value of the blocked property.
     * 
     */
    public void setBlocked(int value) {
        this.blocked = value;
    }

    /**
     * Gets the value of the embargoed property.
     * 
     */
    public int getEmbargoed() {
        return embargoed;
    }

    /**
     * Sets the value of the embargoed property.
     * 
     */
    public void setEmbargoed(int value) {
        this.embargoed = value;
    }

    /**
     * Gets the value of the available property.
     * 
     */
    public int getAvailable() {
        return available;
    }

    /**
     * Sets the value of the available property.
     * 
     */
    public void setAvailable(int value) {
        this.available = value;
    }

}

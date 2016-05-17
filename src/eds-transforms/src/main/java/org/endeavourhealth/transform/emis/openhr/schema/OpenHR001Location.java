
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.Location complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Location">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="name" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="locationType" type="{http://www.e-mis.com/emisopen}OpenHR001.LocationType" minOccurs="0"/>
 *         &lt;element name="parentLocation" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="mainContactPerson" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="notes" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="openDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="closeDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="address" type="{http://www.e-mis.com/emisopen}dt.Address" minOccurs="0"/>
 *         &lt;element name="contact" type="{http://www.e-mis.com/emisopen}dt.Contact" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Location", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "name",
    "locationType",
    "parentLocation",
    "mainContactPerson",
    "notes",
    "openDate",
    "closeDate",
    "address",
    "contact"
})
public class OpenHR001Location
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String name;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001LocationType locationType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String parentLocation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String mainContactPerson;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String notes;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar openDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar closeDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtAddress address;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<DtContact> contact;

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

    /**
     * Gets the value of the locationType property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001LocationType }
     *     
     */
    public OpenHR001LocationType getLocationType() {
        return locationType;
    }

    /**
     * Sets the value of the locationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001LocationType }
     *     
     */
    public void setLocationType(OpenHR001LocationType value) {
        this.locationType = value;
    }

    /**
     * Gets the value of the parentLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentLocation() {
        return parentLocation;
    }

    /**
     * Sets the value of the parentLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentLocation(String value) {
        this.parentLocation = value;
    }

    /**
     * Gets the value of the mainContactPerson property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMainContactPerson() {
        return mainContactPerson;
    }

    /**
     * Sets the value of the mainContactPerson property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMainContactPerson(String value) {
        this.mainContactPerson = value;
    }

    /**
     * Gets the value of the notes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotes(String value) {
        this.notes = value;
    }

    /**
     * Gets the value of the openDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOpenDate() {
        return openDate;
    }

    /**
     * Sets the value of the openDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOpenDate(XMLGregorianCalendar value) {
        this.openDate = value;
    }

    /**
     * Gets the value of the closeDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCloseDate() {
        return closeDate;
    }

    /**
     * Sets the value of the closeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCloseDate(XMLGregorianCalendar value) {
        this.closeDate = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link DtAddress }
     *     
     */
    public DtAddress getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtAddress }
     *     
     */
    public void setAddress(DtAddress value) {
        this.address = value;
    }

    /**
     * Gets the value of the contact property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contact property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContact().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DtContact }
     * 
     * 
     */
    public List<DtContact> getContact() {
        if (contact == null) {
            contact = new ArrayList<DtContact>();
        }
        return this.contact;
    }

}

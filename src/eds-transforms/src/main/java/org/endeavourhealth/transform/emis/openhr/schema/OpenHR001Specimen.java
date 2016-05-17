
package org.endeavourhealth.transform.emis.openhr.schema;

import java.math.BigDecimal;
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
 * <p>Java class for OpenHR001.Specimen complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Specimen">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="labSpecimenReference" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="specimenType" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="fastingStatus" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="volume" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="volumeUnits" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="collectionProcedure" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="anatomicalOrigin" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="sampleDateTime" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="collectionStartDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="collectionEndDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="dateTimeReceivedByLab" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="battery" type="{http://www.e-mis.com/emisopen}dt.uid" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="test" type="{http://www.e-mis.com/emisopen}dt.uid" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Specimen", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "labSpecimenReference",
    "specimenType",
    "fastingStatus",
    "volume",
    "volumeUnits",
    "collectionProcedure",
    "anatomicalOrigin",
    "sampleDateTime",
    "collectionStartDate",
    "collectionEndDate",
    "dateTimeReceivedByLab",
    "comment",
    "battery",
    "test"
})
public class OpenHR001Specimen
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String labSpecimenReference;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String specimenType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String fastingStatus;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected BigDecimal volume;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String volumeUnits;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String collectionProcedure;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String anatomicalOrigin;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar sampleDateTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar collectionStartDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar collectionEndDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTimeReceivedByLab;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<String> comment;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> battery;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> test;

    /**
     * Gets the value of the labSpecimenReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabSpecimenReference() {
        return labSpecimenReference;
    }

    /**
     * Sets the value of the labSpecimenReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabSpecimenReference(String value) {
        this.labSpecimenReference = value;
    }

    /**
     * Gets the value of the specimenType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecimenType() {
        return specimenType;
    }

    /**
     * Sets the value of the specimenType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecimenType(String value) {
        this.specimenType = value;
    }

    /**
     * Gets the value of the fastingStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFastingStatus() {
        return fastingStatus;
    }

    /**
     * Sets the value of the fastingStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFastingStatus(String value) {
        this.fastingStatus = value;
    }

    /**
     * Gets the value of the volume property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getVolume() {
        return volume;
    }

    /**
     * Sets the value of the volume property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setVolume(BigDecimal value) {
        this.volume = value;
    }

    /**
     * Gets the value of the volumeUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVolumeUnits() {
        return volumeUnits;
    }

    /**
     * Sets the value of the volumeUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVolumeUnits(String value) {
        this.volumeUnits = value;
    }

    /**
     * Gets the value of the collectionProcedure property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionProcedure() {
        return collectionProcedure;
    }

    /**
     * Sets the value of the collectionProcedure property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionProcedure(String value) {
        this.collectionProcedure = value;
    }

    /**
     * Gets the value of the anatomicalOrigin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnatomicalOrigin() {
        return anatomicalOrigin;
    }

    /**
     * Sets the value of the anatomicalOrigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnatomicalOrigin(String value) {
        this.anatomicalOrigin = value;
    }

    /**
     * Gets the value of the sampleDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSampleDateTime() {
        return sampleDateTime;
    }

    /**
     * Sets the value of the sampleDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSampleDateTime(XMLGregorianCalendar value) {
        this.sampleDateTime = value;
    }

    /**
     * Gets the value of the collectionStartDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCollectionStartDate() {
        return collectionStartDate;
    }

    /**
     * Sets the value of the collectionStartDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCollectionStartDate(XMLGregorianCalendar value) {
        this.collectionStartDate = value;
    }

    /**
     * Gets the value of the collectionEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCollectionEndDate() {
        return collectionEndDate;
    }

    /**
     * Sets the value of the collectionEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCollectionEndDate(XMLGregorianCalendar value) {
        this.collectionEndDate = value;
    }

    /**
     * Gets the value of the dateTimeReceivedByLab property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTimeReceivedByLab() {
        return dateTimeReceivedByLab;
    }

    /**
     * Sets the value of the dateTimeReceivedByLab property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTimeReceivedByLab(XMLGregorianCalendar value) {
        this.dateTimeReceivedByLab = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the comment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getComment() {
        if (comment == null) {
            comment = new ArrayList<String>();
        }
        return this.comment;
    }

    /**
     * Gets the value of the battery property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the battery property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBattery().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBattery() {
        if (battery == null) {
            battery = new ArrayList<String>();
        }
        return this.battery;
    }

    /**
     * Gets the value of the test property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the test property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getTest() {
        if (test == null) {
            test = new ArrayList<String>();
        }
        return this.test;
    }

}

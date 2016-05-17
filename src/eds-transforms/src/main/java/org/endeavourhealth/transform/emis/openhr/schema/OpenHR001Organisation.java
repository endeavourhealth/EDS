
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
 * <p>Java class for OpenHR001.Organisation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Organisation">
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
 *         &lt;element name="organisationType" type="{http://www.e-mis.com/emisopen}dt.Code" minOccurs="0"/>
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
 *         &lt;element name="cdb" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="nationalPracticeCode" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="20"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="mainLocation" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="locations" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *                 &lt;sequence>
 *                   &lt;element name="location" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *                 &lt;/sequence>
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="caseload" type="{http://www.e-mis.com/emisopen}OpenHR001.Caseload" minOccurs="0"/>
 *         &lt;element name="parentOrganisation" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="openDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="closeDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Organisation", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "name",
    "organisationType",
    "speciality",
    "cdb",
    "nationalPracticeCode",
    "mainLocation",
    "locations",
    "caseload",
    "parentOrganisation",
    "openDate",
    "closeDate"
})
public class OpenHR001Organisation
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String name;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtCode organisationType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Organisation.Speciality speciality;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Integer cdb;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String nationalPracticeCode;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String mainLocation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Organisation.Locations> locations;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Caseload caseload;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String parentOrganisation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar openDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar closeDate;

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
     * Gets the value of the organisationType property.
     * 
     * @return
     *     possible object is
     *     {@link DtCode }
     *     
     */
    public DtCode getOrganisationType() {
        return organisationType;
    }

    /**
     * Sets the value of the organisationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCode }
     *     
     */
    public void setOrganisationType(DtCode value) {
        this.organisationType = value;
    }

    /**
     * Gets the value of the speciality property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Organisation.Speciality }
     *     
     */
    public OpenHR001Organisation.Speciality getSpeciality() {
        return speciality;
    }

    /**
     * Sets the value of the speciality property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Organisation.Speciality }
     *     
     */
    public void setSpeciality(OpenHR001Organisation.Speciality value) {
        this.speciality = value;
    }

    /**
     * Gets the value of the cdb property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCdb() {
        return cdb;
    }

    /**
     * Sets the value of the cdb property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCdb(Integer value) {
        this.cdb = value;
    }

    /**
     * Gets the value of the nationalPracticeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNationalPracticeCode() {
        return nationalPracticeCode;
    }

    /**
     * Sets the value of the nationalPracticeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNationalPracticeCode(String value) {
        this.nationalPracticeCode = value;
    }

    /**
     * Gets the value of the mainLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMainLocation() {
        return mainLocation;
    }

    /**
     * Sets the value of the mainLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMainLocation(String value) {
        this.mainLocation = value;
    }

    /**
     * Gets the value of the locations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the locations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Organisation.Locations }
     * 
     * 
     */
    public List<OpenHR001Organisation.Locations> getLocations() {
        if (locations == null) {
            locations = new ArrayList<OpenHR001Organisation.Locations>();
        }
        return this.locations;
    }

    /**
     * Gets the value of the caseload property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Caseload }
     *     
     */
    public OpenHR001Caseload getCaseload() {
        return caseload;
    }

    /**
     * Sets the value of the caseload property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Caseload }
     *     
     */
    public void setCaseload(OpenHR001Caseload value) {
        this.caseload = value;
    }

    /**
     * Gets the value of the parentOrganisation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentOrganisation() {
        return parentOrganisation;
    }

    /**
     * Sets the value of the parentOrganisation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentOrganisation(String value) {
        this.parentOrganisation = value;
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
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
     *       &lt;sequence>
     *         &lt;element name="location" type="{http://www.e-mis.com/emisopen}dt.uid"/>
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
        "location"
    })
    public static class Locations
        extends DtDbo
    {

        @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "token")
        protected String location;

        /**
         * Gets the value of the location property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLocation() {
            return location;
        }

        /**
         * Sets the value of the location property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLocation(String value) {
            this.location = value;
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

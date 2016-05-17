
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
 * <p>Java class for OpenHR001.Person complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Person">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="birthDate" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="sex" type="{http://www.e-mis.com/emisopen}voc.Sex" minOccurs="0"/>
 *         &lt;element name="forenames" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="108"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="callingName" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="35"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="surname" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="birthSurname" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="previousSurname" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="title" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="address" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.e-mis.com/emisopen}dt.Address">
 *                 &lt;attribute name="updateMode" type="{http://www.e-mis.com/emisopen}voc.UpdateMode" default="none" />
 *                 &lt;attribute name="auditDeleteDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" />
 *                 &lt;attribute name="auditDeleteUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" />
 *                 &lt;attribute name="auditDeleteInfo" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="contact" type="{http://www.e-mis.com/emisopen}dt.Contact" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="maritalStatus" type="{http://www.e-mis.com/emisopen}dt.Code" minOccurs="0"/>
 *         &lt;element name="ethnicGroup" type="{http://www.e-mis.com/emisopen}dt.Code" minOccurs="0"/>
 *         &lt;element name="religion" type="{http://www.e-mis.com/emisopen}dt.Code" minOccurs="0"/>
 *         &lt;element name="nationality" type="{http://www.e-mis.com/emisopen}dt.Code" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Person", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "birthDate",
    "sex",
    "forenames",
    "callingName",
    "surname",
    "birthSurname",
    "previousSurname",
    "title",
    "address",
    "contact",
    "maritalStatus",
    "ethnicGroup",
    "religion",
    "nationality"
})
public class OpenHR001Person
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar birthDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocSex sex;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String forenames;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String callingName;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String surname;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String birthSurname;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String previousSurname;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String title;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Person.Address> address;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<DtContact> contact;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtCode maritalStatus;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtCode ethnicGroup;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtCode religion;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtCode nationality;

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
     * Gets the value of the birthDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBirthDate() {
        return birthDate;
    }

    /**
     * Sets the value of the birthDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBirthDate(XMLGregorianCalendar value) {
        this.birthDate = value;
    }

    /**
     * Gets the value of the sex property.
     * 
     * @return
     *     possible object is
     *     {@link VocSex }
     *     
     */
    public VocSex getSex() {
        return sex;
    }

    /**
     * Sets the value of the sex property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocSex }
     *     
     */
    public void setSex(VocSex value) {
        this.sex = value;
    }

    /**
     * Gets the value of the forenames property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getForenames() {
        return forenames;
    }

    /**
     * Sets the value of the forenames property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setForenames(String value) {
        this.forenames = value;
    }

    /**
     * Gets the value of the callingName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallingName() {
        return callingName;
    }

    /**
     * Sets the value of the callingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallingName(String value) {
        this.callingName = value;
    }

    /**
     * Gets the value of the surname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Sets the value of the surname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSurname(String value) {
        this.surname = value;
    }

    /**
     * Gets the value of the birthSurname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBirthSurname() {
        return birthSurname;
    }

    /**
     * Sets the value of the birthSurname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBirthSurname(String value) {
        this.birthSurname = value;
    }

    /**
     * Gets the value of the previousSurname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreviousSurname() {
        return previousSurname;
    }

    /**
     * Sets the value of the previousSurname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreviousSurname(String value) {
        this.previousSurname = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the address property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddress().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Person.Address }
     * 
     * 
     */
    public List<OpenHR001Person.Address> getAddress() {
        if (address == null) {
            address = new ArrayList<OpenHR001Person.Address>();
        }
        return this.address;
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

    /**
     * Gets the value of the maritalStatus property.
     * 
     * @return
     *     possible object is
     *     {@link DtCode }
     *     
     */
    public DtCode getMaritalStatus() {
        return maritalStatus;
    }

    /**
     * Sets the value of the maritalStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCode }
     *     
     */
    public void setMaritalStatus(DtCode value) {
        this.maritalStatus = value;
    }

    /**
     * Gets the value of the ethnicGroup property.
     * 
     * @return
     *     possible object is
     *     {@link DtCode }
     *     
     */
    public DtCode getEthnicGroup() {
        return ethnicGroup;
    }

    /**
     * Sets the value of the ethnicGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCode }
     *     
     */
    public void setEthnicGroup(DtCode value) {
        this.ethnicGroup = value;
    }

    /**
     * Gets the value of the religion property.
     * 
     * @return
     *     possible object is
     *     {@link DtCode }
     *     
     */
    public DtCode getReligion() {
        return religion;
    }

    /**
     * Sets the value of the religion property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCode }
     *     
     */
    public void setReligion(DtCode value) {
        this.religion = value;
    }

    /**
     * Gets the value of the nationality property.
     * 
     * @return
     *     possible object is
     *     {@link DtCode }
     *     
     */
    public DtCode getNationality() {
        return nationality;
    }

    /**
     * Sets the value of the nationality property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCode }
     *     
     */
    public void setNationality(DtCode value) {
        this.nationality = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.Address">
     *       &lt;attribute name="updateMode" type="{http://www.e-mis.com/emisopen}voc.UpdateMode" default="none" />
     *       &lt;attribute name="auditDeleteDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" />
     *       &lt;attribute name="auditDeleteUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" />
     *       &lt;attribute name="auditDeleteInfo" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Address
        extends DtAddress
    {

        @XmlAttribute(name = "updateMode")
        protected VocUpdateMode updateMode;
        @XmlAttribute(name = "auditDeleteDate")
        protected XMLGregorianCalendar auditDeleteDate;
        @XmlAttribute(name = "auditDeleteUserInRole")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String auditDeleteUserInRole;
        @XmlAttribute(name = "auditDeleteInfo")
        protected String auditDeleteInfo;

        /**
         * Gets the value of the updateMode property.
         * 
         * @return
         *     possible object is
         *     {@link VocUpdateMode }
         *     
         */
        public VocUpdateMode getUpdateMode() {
            if (updateMode == null) {
                return VocUpdateMode.NONE;
            } else {
                return updateMode;
            }
        }

        /**
         * Sets the value of the updateMode property.
         * 
         * @param value
         *     allowed object is
         *     {@link VocUpdateMode }
         *     
         */
        public void setUpdateMode(VocUpdateMode value) {
            this.updateMode = value;
        }

        /**
         * Gets the value of the auditDeleteDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getAuditDeleteDate() {
            return auditDeleteDate;
        }

        /**
         * Sets the value of the auditDeleteDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setAuditDeleteDate(XMLGregorianCalendar value) {
            this.auditDeleteDate = value;
        }

        /**
         * Gets the value of the auditDeleteUserInRole property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAuditDeleteUserInRole() {
            return auditDeleteUserInRole;
        }

        /**
         * Sets the value of the auditDeleteUserInRole property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAuditDeleteUserInRole(String value) {
            this.auditDeleteUserInRole = value;
        }

        /**
         * Gets the value of the auditDeleteInfo property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAuditDeleteInfo() {
            return auditDeleteInfo;
        }

        /**
         * Sets the value of the auditDeleteInfo property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAuditDeleteInfo(String value) {
            this.auditDeleteInfo = value;
        }

    }

}

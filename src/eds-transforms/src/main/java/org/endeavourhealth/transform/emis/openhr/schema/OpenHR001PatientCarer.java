
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.PatientCarer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.PatientCarer">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="PersonGUID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ResponsiblePCT" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AuthorisationDate" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="Relationship" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Forenames" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Surname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Sex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DateOfBirth" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="CarerAddress" type="{http://www.e-mis.com/emisopen}dt.Address" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Telephone1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Telephone2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.PatientCarer", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "personGUID",
    "responsiblePCT",
    "authorisationDate",
    "relationship",
    "title",
    "forenames",
    "surname",
    "sex",
    "dateOfBirth",
    "carerAddress",
    "telephone1",
    "telephone2"
})
public class OpenHR001PatientCarer
    extends DtDbo
{

    @XmlElement(name = "PersonGUID", namespace = "http://www.e-mis.com/emisopen")
    protected String personGUID;
    @XmlElement(name = "ResponsiblePCT", namespace = "http://www.e-mis.com/emisopen")
    protected String responsiblePCT;
    @XmlElement(name = "AuthorisationDate", namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar authorisationDate;
    @XmlElement(name = "Relationship", namespace = "http://www.e-mis.com/emisopen")
    protected String relationship;
    @XmlElement(name = "Title", namespace = "http://www.e-mis.com/emisopen")
    protected String title;
    @XmlElement(name = "Forenames", namespace = "http://www.e-mis.com/emisopen")
    protected String forenames;
    @XmlElement(name = "Surname", namespace = "http://www.e-mis.com/emisopen")
    protected String surname;
    @XmlElement(name = "Sex", namespace = "http://www.e-mis.com/emisopen")
    protected String sex;
    @XmlElement(name = "DateOfBirth", namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateOfBirth;
    @XmlElement(name = "CarerAddress", namespace = "http://www.e-mis.com/emisopen")
    protected List<DtAddress> carerAddress;
    @XmlElement(name = "Telephone1", namespace = "http://www.e-mis.com/emisopen")
    protected String telephone1;
    @XmlElement(name = "Telephone2", namespace = "http://www.e-mis.com/emisopen")
    protected String telephone2;

    /**
     * Gets the value of the personGUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPersonGUID() {
        return personGUID;
    }

    /**
     * Sets the value of the personGUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPersonGUID(String value) {
        this.personGUID = value;
    }

    /**
     * Gets the value of the responsiblePCT property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResponsiblePCT() {
        return responsiblePCT;
    }

    /**
     * Sets the value of the responsiblePCT property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResponsiblePCT(String value) {
        this.responsiblePCT = value;
    }

    /**
     * Gets the value of the authorisationDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAuthorisationDate() {
        return authorisationDate;
    }

    /**
     * Sets the value of the authorisationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAuthorisationDate(XMLGregorianCalendar value) {
        this.authorisationDate = value;
    }

    /**
     * Gets the value of the relationship property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelationship() {
        return relationship;
    }

    /**
     * Sets the value of the relationship property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelationship(String value) {
        this.relationship = value;
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
     * Gets the value of the sex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSex() {
        return sex;
    }

    /**
     * Sets the value of the sex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSex(String value) {
        this.sex = value;
    }

    /**
     * Gets the value of the dateOfBirth property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the value of the dateOfBirth property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateOfBirth(XMLGregorianCalendar value) {
        this.dateOfBirth = value;
    }

    /**
     * Gets the value of the carerAddress property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the carerAddress property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCarerAddress().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DtAddress }
     * 
     * 
     */
    public List<DtAddress> getCarerAddress() {
        if (carerAddress == null) {
            carerAddress = new ArrayList<DtAddress>();
        }
        return this.carerAddress;
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

}

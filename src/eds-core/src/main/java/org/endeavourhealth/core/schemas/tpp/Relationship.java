
package org.endeavourhealth.core.schemas.tpp;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Relationship complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Relationship">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FirstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MiddleNames" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Surname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Address" type="{}Address" minOccurs="0"/>
 *         &lt;element name="Type">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Adopted Father"/>
 *               &lt;enumeration value="Adopted Mother"/>
 *               &lt;enumeration value="Advisor (Day)"/>
 *               &lt;enumeration value="Advisor (Night)"/>
 *               &lt;enumeration value="Audiologist"/>
 *               &lt;enumeration value="Aunt"/>
 *               &lt;enumeration value="Boyfriend"/>
 *               &lt;enumeration value="Brother"/>
 *               &lt;enumeration value="Brother-in-law"/>
 *               &lt;enumeration value="CAB Adviser"/>
 *               &lt;enumeration value="CAF Lead Professional"/>
 *               &lt;enumeration value="Care Co-ordinator"/>
 *               &lt;enumeration value="Care Lead"/>
 *               &lt;enumeration value="Care Provider"/>
 *               &lt;enumeration value="Carer"/>
 *               &lt;enumeration value="Case Manager"/>
 *               &lt;enumeration value="Chaplain"/>
 *               &lt;enumeration value="Child"/>
 *               &lt;enumeration value="Child Protection/CIN Supervisor"/>
 *               &lt;enumeration value="Chiropodist"/>
 *               &lt;enumeration value="Civil Partner"/>
 *               &lt;enumeration value="Clinical Coordinator"/>
 *               &lt;enumeration value="Clinical Nurse Specialist"/>
 *               &lt;enumeration value="Clinical Psychologist"/>
 *               &lt;enumeration value="Community Matron"/>
 *               &lt;enumeration value="Community Midwife"/>
 *               &lt;enumeration value="Community Nurse"/>
 *               &lt;enumeration value="Community Paediatrician"/>
 *               &lt;enumeration value="Community Psychiatric Nurse"/>
 *               &lt;enumeration value="Community Volunteer"/>
 *               &lt;enumeration value="Consultant"/>
 *               &lt;enumeration value="Continence Advisor"/>
 *               &lt;enumeration value="Corporate Appointee"/>
 *               &lt;enumeration value="Counsellor"/>
 *               &lt;enumeration value="Cousin"/>
 *               &lt;enumeration value="Daughter"/>
 *               &lt;enumeration value="Daughter-in-law"/>
 *               &lt;enumeration value="Dentist"/>
 *               &lt;enumeration value="Dependant"/>
 *               &lt;enumeration value="Detox Worker"/>
 *               &lt;enumeration value="Dietician"/>
 *               &lt;enumeration value="District Nurse"/>
 *               &lt;enumeration value="Educational Psychologist"/>
 *               &lt;enumeration value="Ex-husband"/>
 *               &lt;enumeration value="Ex-partner"/>
 *               &lt;enumeration value="Ex-wife"/>
 *               &lt;enumeration value="Family Member"/>
 *               &lt;enumeration value="Family Nurse"/>
 *               &lt;enumeration value="Family Support"/>
 *               &lt;enumeration value="Father"/>
 *               &lt;enumeration value="Father-in-law"/>
 *               &lt;enumeration value="Fiance"/>
 *               &lt;enumeration value="Flatmate"/>
 *               &lt;enumeration value="Foster Child"/>
 *               &lt;enumeration value="Foster Father"/>
 *               &lt;enumeration value="Foster Mother"/>
 *               &lt;enumeration value="Foster Parent"/>
 *               &lt;enumeration value="Fostering Social Worker"/>
 *               &lt;enumeration value="Friend"/>
 *               &lt;enumeration value="Funeral Director"/>
 *               &lt;enumeration value="Generic Worker"/>
 *               &lt;enumeration value="Girlfriend"/>
 *               &lt;enumeration value="GP"/>
 *               &lt;enumeration value="GP For Ante-natal Care"/>
 *               &lt;enumeration value="GP Practice"/>
 *               &lt;enumeration value="Grandchild"/>
 *               &lt;enumeration value="Grandfather"/>
 *               &lt;enumeration value="Grandmother"/>
 *               &lt;enumeration value="Group Worker"/>
 *               &lt;enumeration value="Guardian"/>
 *               &lt;enumeration value="Half Brother"/>
 *               &lt;enumeration value="Half Sister"/>
 *               &lt;enumeration value="Health Visitor"/>
 *               &lt;enumeration value="Heart Failure Specialist Nurse"/>
 *               &lt;enumeration value="Home Team Leader"/>
 *               &lt;enumeration value="Hospital Paediatrician"/>
 *               &lt;enumeration value="Husband"/>
 *               &lt;enumeration value="Independent Mental Capacity Act Advocate"/>
 *               &lt;enumeration value="Independent Reviewing Officer"/>
 *               &lt;enumeration value="Intermediate Care Team"/>
 *               &lt;enumeration value="Key Palliative Care Member"/>
 *               &lt;enumeration value="Key Worker"/>
 *               &lt;enumeration value="Landlord"/>
 *               &lt;enumeration value="Macmillan Nurse"/>
 *               &lt;enumeration value="Main Assessor"/>
 *               &lt;enumeration value="Main Carer"/>
 *               &lt;enumeration value="Maternity Support Worker"/>
 *               &lt;enumeration value="Mother"/>
 *               &lt;enumeration value="Mother-in-law"/>
 *               &lt;enumeration value="Named Nurse"/>
 *               &lt;enumeration value="Named Prescriber"/>
 *               &lt;enumeration value="Neighbour"/>
 *               &lt;enumeration value="Nephew"/>
 *               &lt;enumeration value="Niece"/>
 *               &lt;enumeration value="Non-dependant"/>
 *               &lt;enumeration value="None"/>
 *               &lt;enumeration value="Nursery Nurse"/>
 *               &lt;enumeration value="Obstetrician"/>
 *               &lt;enumeration value="Occupational Therapist"/>
 *               &lt;enumeration value="Oncologist"/>
 *               &lt;enumeration value="Optometrist"/>
 *               &lt;enumeration value="Other"/>
 *               &lt;enumeration value="Outreach Worker"/>
 *               &lt;enumeration value="Paediatrician"/>
 *               &lt;enumeration value="Parent"/>
 *               &lt;enumeration value="Partner"/>
 *               &lt;enumeration value="Person of Religion"/>
 *               &lt;enumeration value="Pharmacist"/>
 *               &lt;enumeration value="Physiotherapist"/>
 *               &lt;enumeration value="Play Specialist"/>
 *               &lt;enumeration value="Polygamous Partner"/>
 *               &lt;enumeration value="Post Discharge Worker"/>
 *               &lt;enumeration value="Power of Attorney"/>
 *               &lt;enumeration value="Practice Nurse"/>
 *               &lt;enumeration value="Proxy - Communication"/>
 *               &lt;enumeration value="Proxy - Contact"/>
 *               &lt;enumeration value="Proxy - Contact and Communication"/>
 *               &lt;enumeration value="PSI Worker"/>
 *               &lt;enumeration value="Psychiatrist"/>
 *               &lt;enumeration value="Residential Carer"/>
 *               &lt;enumeration value="Safeguarding Lead Professional"/>
 *               &lt;enumeration value="School Health Assistant"/>
 *               &lt;enumeration value="School Nurse"/>
 *               &lt;enumeration value="School Teacher"/>
 *               &lt;enumeration value="SENCO"/>
 *               &lt;enumeration value="Sibling"/>
 *               &lt;enumeration value="Sister"/>
 *               &lt;enumeration value="Sister-in-law"/>
 *               &lt;enumeration value="Social Care Provider"/>
 *               &lt;enumeration value="Social Worker"/>
 *               &lt;enumeration value="Solicitor"/>
 *               &lt;enumeration value="Son"/>
 *               &lt;enumeration value="Son-in-law"/>
 *               &lt;enumeration value="Specialist Health Visitor"/>
 *               &lt;enumeration value="Specialist Midwife"/>
 *               &lt;enumeration value="Specialist Nurse"/>
 *               &lt;enumeration value="Speech Therapist"/>
 *               &lt;enumeration value="Spouse/Partner"/>
 *               &lt;enumeration value="Step-brother"/>
 *               &lt;enumeration value="Step-daughter"/>
 *               &lt;enumeration value="Step-father"/>
 *               &lt;enumeration value="Step-mother"/>
 *               &lt;enumeration value="Step-parent"/>
 *               &lt;enumeration value="Step-sister"/>
 *               &lt;enumeration value="Step-son"/>
 *               &lt;enumeration value="Uncle"/>
 *               &lt;enumeration value="Unknown"/>
 *               &lt;enumeration value="Ward Nurse"/>
 *               &lt;enumeration value="Warden"/>
 *               &lt;enumeration value="Wife"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="OrganisationName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Telephone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="WorkContact" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EndDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="DateOfBirth" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="LinkedProblemUID" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Relationship", propOrder = {
    "title",
    "firstName",
    "middleNames",
    "surname",
    "address",
    "type",
    "organisationName",
    "telephone",
    "workContact",
    "description",
    "endDate",
    "dateOfBirth",
    "linkedProblemUID"
})
public class Relationship {

    @XmlElement(name = "Title")
    protected String title;
    @XmlElement(name = "FirstName")
    protected String firstName;
    @XmlElement(name = "MiddleNames")
    protected String middleNames;
    @XmlElement(name = "Surname")
    protected String surname;
    @XmlElement(name = "Address")
    protected Address address;
    @XmlElement(name = "Type", required = true)
    protected String type;
    @XmlElement(name = "OrganisationName")
    protected String organisationName;
    @XmlElement(name = "Telephone")
    protected String telephone;
    @XmlElement(name = "WorkContact")
    protected String workContact;
    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "EndDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar endDate;
    @XmlElement(name = "DateOfBirth")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dateOfBirth;
    @XmlElement(name = "LinkedProblemUID")
    protected List<String> linkedProblemUID;

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
     * Gets the value of the firstName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the value of the firstName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstName(String value) {
        this.firstName = value;
    }

    /**
     * Gets the value of the middleNames property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMiddleNames() {
        return middleNames;
    }

    /**
     * Sets the value of the middleNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMiddleNames(String value) {
        this.middleNames = value;
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
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link Address }
     *     
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link Address }
     *     
     */
    public void setAddress(Address value) {
        this.address = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the organisationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * Sets the value of the organisationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisationName(String value) {
        this.organisationName = value;
    }

    /**
     * Gets the value of the telephone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * Sets the value of the telephone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTelephone(String value) {
        this.telephone = value;
    }

    /**
     * Gets the value of the workContact property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkContact() {
        return workContact;
    }

    /**
     * Sets the value of the workContact property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkContact(String value) {
        this.workContact = value;
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
     * Gets the value of the endDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEndDate() {
        return endDate;
    }

    /**
     * Sets the value of the endDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEndDate(XMLGregorianCalendar value) {
        this.endDate = value;
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
     * Gets the value of the linkedProblemUID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkedProblemUID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkedProblemUID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLinkedProblemUID() {
        if (linkedProblemUID == null) {
            linkedProblemUID = new ArrayList<String>();
        }
        return this.linkedProblemUID;
    }

}

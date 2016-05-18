
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Desribes a person  (e.g. as user) user n a particular role
 * 
 * <p>Java class for PersonType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PersonType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="Mnemonic" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PCSUser" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="ActiveEMISUser" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FirstNames" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Category" type="{http://www.e-mis.com/emisopen/MedicalRecord}PersonCategoryType" minOccurs="0"/>
 *         &lt;element name="NationalCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PrescriptionCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="GmcCode" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="UKCCCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Scripts" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Y"/>
 *               &lt;enumeration value="N"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Consulter" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Y"/>
 *               &lt;enumeration value="N"/>
 *               &lt;enumeration value="O"/>
 *               &lt;enumeration value=""/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ContractStart" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ContractEnd" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Doctor" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Y"/>
 *               &lt;enumeration value="N"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Locum" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Y"/>
 *               &lt;enumeration value="N"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Registrar" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Y"/>
 *               &lt;enumeration value="N"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Maternity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ChildSurveillance" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;length value="1"/>
 *               &lt;enumeration value="Y"/>
 *               &lt;enumeration value="N"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Contraceptive" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;length value="1"/>
 *               &lt;enumeration value="Y"/>
 *               &lt;enumeration value="N"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="TwentyFourHour" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Y"/>
 *               &lt;enumeration value="N"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="MinorSurgery" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Y"/>
 *               &lt;enumeration value="N"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ResonsibleHA" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="Trainer" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="ContractualRelationship" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="A"/>
 *               &lt;enumeration value="E"/>
 *               &lt;enumeration value="O"/>
 *               &lt;enumeration value="P"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Address" type="{http://www.e-mis.com/emisopen/MedicalRecord}AddressType" minOccurs="0"/>
 *         &lt;element name="Telephone1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Telephone2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Fax" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Role" type="{http://www.e-mis.com/emisopen/MedicalRecord}RoleType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Security1" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Security2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PersonType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "mnemonic",
    "pcsUser",
    "activeEMISUser",
    "title",
    "firstNames",
    "lastName",
    "category",
    "nationalCode",
    "prescriptionCode",
    "gmcCode",
    "ukccCode",
    "scripts",
    "consulter",
    "contractStart",
    "contractEnd",
    "doctor",
    "locum",
    "registrar",
    "maternity",
    "childSurveillance",
    "contraceptive",
    "twentyFourHour",
    "minorSurgery",
    "resonsibleHA",
    "trainer",
    "contractualRelationship",
    "address",
    "telephone1",
    "telephone2",
    "fax",
    "email",
    "role"
})
public class PersonType
    extends IdentType
{

    @XmlElement(name = "Mnemonic", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String mnemonic;
    @XmlElement(name = "PCSUser", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger pcsUser;
    @XmlElement(name = "ActiveEMISUser", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String activeEMISUser;
    @XmlElement(name = "Title", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String title;
    @XmlElement(name = "FirstNames", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String firstNames;
    @XmlElement(name = "LastName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String lastName;
    @XmlElement(name = "Category", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected PersonCategoryType category;
    @XmlElement(name = "NationalCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String nationalCode;
    @XmlElement(name = "PrescriptionCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String prescriptionCode;
    @XmlElement(name = "GmcCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger gmcCode;
    @XmlElement(name = "UKCCCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String ukccCode;
    @XmlElement(name = "Scripts", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String scripts;
    @XmlElement(name = "Consulter", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String consulter;
    @XmlElement(name = "ContractStart", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String contractStart;
    @XmlElement(name = "ContractEnd", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String contractEnd;
    @XmlElement(name = "Doctor", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String doctor;
    @XmlElement(name = "Locum", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String locum;
    @XmlElement(name = "Registrar", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String registrar;
    @XmlElement(name = "Maternity", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String maternity;
    @XmlElement(name = "ChildSurveillance", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String childSurveillance;
    @XmlElement(name = "Contraceptive", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String contraceptive;
    @XmlElement(name = "TwentyFourHour", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String twentyFourHour;
    @XmlElement(name = "MinorSurgery", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String minorSurgery;
    @XmlElement(name = "ResonsibleHA", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType resonsibleHA;
    @XmlElement(name = "Trainer", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType trainer;
    @XmlElement(name = "ContractualRelationship", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String contractualRelationship;
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
    @XmlElement(name = "Role", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected RoleType role;
    @XmlAttribute(name = "Security1")
    protected String security1;
    @XmlAttribute(name = "Security2")
    protected String security2;

    /**
     * Gets the value of the mnemonic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMnemonic() {
        return mnemonic;
    }

    /**
     * Sets the value of the mnemonic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMnemonic(String value) {
        this.mnemonic = value;
    }

    /**
     * Gets the value of the pcsUser property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPCSUser() {
        return pcsUser;
    }

    /**
     * Sets the value of the pcsUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPCSUser(BigInteger value) {
        this.pcsUser = value;
    }

    /**
     * Gets the value of the activeEMISUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActiveEMISUser() {
        return activeEMISUser;
    }

    /**
     * Sets the value of the activeEMISUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActiveEMISUser(String value) {
        this.activeEMISUser = value;
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
     * Gets the value of the firstNames property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstNames() {
        return firstNames;
    }

    /**
     * Sets the value of the firstNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstNames(String value) {
        this.firstNames = value;
    }

    /**
     * Gets the value of the lastName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the value of the lastName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastName(String value) {
        this.lastName = value;
    }

    /**
     * Gets the value of the category property.
     * 
     * @return
     *     possible object is
     *     {@link PersonCategoryType }
     *     
     */
    public PersonCategoryType getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     * @param value
     *     allowed object is
     *     {@link PersonCategoryType }
     *     
     */
    public void setCategory(PersonCategoryType value) {
        this.category = value;
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
     * Gets the value of the prescriptionCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrescriptionCode() {
        return prescriptionCode;
    }

    /**
     * Sets the value of the prescriptionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrescriptionCode(String value) {
        this.prescriptionCode = value;
    }

    /**
     * Gets the value of the gmcCode property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getGmcCode() {
        return gmcCode;
    }

    /**
     * Sets the value of the gmcCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setGmcCode(BigInteger value) {
        this.gmcCode = value;
    }

    /**
     * Gets the value of the ukccCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUKCCCode() {
        return ukccCode;
    }

    /**
     * Sets the value of the ukccCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUKCCCode(String value) {
        this.ukccCode = value;
    }

    /**
     * Gets the value of the scripts property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScripts() {
        return scripts;
    }

    /**
     * Sets the value of the scripts property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScripts(String value) {
        this.scripts = value;
    }

    /**
     * Gets the value of the consulter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsulter() {
        return consulter;
    }

    /**
     * Sets the value of the consulter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsulter(String value) {
        this.consulter = value;
    }

    /**
     * Gets the value of the contractStart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContractStart() {
        return contractStart;
    }

    /**
     * Sets the value of the contractStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContractStart(String value) {
        this.contractStart = value;
    }

    /**
     * Gets the value of the contractEnd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContractEnd() {
        return contractEnd;
    }

    /**
     * Sets the value of the contractEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContractEnd(String value) {
        this.contractEnd = value;
    }

    /**
     * Gets the value of the doctor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDoctor() {
        return doctor;
    }

    /**
     * Sets the value of the doctor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDoctor(String value) {
        this.doctor = value;
    }

    /**
     * Gets the value of the locum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocum() {
        return locum;
    }

    /**
     * Sets the value of the locum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocum(String value) {
        this.locum = value;
    }

    /**
     * Gets the value of the registrar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegistrar() {
        return registrar;
    }

    /**
     * Sets the value of the registrar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegistrar(String value) {
        this.registrar = value;
    }

    /**
     * Gets the value of the maternity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaternity() {
        return maternity;
    }

    /**
     * Sets the value of the maternity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaternity(String value) {
        this.maternity = value;
    }

    /**
     * Gets the value of the childSurveillance property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChildSurveillance() {
        return childSurveillance;
    }

    /**
     * Sets the value of the childSurveillance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChildSurveillance(String value) {
        this.childSurveillance = value;
    }

    /**
     * Gets the value of the contraceptive property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContraceptive() {
        return contraceptive;
    }

    /**
     * Sets the value of the contraceptive property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContraceptive(String value) {
        this.contraceptive = value;
    }

    /**
     * Gets the value of the twentyFourHour property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTwentyFourHour() {
        return twentyFourHour;
    }

    /**
     * Sets the value of the twentyFourHour property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTwentyFourHour(String value) {
        this.twentyFourHour = value;
    }

    /**
     * Gets the value of the minorSurgery property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinorSurgery() {
        return minorSurgery;
    }

    /**
     * Sets the value of the minorSurgery property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinorSurgery(String value) {
        this.minorSurgery = value;
    }

    /**
     * Gets the value of the resonsibleHA property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getResonsibleHA() {
        return resonsibleHA;
    }

    /**
     * Sets the value of the resonsibleHA property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setResonsibleHA(IdentType value) {
        this.resonsibleHA = value;
    }

    /**
     * Gets the value of the trainer property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getTrainer() {
        return trainer;
    }

    /**
     * Sets the value of the trainer property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setTrainer(IdentType value) {
        this.trainer = value;
    }

    /**
     * Gets the value of the contractualRelationship property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContractualRelationship() {
        return contractualRelationship;
    }

    /**
     * Sets the value of the contractualRelationship property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContractualRelationship(String value) {
        this.contractualRelationship = value;
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
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link RoleType }
     *     
     */
    public RoleType getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link RoleType }
     *     
     */
    public void setRole(RoleType value) {
        this.role = value;
    }

    /**
     * Gets the value of the security1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecurity1() {
        return security1;
    }

    /**
     * Sets the value of the security1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecurity1(String value) {
        this.security1 = value;
    }

    /**
     * Gets the value of the security2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecurity2() {
        return security2;
    }

    /**
     * Sets the value of the security2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecurity2(String value) {
        this.security2 = value;
    }

}

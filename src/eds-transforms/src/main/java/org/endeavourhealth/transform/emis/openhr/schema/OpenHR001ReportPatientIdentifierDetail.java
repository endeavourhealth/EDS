
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.ReportPatientIdentifierDetail complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.ReportPatientIdentifierDetail">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="forenames" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="108"/>
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
 *         &lt;element name="birthDate" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="nhs" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="address" type="{http://www.e-mis.com/emisopen}dt.Address" minOccurs="0"/>
 *         &lt;element name="sex" type="{http://www.e-mis.com/emisopen}voc.Sex" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.ReportPatientIdentifierDetail", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "forenames",
    "surname",
    "birthDate",
    "nhs",
    "address",
    "sex"
})
@XmlSeeAlso({
    OpenHR001ReportPatientIdentifier.OriginalDetails.class,
    OpenHR001ReportPatientIdentifier.MatchedDetails.class
})
public class OpenHR001ReportPatientIdentifierDetail {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String forenames;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String surname;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar birthDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String nhs;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtAddress address;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocSex sex;

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
     * Gets the value of the nhs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNhs() {
        return nhs;
    }

    /**
     * Sets the value of the nhs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNhs(String value) {
        this.nhs = value;
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

}

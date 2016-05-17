
package org.endeavourhealth.transform.emis.emisopen.schema.eomslotsforsession;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PatientStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PatientStruct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DBID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="RefID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="GUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FirstNames" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Surname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FullName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PatientStruct", propOrder = {
    "dbid",
    "refID",
    "guid",
    "title",
    "firstNames",
    "surname",
    "fullName"
})
public class PatientStruct {

    @XmlElement(name = "DBID")
    protected int dbid;
    @XmlElement(name = "RefID")
    protected int refID;
    @XmlElement(name = "GUID", required = true)
    protected String guid;
    @XmlElement(name = "Title", required = true)
    protected String title;
    @XmlElement(name = "FirstNames", required = true)
    protected String firstNames;
    @XmlElement(name = "Surname", required = true)
    protected String surname;
    @XmlElement(name = "FullName", required = true)
    protected String fullName;

    /**
     * Gets the value of the dbid property.
     * 
     */
    public int getDBID() {
        return dbid;
    }

    /**
     * Sets the value of the dbid property.
     * 
     */
    public void setDBID(int value) {
        this.dbid = value;
    }

    /**
     * Gets the value of the refID property.
     * 
     */
    public int getRefID() {
        return refID;
    }

    /**
     * Sets the value of the refID property.
     * 
     */
    public void setRefID(int value) {
        this.refID = value;
    }

    /**
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGUID() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGUID(String value) {
        this.guid = value;
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
     * Gets the value of the fullName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the value of the fullName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFullName(String value) {
        this.fullName = value;
    }

}

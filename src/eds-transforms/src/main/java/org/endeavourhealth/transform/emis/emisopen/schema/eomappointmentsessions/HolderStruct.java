
package org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HolderStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HolderStruct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DBID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="RefID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="GUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FirstNames" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LastName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HolderStruct", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "dbid",
    "refID",
    "guid",
    "title",
    "firstNames",
    "lastName"
})
public class HolderStruct {

    @XmlElement(name = "DBID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected int dbid;
    @XmlElement(name = "RefID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected int refID;
    @XmlElement(name = "GUID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String guid;
    @XmlElement(name = "Title", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String title;
    @XmlElement(name = "FirstNames", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String firstNames;
    @XmlElement(name = "LastName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String lastName;

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

}

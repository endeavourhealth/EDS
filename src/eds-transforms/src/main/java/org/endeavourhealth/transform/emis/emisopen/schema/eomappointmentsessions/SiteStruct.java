
package org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SiteStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SiteStruct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DBID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="GUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SiteStruct", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "dbid",
    "guid",
    "name"
})
public class SiteStruct {

    @XmlElement(name = "DBID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected int dbid;
    @XmlElement(name = "GUID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String guid;
    @XmlElement(name = "Name", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String name;

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

}

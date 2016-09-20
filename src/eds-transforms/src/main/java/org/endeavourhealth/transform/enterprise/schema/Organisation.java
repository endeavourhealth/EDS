
package org.endeavourhealth.transform.enterprise.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for organisation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="organisation">
 *   &lt;complexContent>
 *     &lt;extension base="{}baseRecord">
 *       &lt;sequence>
 *         &lt;element name="ods_code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type_code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type_desc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="postcode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parent_organisation_id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "organisation", propOrder = {
    "odsCode",
    "name",
    "typeCode",
    "typeDesc",
    "postcode",
    "parentOrganisationId"
})
public class Organisation
    extends BaseRecord
{

    @XmlElement(name = "ods_code")
    protected String odsCode;
    protected String name;
    @XmlElement(name = "type_code")
    protected String typeCode;
    @XmlElement(name = "type_desc")
    protected String typeDesc;
    protected String postcode;
    @XmlElement(name = "parent_organisation_id")
    protected String parentOrganisationId;

    /**
     * Gets the value of the odsCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOdsCode() {
        return odsCode;
    }

    /**
     * Sets the value of the odsCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOdsCode(String value) {
        this.odsCode = value;
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
     * Gets the value of the typeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeCode() {
        return typeCode;
    }

    /**
     * Sets the value of the typeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeCode(String value) {
        this.typeCode = value;
    }

    /**
     * Gets the value of the typeDesc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeDesc() {
        return typeDesc;
    }

    /**
     * Sets the value of the typeDesc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeDesc(String value) {
        this.typeDesc = value;
    }

    /**
     * Gets the value of the postcode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets the value of the postcode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostcode(String value) {
        this.postcode = value;
    }

    /**
     * Gets the value of the parentOrganisationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentOrganisationId() {
        return parentOrganisationId;
    }

    /**
     * Sets the value of the parentOrganisationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentOrganisationId(String value) {
        this.parentOrganisationId = value;
    }

}

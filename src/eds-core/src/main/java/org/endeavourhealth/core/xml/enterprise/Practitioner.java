
package org.endeavourhealth.core.xml.enterprise;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for practitioner complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="practitioner">
 *   &lt;complexContent>
 *     &lt;extension base="{}baseRecord">
 *       &lt;sequence>
 *         &lt;element name="organisation_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="role_code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="role_desc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "practitioner", propOrder = {
    "organisationId",
    "name",
    "roleCode",
    "roleDesc"
})
public class Practitioner
    extends BaseRecord
{

    @XmlElement(name = "organisation_id")
    protected String organisationId;
    protected String name;
    @XmlElement(name = "role_code")
    protected String roleCode;
    @XmlElement(name = "role_desc")
    protected String roleDesc;

    /**
     * Gets the value of the organisationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisationId() {
        return organisationId;
    }

    /**
     * Sets the value of the organisationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisationId(String value) {
        this.organisationId = value;
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
     * Gets the value of the roleCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoleCode() {
        return roleCode;
    }

    /**
     * Sets the value of the roleCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoleCode(String value) {
        this.roleCode = value;
    }

    /**
     * Gets the value of the roleDesc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoleDesc() {
        return roleDesc;
    }

    /**
     * Sets the value of the roleDesc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoleDesc(String value) {
        this.roleDesc = value;
    }

}

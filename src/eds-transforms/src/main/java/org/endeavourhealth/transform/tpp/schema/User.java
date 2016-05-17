
package org.endeavourhealth.transform.tpp.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for User complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="User">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FirstName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MiddleNames" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Surname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="UserName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Gender" type="{}Sex"/>
 *         &lt;element name="Role" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RoleCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NationalID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NationalIDType" type="{}NationalIDType" minOccurs="0"/>
 *         &lt;element name="AccessRights" type="{}AccessRights"/>
 *         &lt;element name="UserGroups" type="{}UserGroups"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "User", propOrder = {
    "title",
    "firstName",
    "middleNames",
    "surname",
    "userName",
    "gender",
    "role",
    "roleCode",
    "nationalID",
    "nationalIDType",
    "accessRights",
    "userGroups"
})
public class User {

    @XmlElement(name = "Title", required = true)
    protected String title;
    @XmlElement(name = "FirstName", required = true)
    protected String firstName;
    @XmlElement(name = "MiddleNames")
    protected String middleNames;
    @XmlElement(name = "Surname", required = true)
    protected String surname;
    @XmlElement(name = "UserName", required = true)
    protected String userName;
    @XmlElement(name = "Gender", required = true)
    @XmlSchemaType(name = "string")
    protected Sex gender;
    @XmlElement(name = "Role", required = true)
    protected String role;
    @XmlElement(name = "RoleCode")
    protected String roleCode;
    @XmlElement(name = "NationalID")
    protected String nationalID;
    @XmlElement(name = "NationalIDType")
    @XmlSchemaType(name = "string")
    protected NationalIDType nationalIDType;
    @XmlElement(name = "AccessRights", required = true)
    protected AccessRights accessRights;
    @XmlElement(name = "UserGroups", required = true)
    protected UserGroups userGroups;

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
     * Gets the value of the userName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of the gender property.
     * 
     * @return
     *     possible object is
     *     {@link Sex }
     *     
     */
    public Sex getGender() {
        return gender;
    }

    /**
     * Sets the value of the gender property.
     * 
     * @param value
     *     allowed object is
     *     {@link Sex }
     *     
     */
    public void setGender(Sex value) {
        this.gender = value;
    }

    /**
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRole(String value) {
        this.role = value;
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
     * Gets the value of the nationalID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNationalID() {
        return nationalID;
    }

    /**
     * Sets the value of the nationalID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNationalID(String value) {
        this.nationalID = value;
    }

    /**
     * Gets the value of the nationalIDType property.
     * 
     * @return
     *     possible object is
     *     {@link NationalIDType }
     *     
     */
    public NationalIDType getNationalIDType() {
        return nationalIDType;
    }

    /**
     * Sets the value of the nationalIDType property.
     * 
     * @param value
     *     allowed object is
     *     {@link NationalIDType }
     *     
     */
    public void setNationalIDType(NationalIDType value) {
        this.nationalIDType = value;
    }

    /**
     * Gets the value of the accessRights property.
     * 
     * @return
     *     possible object is
     *     {@link AccessRights }
     *     
     */
    public AccessRights getAccessRights() {
        return accessRights;
    }

    /**
     * Sets the value of the accessRights property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessRights }
     *     
     */
    public void setAccessRights(AccessRights value) {
        this.accessRights = value;
    }

    /**
     * Gets the value of the userGroups property.
     * 
     * @return
     *     possible object is
     *     {@link UserGroups }
     *     
     */
    public UserGroups getUserGroups() {
        return userGroups;
    }

    /**
     * Sets the value of the userGroups property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserGroups }
     *     
     */
    public void setUserGroups(UserGroups value) {
        this.userGroups = value;
    }

}

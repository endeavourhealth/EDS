
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.UserInRole complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.UserInRole">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="user" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="role" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="contractualRelationship" type="{http://www.e-mis.com/emisopen}voc.ContractualRelationship"/>
 *         &lt;element name="contractStart" type="{http://www.e-mis.com/emisopen}dt.Date"/>
 *         &lt;element name="contractEnd" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *         &lt;element name="filingConfidentialityPolicy" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="userIdentifier" type="{http://www.e-mis.com/emisopen}dt.UserIdentifier" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="authoriseScripts" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="consulter" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="sessionHolder" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.UserInRole", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "user",
    "role",
    "contractualRelationship",
    "contractStart",
    "contractEnd",
    "filingConfidentialityPolicy",
    "userIdentifier",
    "authoriseScripts",
    "consulter",
    "sessionHolder"
})
public class OpenHR001UserInRole
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String user;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String role;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocContractualRelationship contractualRelationship;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar contractStart;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar contractEnd;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String filingConfidentialityPolicy;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<DtUserIdentifier> userIdentifier;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean authoriseScripts;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean consulter;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean sessionHolder;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUser(String value) {
        this.user = value;
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
     * Gets the value of the contractualRelationship property.
     * 
     * @return
     *     possible object is
     *     {@link VocContractualRelationship }
     *     
     */
    public VocContractualRelationship getContractualRelationship() {
        return contractualRelationship;
    }

    /**
     * Sets the value of the contractualRelationship property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocContractualRelationship }
     *     
     */
    public void setContractualRelationship(VocContractualRelationship value) {
        this.contractualRelationship = value;
    }

    /**
     * Gets the value of the contractStart property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getContractStart() {
        return contractStart;
    }

    /**
     * Sets the value of the contractStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setContractStart(XMLGregorianCalendar value) {
        this.contractStart = value;
    }

    /**
     * Gets the value of the contractEnd property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getContractEnd() {
        return contractEnd;
    }

    /**
     * Sets the value of the contractEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setContractEnd(XMLGregorianCalendar value) {
        this.contractEnd = value;
    }

    /**
     * Gets the value of the filingConfidentialityPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilingConfidentialityPolicy() {
        return filingConfidentialityPolicy;
    }

    /**
     * Sets the value of the filingConfidentialityPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilingConfidentialityPolicy(String value) {
        this.filingConfidentialityPolicy = value;
    }

    /**
     * Gets the value of the userIdentifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userIdentifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DtUserIdentifier }
     * 
     * 
     */
    public List<DtUserIdentifier> getUserIdentifier() {
        if (userIdentifier == null) {
            userIdentifier = new ArrayList<DtUserIdentifier>();
        }
        return this.userIdentifier;
    }

    /**
     * Gets the value of the authoriseScripts property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAuthoriseScripts() {
        return authoriseScripts;
    }

    /**
     * Sets the value of the authoriseScripts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAuthoriseScripts(Boolean value) {
        this.authoriseScripts = value;
    }

    /**
     * Gets the value of the consulter property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isConsulter() {
        return consulter;
    }

    /**
     * Sets the value of the consulter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setConsulter(Boolean value) {
        this.consulter = value;
    }

    /**
     * Gets the value of the sessionHolder property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSessionHolder() {
        return sessionHolder;
    }

    /**
     * Sets the value of the sessionHolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSessionHolder(Boolean value) {
        this.sessionHolder = value;
    }

}

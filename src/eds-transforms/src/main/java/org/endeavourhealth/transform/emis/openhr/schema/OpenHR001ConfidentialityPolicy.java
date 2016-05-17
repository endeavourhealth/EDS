
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


/**
 * <p>Java class for OpenHR001.ConfidentialityPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.ConfidentialityPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="notificationType" type="{http://www.e-mis.com/emisopen}voc.ConfidentialityPolicyNotificationType"/>
 *         &lt;element name="ownerUser" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="showHiddenRecordIndicator" type="{http://www.e-mis.com/emisopen}dt.bool"/>
 *         &lt;element name="organisation" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="restrictToOrganisation" type="{http://www.e-mis.com/emisopen}dt.bool"/>
 *         &lt;element name="userCategory" type="{http://www.e-mis.com/emisopen}dt.Code" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.ConfidentialityPolicy", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "name",
    "notificationType",
    "ownerUser",
    "showHiddenRecordIndicator",
    "organisation",
    "restrictToOrganisation",
    "userCategory"
})
public class OpenHR001ConfidentialityPolicy
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String name;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocConfidentialityPolicyNotificationType notificationType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String ownerUser;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected boolean showHiddenRecordIndicator;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String organisation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected boolean restrictToOrganisation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<DtCode> userCategory;

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
     * Gets the value of the notificationType property.
     * 
     * @return
     *     possible object is
     *     {@link VocConfidentialityPolicyNotificationType }
     *     
     */
    public VocConfidentialityPolicyNotificationType getNotificationType() {
        return notificationType;
    }

    /**
     * Sets the value of the notificationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocConfidentialityPolicyNotificationType }
     *     
     */
    public void setNotificationType(VocConfidentialityPolicyNotificationType value) {
        this.notificationType = value;
    }

    /**
     * Gets the value of the ownerUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerUser() {
        return ownerUser;
    }

    /**
     * Sets the value of the ownerUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerUser(String value) {
        this.ownerUser = value;
    }

    /**
     * Gets the value of the showHiddenRecordIndicator property.
     * 
     */
    public boolean isShowHiddenRecordIndicator() {
        return showHiddenRecordIndicator;
    }

    /**
     * Sets the value of the showHiddenRecordIndicator property.
     * 
     */
    public void setShowHiddenRecordIndicator(boolean value) {
        this.showHiddenRecordIndicator = value;
    }

    /**
     * Gets the value of the organisation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisation() {
        return organisation;
    }

    /**
     * Sets the value of the organisation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisation(String value) {
        this.organisation = value;
    }

    /**
     * Gets the value of the restrictToOrganisation property.
     * 
     */
    public boolean isRestrictToOrganisation() {
        return restrictToOrganisation;
    }

    /**
     * Sets the value of the restrictToOrganisation property.
     * 
     */
    public void setRestrictToOrganisation(boolean value) {
        this.restrictToOrganisation = value;
    }

    /**
     * Gets the value of the userCategory property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userCategory property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserCategory().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DtCode }
     * 
     * 
     */
    public List<DtCode> getUserCategory() {
        if (userCategory == null) {
            userCategory = new ArrayList<DtCode>();
        }
        return this.userCategory;
    }

}

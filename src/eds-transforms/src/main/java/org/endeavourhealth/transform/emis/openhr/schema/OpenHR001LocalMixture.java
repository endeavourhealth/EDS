
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
 * <p>Java class for OpenHR001.LocalMixture complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.LocalMixture">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="name" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="organisation" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="deleted" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="createdTime" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="creatingUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="editedTime" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="editedUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="constituent" type="{http://www.e-mis.com/emisopen}OpenHR001.LocalMixtureConstituent" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.LocalMixture", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "name",
    "organisation",
    "deleted",
    "createdTime",
    "creatingUserInRole",
    "editedTime",
    "editedUserInRole",
    "constituent"
})
public class OpenHR001LocalMixture
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String name;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String organisation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean deleted;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createdTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String creatingUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar editedTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String editedUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001LocalMixtureConstituent> constituent;

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
     * Gets the value of the deleted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDeleted() {
        return deleted;
    }

    /**
     * Sets the value of the deleted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDeleted(Boolean value) {
        this.deleted = value;
    }

    /**
     * Gets the value of the createdTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreatedTime() {
        return createdTime;
    }

    /**
     * Sets the value of the createdTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreatedTime(XMLGregorianCalendar value) {
        this.createdTime = value;
    }

    /**
     * Gets the value of the creatingUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatingUserInRole() {
        return creatingUserInRole;
    }

    /**
     * Sets the value of the creatingUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatingUserInRole(String value) {
        this.creatingUserInRole = value;
    }

    /**
     * Gets the value of the editedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEditedTime() {
        return editedTime;
    }

    /**
     * Sets the value of the editedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEditedTime(XMLGregorianCalendar value) {
        this.editedTime = value;
    }

    /**
     * Gets the value of the editedUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEditedUserInRole() {
        return editedUserInRole;
    }

    /**
     * Sets the value of the editedUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEditedUserInRole(String value) {
        this.editedUserInRole = value;
    }

    /**
     * Gets the value of the constituent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the constituent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConstituent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001LocalMixtureConstituent }
     * 
     * 
     */
    public List<OpenHR001LocalMixtureConstituent> getConstituent() {
        if (constituent == null) {
            constituent = new ArrayList<OpenHR001LocalMixtureConstituent>();
        }
        return this.constituent;
    }

}

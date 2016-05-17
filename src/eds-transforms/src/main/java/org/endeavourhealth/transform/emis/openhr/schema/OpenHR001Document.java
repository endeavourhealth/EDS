
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
 * <p>Java class for OpenHR001.Document complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Document">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="name" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="500"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="description" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="1000"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="uploadUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="producedTime" type="{http://www.e-mis.com/emisopen}dt.DateTime" minOccurs="0"/>
 *         &lt;element name="deleted" type="{http://www.e-mis.com/emisopen}dt.bool" minOccurs="0"/>
 *         &lt;element name="folder" type="{http://www.e-mis.com/emisopen}OpenHR001.DocumentFolder" minOccurs="0"/>
 *         &lt;element name="observation" type="{http://www.e-mis.com/emisopen}dt.uid" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="locator" type="{http://www.e-mis.com/emisopen}voc.AttachmentLocator" minOccurs="0"/>
 *         &lt;element name="data" type="{http://www.e-mis.com/emisopen}dt.DocumentData" minOccurs="0"/>
 *         &lt;element name="code" type="{http://www.e-mis.com/emisopen}dt.CodeQualified" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Document", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "name",
    "description",
    "uploadUserInRole",
    "producedTime",
    "deleted",
    "folder",
    "observation",
    "locator",
    "data",
    "code"
})
public class OpenHR001Document
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String name;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String description;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String uploadUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar producedTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected Boolean deleted;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001DocumentFolder folder;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected List<String> observation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocAttachmentLocator locator;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtDocumentData data;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtCodeQualified code;

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
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the uploadUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUploadUserInRole() {
        return uploadUserInRole;
    }

    /**
     * Sets the value of the uploadUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUploadUserInRole(String value) {
        this.uploadUserInRole = value;
    }

    /**
     * Gets the value of the producedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getProducedTime() {
        return producedTime;
    }

    /**
     * Sets the value of the producedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setProducedTime(XMLGregorianCalendar value) {
        this.producedTime = value;
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
     * Gets the value of the folder property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001DocumentFolder }
     *     
     */
    public OpenHR001DocumentFolder getFolder() {
        return folder;
    }

    /**
     * Sets the value of the folder property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001DocumentFolder }
     *     
     */
    public void setFolder(OpenHR001DocumentFolder value) {
        this.folder = value;
    }

    /**
     * Gets the value of the observation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the observation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObservation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getObservation() {
        if (observation == null) {
            observation = new ArrayList<String>();
        }
        return this.observation;
    }

    /**
     * Gets the value of the locator property.
     * 
     * @return
     *     possible object is
     *     {@link VocAttachmentLocator }
     *     
     */
    public VocAttachmentLocator getLocator() {
        return locator;
    }

    /**
     * Sets the value of the locator property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocAttachmentLocator }
     *     
     */
    public void setLocator(VocAttachmentLocator value) {
        this.locator = value;
    }

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     {@link DtDocumentData }
     *     
     */
    public DtDocumentData getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtDocumentData }
     *     
     */
    public void setData(DtDocumentData value) {
        this.data = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link DtCodeQualified }
     *     
     */
    public DtCodeQualified getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCodeQualified }
     *     
     */
    public void setCode(DtCodeQualified value) {
        this.code = value;
    }

}

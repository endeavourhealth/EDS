
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.Attachment complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Attachment">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="attachmentType" type="{http://www.e-mis.com/emisopen}voc.AttachmentType"/>
 *         &lt;element name="uploadedUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="uploadedTime" type="{http://www.e-mis.com/emisopen}dt.DateTime"/>
 *         &lt;element name="data" type="{http://www.e-mis.com/emisopen}dt.DocumentData"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Attachment", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "attachmentType",
    "uploadedUserInRole",
    "uploadedTime",
    "data"
})
public class OpenHR001Attachment
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocAttachmentType attachmentType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String uploadedUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar uploadedTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected DtDocumentData data;

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
     * Gets the value of the attachmentType property.
     * 
     * @return
     *     possible object is
     *     {@link VocAttachmentType }
     *     
     */
    public VocAttachmentType getAttachmentType() {
        return attachmentType;
    }

    /**
     * Sets the value of the attachmentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocAttachmentType }
     *     
     */
    public void setAttachmentType(VocAttachmentType value) {
        this.attachmentType = value;
    }

    /**
     * Gets the value of the uploadedUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUploadedUserInRole() {
        return uploadedUserInRole;
    }

    /**
     * Sets the value of the uploadedUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUploadedUserInRole(String value) {
        this.uploadedUserInRole = value;
    }

    /**
     * Gets the value of the uploadedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUploadedTime() {
        return uploadedTime;
    }

    /**
     * Sets the value of the uploadedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUploadedTime(XMLGregorianCalendar value) {
        this.uploadedTime = value;
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

}


package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.DocumentFolder complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.DocumentFolder">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="folderType" type="{http://www.e-mis.com/emisopen}voc.DocumentFolderType"/>
 *         &lt;element name="name">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="200"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="deleted" type="{http://www.e-mis.com/emisopen}dt.bool"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.DocumentFolder", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "folderType",
    "name",
    "deleted"
})
public class OpenHR001DocumentFolder
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocDocumentFolderType folderType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String name;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", defaultValue = "false")
    protected boolean deleted;

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
     * Gets the value of the folderType property.
     * 
     * @return
     *     possible object is
     *     {@link VocDocumentFolderType }
     *     
     */
    public VocDocumentFolderType getFolderType() {
        return folderType;
    }

    /**
     * Sets the value of the folderType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocDocumentFolderType }
     *     
     */
    public void setFolderType(VocDocumentFolderType value) {
        this.folderType = value;
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
     * Gets the value of the deleted property.
     * 
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Sets the value of the deleted property.
     * 
     */
    public void setDeleted(boolean value) {
        this.deleted = value;
    }

}

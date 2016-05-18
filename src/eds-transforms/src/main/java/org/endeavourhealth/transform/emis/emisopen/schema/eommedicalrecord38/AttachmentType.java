
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AttachmentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttachmentType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}CodedItemBaseType">
 *       &lt;sequence>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Address" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Version" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="DDSIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AttachmentData" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttachmentType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "title",
    "address",
    "version",
    "ddsIdentifier",
    "attachmentData"
})
public class AttachmentType
    extends CodedItemBaseType
{

    @XmlElement(name = "Title", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String title;
    @XmlElement(name = "Address", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String address;
    @XmlElement(name = "Version", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger version;
    @XmlElement(name = "DDSIdentifier", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String ddsIdentifier;
    @XmlElement(name = "AttachmentData", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String attachmentData;

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
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddress(String value) {
        this.address = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setVersion(BigInteger value) {
        this.version = value;
    }

    /**
     * Gets the value of the ddsIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDDSIdentifier() {
        return ddsIdentifier;
    }

    /**
     * Sets the value of the ddsIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDDSIdentifier(String value) {
        this.ddsIdentifier = value;
    }

    /**
     * Gets the value of the attachmentData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachmentData() {
        return attachmentData;
    }

    /**
     * Sets the value of the attachmentData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachmentData(String value) {
        this.attachmentData = value;
    }

}

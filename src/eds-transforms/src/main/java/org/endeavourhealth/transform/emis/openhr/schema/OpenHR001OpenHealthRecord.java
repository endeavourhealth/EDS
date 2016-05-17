
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.OpenHealthRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.OpenHealthRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="creationTime" type="{http://www.e-mis.com/emisopen}dt.DateTime"/>
 *         &lt;element name="author" type="{http://www.e-mis.com/emisopen}OpenHR001.MessageAuthor"/>
 *         &lt;element name="contentSpecification" type="{http://www.e-mis.com/emisopen}OpenHR001.ContentSpecification"/>
 *         &lt;element name="requestMessage" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="adminDomain" type="{http://www.e-mis.com/emisopen}OpenHR001.AdminDomain" minOccurs="0"/>
 *         &lt;element name="healthDomain" type="{http://www.e-mis.com/emisopen}OpenHR001.HealthDomain" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="schemaVersion" type="{http://www.e-mis.com/emisopen}dt.Version" default="1.3.0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.OpenHealthRecord", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "creationTime",
    "author",
    "contentSpecification",
    "requestMessage",
    "adminDomain",
    "healthDomain"
})
public class OpenHR001OpenHealthRecord {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar creationTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected OpenHR001MessageAuthor author;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected OpenHR001ContentSpecification contentSpecification;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String requestMessage;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001AdminDomain adminDomain;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001HealthDomain healthDomain;
    @XmlAttribute(name = "schemaVersion")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String schemaVersion;

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
     * Gets the value of the creationTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreationTime() {
        return creationTime;
    }

    /**
     * Sets the value of the creationTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreationTime(XMLGregorianCalendar value) {
        this.creationTime = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001MessageAuthor }
     *     
     */
    public OpenHR001MessageAuthor getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001MessageAuthor }
     *     
     */
    public void setAuthor(OpenHR001MessageAuthor value) {
        this.author = value;
    }

    /**
     * Gets the value of the contentSpecification property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001ContentSpecification }
     *     
     */
    public OpenHR001ContentSpecification getContentSpecification() {
        return contentSpecification;
    }

    /**
     * Sets the value of the contentSpecification property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001ContentSpecification }
     *     
     */
    public void setContentSpecification(OpenHR001ContentSpecification value) {
        this.contentSpecification = value;
    }

    /**
     * Gets the value of the requestMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestMessage() {
        return requestMessage;
    }

    /**
     * Sets the value of the requestMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestMessage(String value) {
        this.requestMessage = value;
    }

    /**
     * Gets the value of the adminDomain property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001AdminDomain }
     *     
     */
    public OpenHR001AdminDomain getAdminDomain() {
        return adminDomain;
    }

    /**
     * Sets the value of the adminDomain property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001AdminDomain }
     *     
     */
    public void setAdminDomain(OpenHR001AdminDomain value) {
        this.adminDomain = value;
    }

    /**
     * Gets the value of the healthDomain property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001HealthDomain }
     *     
     */
    public OpenHR001HealthDomain getHealthDomain() {
        return healthDomain;
    }

    /**
     * Sets the value of the healthDomain property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001HealthDomain }
     *     
     */
    public void setHealthDomain(OpenHR001HealthDomain value) {
        this.healthDomain = value;
    }

    /**
     * Gets the value of the schemaVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchemaVersion() {
        if (schemaVersion == null) {
            return "1.3.0";
        } else {
            return schemaVersion;
        }
    }

    /**
     * Sets the value of the schemaVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchemaVersion(String value) {
        this.schemaVersion = value;
    }

}

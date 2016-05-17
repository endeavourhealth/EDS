
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.MessageAuthor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.MessageAuthor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="system" type="{http://www.e-mis.com/emisopen}OpenHR001.AuthorSystem"/>
 *         &lt;element name="organisation" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="SDSIdentifier" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="nationalCode" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="20"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="displayName" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.MessageAuthor", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "system",
    "organisation",
    "sdsIdentifier",
    "nationalCode",
    "displayName"
})
public class OpenHR001MessageAuthor {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected OpenHR001AuthorSystem system;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String organisation;
    @XmlElement(name = "SDSIdentifier", namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String sdsIdentifier;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String nationalCode;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String displayName;

    /**
     * Gets the value of the system property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001AuthorSystem }
     *     
     */
    public OpenHR001AuthorSystem getSystem() {
        return system;
    }

    /**
     * Sets the value of the system property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001AuthorSystem }
     *     
     */
    public void setSystem(OpenHR001AuthorSystem value) {
        this.system = value;
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
     * Gets the value of the sdsIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSDSIdentifier() {
        return sdsIdentifier;
    }

    /**
     * Sets the value of the sdsIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSDSIdentifier(String value) {
        this.sdsIdentifier = value;
    }

    /**
     * Gets the value of the nationalCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNationalCode() {
        return nationalCode;
    }

    /**
     * Sets the value of the nationalCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNationalCode(String value) {
        this.nationalCode = value;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

}

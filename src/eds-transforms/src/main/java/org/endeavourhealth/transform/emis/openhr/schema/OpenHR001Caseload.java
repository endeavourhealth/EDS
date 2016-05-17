
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.Caseload complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Caseload">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="title">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="referredOnly" type="{http://www.e-mis.com/emisopen}dt.bool"/>
 *         &lt;element name="includeReferrals" type="{http://www.e-mis.com/emisopen}dt.bool"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Caseload", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "title",
    "referredOnly",
    "includeReferrals"
})
public class OpenHR001Caseload
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String title;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected boolean referredOnly;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected boolean includeReferrals;

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
     * Gets the value of the referredOnly property.
     * 
     */
    public boolean isReferredOnly() {
        return referredOnly;
    }

    /**
     * Sets the value of the referredOnly property.
     * 
     */
    public void setReferredOnly(boolean value) {
        this.referredOnly = value;
    }

    /**
     * Gets the value of the includeReferrals property.
     * 
     */
    public boolean isIncludeReferrals() {
        return includeReferrals;
    }

    /**
     * Sets the value of the includeReferrals property.
     * 
     */
    public void setIncludeReferrals(boolean value) {
        this.includeReferrals = value;
    }

}

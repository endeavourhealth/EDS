
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AuthorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuthorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="User" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="SystemDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuthorType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "user",
    "systemDate"
})
public class AuthorType {

    @XmlElement(name = "User", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType user;
    @XmlElement(name = "SystemDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String systemDate;

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setUser(IdentType value) {
        this.user = value;
    }

    /**
     * Gets the value of the systemDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystemDate() {
        return systemDate;
    }

    /**
     * Sets the value of the systemDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystemDate(String value) {
        this.systemDate = value;
    }

}

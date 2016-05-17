
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
 * <p>Java class for OpenHR001.AuthorisedIssue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.AuthorisedIssue">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="authorisedDate" type="{http://www.e-mis.com/emisopen}dt.Date"/>
 *         &lt;element name="authorisingUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="enteredByUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="authorisedIssues" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.AuthorisedIssue", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "authorisedDate",
    "authorisingUserInRole",
    "enteredByUserInRole",
    "authorisedIssues"
})
public class OpenHR001AuthorisedIssue
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar authorisedDate;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String authorisingUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String enteredByUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected int authorisedIssues;

    /**
     * Gets the value of the authorisedDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAuthorisedDate() {
        return authorisedDate;
    }

    /**
     * Sets the value of the authorisedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAuthorisedDate(XMLGregorianCalendar value) {
        this.authorisedDate = value;
    }

    /**
     * Gets the value of the authorisingUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthorisingUserInRole() {
        return authorisingUserInRole;
    }

    /**
     * Sets the value of the authorisingUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthorisingUserInRole(String value) {
        this.authorisingUserInRole = value;
    }

    /**
     * Gets the value of the enteredByUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnteredByUserInRole() {
        return enteredByUserInRole;
    }

    /**
     * Sets the value of the enteredByUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnteredByUserInRole(String value) {
        this.enteredByUserInRole = value;
    }

    /**
     * Gets the value of the authorisedIssues property.
     * 
     */
    public int getAuthorisedIssues() {
        return authorisedIssues;
    }

    /**
     * Sets the value of the authorisedIssues property.
     * 
     */
    public void setAuthorisedIssues(int value) {
        this.authorisedIssues = value;
    }

}

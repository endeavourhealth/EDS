
package org.endeavourhealth.core.xml.enterprise;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for schedule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="schedule">
 *   &lt;complexContent>
 *     &lt;extension base="{}baseRecord">
 *       &lt;sequence>
 *         &lt;element name="organisation_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="practitioner_id" type="{}uuid" minOccurs="0"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="location" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "schedule", propOrder = {
    "organisationId",
    "practitionerId",
    "date",
    "type",
    "location"
})
public class Schedule
    extends BaseRecord
{

    @XmlElement(name = "organisation_id")
    protected String organisationId;
    @XmlElement(name = "practitioner_id")
    protected String practitionerId;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar date;
    protected String type;
    protected String location;

    /**
     * Gets the value of the organisationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisationId() {
        return organisationId;
    }

    /**
     * Sets the value of the organisationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisationId(String value) {
        this.organisationId = value;
    }

    /**
     * Gets the value of the practitionerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPractitionerId() {
        return practitionerId;
    }

    /**
     * Sets the value of the practitionerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPractitionerId(String value) {
        this.practitionerId = value;
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDate(XMLGregorianCalendar value) {
        this.date = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

}

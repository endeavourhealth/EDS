
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
 * <p>Java class for OpenHR001.MedicationCancel complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.MedicationCancel">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="cancelledByUserInRole" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="cancellationDateTime" type="{http://www.e-mis.com/emisopen}dt.DateTime"/>
 *         &lt;element name="cancellationText" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="linkedEvent" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.MedicationCancel", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "cancelledByUserInRole",
    "cancellationDateTime",
    "cancellationText",
    "linkedEvent"
})
public class OpenHR001MedicationCancel {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String cancelledByUserInRole;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar cancellationDateTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String cancellationText;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String linkedEvent;

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
     * Gets the value of the cancelledByUserInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCancelledByUserInRole() {
        return cancelledByUserInRole;
    }

    /**
     * Sets the value of the cancelledByUserInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCancelledByUserInRole(String value) {
        this.cancelledByUserInRole = value;
    }

    /**
     * Gets the value of the cancellationDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCancellationDateTime() {
        return cancellationDateTime;
    }

    /**
     * Sets the value of the cancellationDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCancellationDateTime(XMLGregorianCalendar value) {
        this.cancellationDateTime = value;
    }

    /**
     * Gets the value of the cancellationText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCancellationText() {
        return cancellationText;
    }

    /**
     * Sets the value of the cancellationText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCancellationText(String value) {
        this.cancellationText = value;
    }

    /**
     * Gets the value of the linkedEvent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkedEvent() {
        return linkedEvent;
    }

    /**
     * Sets the value of the linkedEvent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkedEvent(String value) {
        this.linkedEvent = value;
    }

}

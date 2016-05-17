
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.ProblemEventLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.ProblemEventLink">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="eventType" type="{http://www.e-mis.com/emisopen}voc.EventType" minOccurs="0"/>
 *         &lt;element name="eventLinkType" type="{http://www.e-mis.com/emisopen}voc.ProblemEventLinkType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.ProblemEventLink", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "eventType",
    "eventLinkType"
})
public class OpenHR001ProblemEventLink
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocEventType eventType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocProblemEventLinkType eventLinkType;

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
     * Gets the value of the eventType property.
     * 
     * @return
     *     possible object is
     *     {@link VocEventType }
     *     
     */
    public VocEventType getEventType() {
        return eventType;
    }

    /**
     * Sets the value of the eventType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocEventType }
     *     
     */
    public void setEventType(VocEventType value) {
        this.eventType = value;
    }

    /**
     * Gets the value of the eventLinkType property.
     * 
     * @return
     *     possible object is
     *     {@link VocProblemEventLinkType }
     *     
     */
    public VocProblemEventLinkType getEventLinkType() {
        return eventLinkType;
    }

    /**
     * Sets the value of the eventLinkType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocProblemEventLinkType }
     *     
     */
    public void setEventLinkType(VocProblemEventLinkType value) {
        this.eventLinkType = value;
    }

}

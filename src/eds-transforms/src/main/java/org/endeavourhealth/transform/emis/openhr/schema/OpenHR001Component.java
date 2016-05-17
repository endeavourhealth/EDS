
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.Component complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Component">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="event" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *         &lt;element name="problemPage" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" minOccurs="0"/>
 *         &lt;element name="heading" type="{http://www.e-mis.com/emisopen}dt.Code" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Component", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "event",
    "problemPage",
    "heading"
})
public class OpenHR001Component
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String event;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "unsignedByte")
    protected Short problemPage;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtCode heading;

    /**
     * Gets the value of the event property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvent() {
        return event;
    }

    /**
     * Sets the value of the event property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvent(String value) {
        this.event = value;
    }

    /**
     * Gets the value of the problemPage property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getProblemPage() {
        return problemPage;
    }

    /**
     * Sets the value of the problemPage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setProblemPage(Short value) {
        this.problemPage = value;
    }

    /**
     * Gets the value of the heading property.
     * 
     * @return
     *     possible object is
     *     {@link DtCode }
     *     
     */
    public DtCode getHeading() {
        return heading;
    }

    /**
     * Sets the value of the heading property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCode }
     *     
     */
    public void setHeading(DtCode value) {
        this.heading = value;
    }

}

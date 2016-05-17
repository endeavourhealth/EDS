
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.TextValue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.TextValue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="range" type="{http://www.e-mis.com/emisopen}OpenHR001.Range" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.TextValue", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "value",
    "range"
})
public class OpenHR001TextValue {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String value;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001Range range;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the range property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001Range }
     *     
     */
    public OpenHR001Range getRange() {
        return range;
    }

    /**
     * Sets the value of the range property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001Range }
     *     
     */
    public void setRange(OpenHR001Range value) {
        this.range = value;
    }

}

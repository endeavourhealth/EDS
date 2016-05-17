
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.NumericRange complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.NumericRange">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="low" type="{http://www.e-mis.com/emisopen}OpenHR001.RangeValue" minOccurs="0"/>
 *         &lt;element name="high" type="{http://www.e-mis.com/emisopen}OpenHR001.RangeValue" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.NumericRange", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "low",
    "high"
})
public class OpenHR001NumericRange {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001RangeValue low;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001RangeValue high;

    /**
     * Gets the value of the low property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001RangeValue }
     *     
     */
    public OpenHR001RangeValue getLow() {
        return low;
    }

    /**
     * Sets the value of the low property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001RangeValue }
     *     
     */
    public void setLow(OpenHR001RangeValue value) {
        this.low = value;
    }

    /**
     * Gets the value of the high property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001RangeValue }
     *     
     */
    public OpenHR001RangeValue getHigh() {
        return high;
    }

    /**
     * Sets the value of the high property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001RangeValue }
     *     
     */
    public void setHigh(OpenHR001RangeValue value) {
        this.high = value;
    }

}

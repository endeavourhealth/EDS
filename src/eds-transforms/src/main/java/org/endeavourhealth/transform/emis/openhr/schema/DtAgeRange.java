
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dt.AgeRange complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.AgeRange">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="low" type="{http://www.e-mis.com/emisopen}dt.Age"/>
 *         &lt;element name="high" type="{http://www.e-mis.com/emisopen}dt.Age"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dt.AgeRange", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "low",
    "high"
})
public class DtAgeRange {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected DtAge low;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected DtAge high;

    /**
     * Gets the value of the low property.
     * 
     * @return
     *     possible object is
     *     {@link DtAge }
     *     
     */
    public DtAge getLow() {
        return low;
    }

    /**
     * Sets the value of the low property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtAge }
     *     
     */
    public void setLow(DtAge value) {
        this.low = value;
    }

    /**
     * Gets the value of the high property.
     * 
     * @return
     *     possible object is
     *     {@link DtAge }
     *     
     */
    public DtAge getHigh() {
        return high;
    }

    /**
     * Sets the value of the high property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtAge }
     *     
     */
    public void setHigh(DtAge value) {
        this.high = value;
    }

}


package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents a duration of time.
 * 
 * <p>Java class for dt.Duration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.Duration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="unit" use="required" type="{http://www.e-mis.com/emisopen}voc.TimeUnit" />
 *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dt.Duration", namespace = "http://www.e-mis.com/emisopen")
public class DtDuration {

    @XmlAttribute(name = "unit", required = true)
    protected VocTimeUnit unit;
    @XmlAttribute(name = "value", required = true)
    protected int value;

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link VocTimeUnit }
     *     
     */
    public VocTimeUnit getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocTimeUnit }
     *     
     */
    public void setUnit(VocTimeUnit value) {
        this.unit = value;
    }

    /**
     * Gets the value of the value property.
     * 
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     */
    public void setValue(int value) {
        this.value = value;
    }

}

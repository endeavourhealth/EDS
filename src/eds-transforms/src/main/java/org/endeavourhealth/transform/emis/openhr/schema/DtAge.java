
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents a duration of time.
 * 
 * <p>Java class for dt.Age complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.Age">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="unit" use="required" type="{http://www.e-mis.com/emisopen}voc.AgeUnit" />
 *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}short" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dt.Age", namespace = "http://www.e-mis.com/emisopen")
public class DtAge {

    @XmlAttribute(name = "unit", required = true)
    protected VocAgeUnit unit;
    @XmlAttribute(name = "value", required = true)
    protected short value;

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link VocAgeUnit }
     *     
     */
    public VocAgeUnit getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocAgeUnit }
     *     
     */
    public void setUnit(VocAgeUnit value) {
        this.unit = value;
    }

    /**
     * Gets the value of the value property.
     * 
     */
    public short getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     */
    public void setValue(short value) {
        this.value = value;
    }

}

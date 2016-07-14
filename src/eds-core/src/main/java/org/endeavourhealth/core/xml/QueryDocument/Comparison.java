
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for comparison complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="comparison">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="valueFrom" type="{}valueFrom"/>
 *         &lt;element name="valueTo" type="{}valueTo"/>
 *         &lt;element name="valueRange" type="{}valueRange"/>
 *         &lt;element name="valueEqualTo" type="{}value"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "comparison", propOrder = {
    "valueFrom",
    "valueTo",
    "valueRange",
    "valueEqualTo"
})
public class Comparison {

    protected ValueFrom valueFrom;
    protected ValueTo valueTo;
    protected ValueRange valueRange;
    protected Value valueEqualTo;

    /**
     * Gets the value of the valueFrom property.
     * 
     * @return
     *     possible object is
     *     {@link ValueFrom }
     *     
     */
    public ValueFrom getValueFrom() {
        return valueFrom;
    }

    /**
     * Sets the value of the valueFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueFrom }
     *     
     */
    public void setValueFrom(ValueFrom value) {
        this.valueFrom = value;
    }

    /**
     * Gets the value of the valueTo property.
     * 
     * @return
     *     possible object is
     *     {@link ValueTo }
     *     
     */
    public ValueTo getValueTo() {
        return valueTo;
    }

    /**
     * Sets the value of the valueTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueTo }
     *     
     */
    public void setValueTo(ValueTo value) {
        this.valueTo = value;
    }

    /**
     * Gets the value of the valueRange property.
     * 
     * @return
     *     possible object is
     *     {@link ValueRange }
     *     
     */
    public ValueRange getValueRange() {
        return valueRange;
    }

    /**
     * Sets the value of the valueRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueRange }
     *     
     */
    public void setValueRange(ValueRange value) {
        this.valueRange = value;
    }

    /**
     * Gets the value of the valueEqualTo property.
     * 
     * @return
     *     possible object is
     *     {@link Value }
     *     
     */
    public Value getValueEqualTo() {
        return valueEqualTo;
    }

    /**
     * Sets the value of the valueEqualTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Value }
     *     
     */
    public void setValueEqualTo(Value value) {
        this.valueEqualTo = value;
    }

}

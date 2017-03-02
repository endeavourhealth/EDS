
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for valueRange complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="valueRange">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="valueFrom" type="{}valueFrom"/>
 *         &lt;element name="valueTo" type="{}valueTo"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "valueRange", propOrder = {
    "valueFrom",
    "valueTo"
})
public class ValueRange {

    @XmlElement(required = true)
    protected ValueFrom valueFrom;
    @XmlElement(required = true)
    protected ValueTo valueTo;

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

}


package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for value complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="value">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="constant" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="parameter" type="{}parameterType"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="absoluteUnit" type="{}valueAbsoluteUnit"/>
 *           &lt;element name="relativeUnit" type="{}valueRelativeUnit"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "value", propOrder = {
    "constant",
    "parameter",
    "absoluteUnit",
    "relativeUnit"
})
@XmlSeeAlso({
    ValueTo.class,
    ValueFrom.class
})
public class Value {

    protected String constant;
    protected ParameterType parameter;
    @XmlSchemaType(name = "string")
    protected ValueAbsoluteUnit absoluteUnit;
    @XmlSchemaType(name = "string")
    protected ValueRelativeUnit relativeUnit;

    /**
     * Gets the value of the constant property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConstant() {
        return constant;
    }

    /**
     * Sets the value of the constant property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConstant(String value) {
        this.constant = value;
    }

    /**
     * Gets the value of the parameter property.
     * 
     * @return
     *     possible object is
     *     {@link ParameterType }
     *     
     */
    public ParameterType getParameter() {
        return parameter;
    }

    /**
     * Sets the value of the parameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParameterType }
     *     
     */
    public void setParameter(ParameterType value) {
        this.parameter = value;
    }

    /**
     * Gets the value of the absoluteUnit property.
     * 
     * @return
     *     possible object is
     *     {@link ValueAbsoluteUnit }
     *     
     */
    public ValueAbsoluteUnit getAbsoluteUnit() {
        return absoluteUnit;
    }

    /**
     * Sets the value of the absoluteUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueAbsoluteUnit }
     *     
     */
    public void setAbsoluteUnit(ValueAbsoluteUnit value) {
        this.absoluteUnit = value;
    }

    /**
     * Gets the value of the relativeUnit property.
     * 
     * @return
     *     possible object is
     *     {@link ValueRelativeUnit }
     *     
     */
    public ValueRelativeUnit getRelativeUnit() {
        return relativeUnit;
    }

    /**
     * Sets the value of the relativeUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueRelativeUnit }
     *     
     */
    public void setRelativeUnit(ValueRelativeUnit value) {
        this.relativeUnit = value;
    }

}

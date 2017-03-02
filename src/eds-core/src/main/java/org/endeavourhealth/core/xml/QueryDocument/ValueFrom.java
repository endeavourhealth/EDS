
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for valueFrom complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="valueFrom">
 *   &lt;complexContent>
 *     &lt;extension base="{}value">
 *       &lt;sequence>
 *         &lt;element name="operator" type="{}valueFromOperator"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "valueFrom", propOrder = {
    "operator"
})
public class ValueFrom
    extends Value
{

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ValueFromOperator operator;

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link ValueFromOperator }
     *     
     */
    public ValueFromOperator getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueFromOperator }
     *     
     */
    public void setOperator(ValueFromOperator value) {
        this.operator = value;
    }

}

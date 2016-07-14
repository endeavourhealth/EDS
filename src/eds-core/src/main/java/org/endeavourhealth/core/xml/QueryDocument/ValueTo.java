
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for valueTo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="valueTo">
 *   &lt;complexContent>
 *     &lt;extension base="{}value">
 *       &lt;sequence>
 *         &lt;element name="operator" type="{}valueToOperator"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "valueTo", propOrder = {
    "operator"
})
public class ValueTo
    extends Value
{

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ValueToOperator operator;

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link ValueToOperator }
     *     
     */
    public ValueToOperator getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueToOperator }
     *     
     */
    public void setOperator(ValueToOperator value) {
        this.operator = value;
    }

}

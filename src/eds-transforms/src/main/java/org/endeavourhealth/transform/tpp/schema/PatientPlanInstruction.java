
package org.endeavourhealth.transform.tpp.schema;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PatientPlanInstruction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PatientPlanInstruction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InstructionText" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Order" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PatientPlanInstruction", propOrder = {
    "instructionText",
    "order"
})
public class PatientPlanInstruction {

    @XmlElement(name = "InstructionText", required = true)
    protected String instructionText;
    @XmlElement(name = "Order", required = true)
    protected BigInteger order;

    /**
     * Gets the value of the instructionText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstructionText() {
        return instructionText;
    }

    /**
     * Sets the value of the instructionText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstructionText(String value) {
        this.instructionText = value;
    }

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setOrder(BigInteger value) {
        this.order = value;
    }

}

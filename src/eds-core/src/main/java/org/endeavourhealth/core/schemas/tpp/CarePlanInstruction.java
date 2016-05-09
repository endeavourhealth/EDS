
package org.endeavourhealth.core.schemas.tpp;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CarePlanInstruction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CarePlanInstruction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InstructionText" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Order" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="Responsibility">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Nurse"/>
 *               &lt;enumeration value="Patient"/>
 *               &lt;enumeration value="Carer"/>
 *               &lt;enumeration value="Therapist"/>
 *               &lt;enumeration value="Assistant"/>
 *               &lt;enumeration value="Practitioner"/>
 *               &lt;enumeration value="Social Care Co-ordinator"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CarePlanInstruction", propOrder = {
    "instructionText",
    "order",
    "responsibility"
})
public class CarePlanInstruction {

    @XmlElement(name = "InstructionText", required = true)
    protected String instructionText;
    @XmlElement(name = "Order", required = true)
    protected BigInteger order;
    @XmlElement(name = "Responsibility", required = true)
    protected String responsibility;

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

    /**
     * Gets the value of the responsibility property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResponsibility() {
        return responsibility;
    }

    /**
     * Sets the value of the responsibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResponsibility(String value) {
        this.responsibility = value;
    }

}

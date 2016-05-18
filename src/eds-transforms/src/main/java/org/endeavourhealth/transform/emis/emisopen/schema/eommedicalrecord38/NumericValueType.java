
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Handles numeric values
 * 
 * <p>Java class for NumericValueType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NumericValueType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Operator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="Units" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MinRangeOperator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MaximumRangeOperator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NumericMinimum" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="NumericMaximum" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NumericValueType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "operator",
    "value",
    "units",
    "minRangeOperator",
    "maximumRangeOperator",
    "numericMinimum",
    "numericMaximum"
})
public class NumericValueType {

    @XmlElement(name = "Operator", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String operator;
    @XmlElement(name = "Value", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Double value;
    @XmlElement(name = "Units", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String units;
    @XmlElement(name = "MinRangeOperator", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String minRangeOperator;
    @XmlElement(name = "MaximumRangeOperator", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String maximumRangeOperator;
    @XmlElement(name = "NumericMinimum", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Double numericMinimum;
    @XmlElement(name = "NumericMaximum", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Double numericMaximum;

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperator(String value) {
        this.operator = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * Gets the value of the units property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnits(String value) {
        this.units = value;
    }

    /**
     * Gets the value of the minRangeOperator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinRangeOperator() {
        return minRangeOperator;
    }

    /**
     * Sets the value of the minRangeOperator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinRangeOperator(String value) {
        this.minRangeOperator = value;
    }

    /**
     * Gets the value of the maximumRangeOperator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaximumRangeOperator() {
        return maximumRangeOperator;
    }

    /**
     * Sets the value of the maximumRangeOperator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaximumRangeOperator(String value) {
        this.maximumRangeOperator = value;
    }

    /**
     * Gets the value of the numericMinimum property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getNumericMinimum() {
        return numericMinimum;
    }

    /**
     * Sets the value of the numericMinimum property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setNumericMinimum(Double value) {
        this.numericMinimum = value;
    }

    /**
     * Gets the value of the numericMaximum property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getNumericMaximum() {
        return numericMaximum;
    }

    /**
     * Sets the value of the numericMaximum property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setNumericMaximum(Double value) {
        this.numericMaximum = value;
    }

}

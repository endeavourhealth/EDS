
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DrugDeliveryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DrugDeliveryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DoseQuantity" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="DoseUnits" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Frequency" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="FrequencyUnits" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DailyDose" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="DailyDoseUnits" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DrugDeliveryType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "doseQuantity",
    "doseUnits",
    "frequency",
    "frequencyUnits",
    "dailyDose",
    "dailyDoseUnits"
})
public class DrugDeliveryType {

    @XmlElement(name = "DoseQuantity", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Float doseQuantity;
    @XmlElement(name = "DoseUnits", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String doseUnits;
    @XmlElement(name = "Frequency", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger frequency;
    @XmlElement(name = "FrequencyUnits", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String frequencyUnits;
    @XmlElement(name = "DailyDose", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Float dailyDose;
    @XmlElement(name = "DailyDoseUnits", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String dailyDoseUnits;

    /**
     * Gets the value of the doseQuantity property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getDoseQuantity() {
        return doseQuantity;
    }

    /**
     * Sets the value of the doseQuantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setDoseQuantity(Float value) {
        this.doseQuantity = value;
    }

    /**
     * Gets the value of the doseUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDoseUnits() {
        return doseUnits;
    }

    /**
     * Sets the value of the doseUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDoseUnits(String value) {
        this.doseUnits = value;
    }

    /**
     * Gets the value of the frequency property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFrequency() {
        return frequency;
    }

    /**
     * Sets the value of the frequency property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFrequency(BigInteger value) {
        this.frequency = value;
    }

    /**
     * Gets the value of the frequencyUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFrequencyUnits() {
        return frequencyUnits;
    }

    /**
     * Sets the value of the frequencyUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrequencyUnits(String value) {
        this.frequencyUnits = value;
    }

    /**
     * Gets the value of the dailyDose property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getDailyDose() {
        return dailyDose;
    }

    /**
     * Sets the value of the dailyDose property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setDailyDose(Float value) {
        this.dailyDose = value;
    }

    /**
     * Gets the value of the dailyDoseUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDailyDoseUnits() {
        return dailyDoseUnits;
    }

    /**
     * Sets the value of the dailyDoseUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDailyDoseUnits(String value) {
        this.dailyDoseUnits = value;
    }

}

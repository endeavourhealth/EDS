
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Community Care Aims/Goals
 * 
 * <p>Java class for CareAimType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CareAimType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PKId" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType"/>
 *         &lt;element name="Goal" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TargetDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ExpectedValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ActualValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CareAimType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "pkId",
    "goal",
    "targetDate",
    "expectedValue",
    "actualValue"
})
public class CareAimType {

    @XmlElement(name = "PKId", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected IdentType pkId;
    @XmlElement(name = "Goal", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String goal;
    @XmlElement(name = "TargetDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar targetDate;
    @XmlElement(name = "ExpectedValue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String expectedValue;
    @XmlElement(name = "ActualValue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String actualValue;

    /**
     * Gets the value of the pkId property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getPKId() {
        return pkId;
    }

    /**
     * Sets the value of the pkId property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setPKId(IdentType value) {
        this.pkId = value;
    }

    /**
     * Gets the value of the goal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGoal() {
        return goal;
    }

    /**
     * Sets the value of the goal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGoal(String value) {
        this.goal = value;
    }

    /**
     * Gets the value of the targetDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTargetDate() {
        return targetDate;
    }

    /**
     * Sets the value of the targetDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTargetDate(XMLGregorianCalendar value) {
        this.targetDate = value;
    }

    /**
     * Gets the value of the expectedValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpectedValue() {
        return expectedValue;
    }

    /**
     * Sets the value of the expectedValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpectedValue(String value) {
        this.expectedValue = value;
    }

    /**
     * Gets the value of the actualValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActualValue() {
        return actualValue;
    }

    /**
     * Sets the value of the actualValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActualValue(String value) {
        this.actualValue = value;
    }

}

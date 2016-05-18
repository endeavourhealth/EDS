
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EventType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EventType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}CodedItemBaseType">
 *       &lt;sequence>
 *         &lt;element name="GroupID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="Episodicity" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}byte">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *               &lt;enumeration value="3"/>
 *               &lt;enumeration value="4"/>
 *               &lt;enumeration value="5"/>
 *               &lt;enumeration value="6"/>
 *               &lt;enumeration value="7"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="NumericValue" type="{http://www.e-mis.com/emisopen/MedicalRecord}NumericValueType" minOccurs="0"/>
 *         &lt;element name="TextValue" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="Abnormal" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="GMS" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="EventType" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *               &lt;enumeration value="3"/>
 *               &lt;enumeration value="4"/>
 *               &lt;enumeration value="5"/>
 *               &lt;enumeration value="6"/>
 *               &lt;enumeration value="7"/>
 *               &lt;enumeration value="8"/>
 *               &lt;enumeration value="9"/>
 *               &lt;enumeration value="10"/>
 *               &lt;enumeration value="11"/>
 *               &lt;enumeration value="12"/>
 *               &lt;enumeration value="13"/>
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="14"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="TemplateID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="TemplateInstanceID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="TemplateComponentName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="QualifiedTerm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="QualifiedCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "groupID",
    "episodicity",
    "numericValue",
    "textValue",
    "abnormal",
    "gms",
    "eventType",
    "templateID",
    "templateInstanceID",
    "templateComponentName",
    "qualifiedTerm",
    "qualifiedCode"
})
public class EventType
    extends CodedItemBaseType
{

    @XmlElement(name = "GroupID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger groupID;
    @XmlElement(name = "Episodicity", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Byte episodicity;
    @XmlElement(name = "NumericValue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected NumericValueType numericValue;
    @XmlElement(name = "TextValue", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected Object textValue;
    @XmlElement(name = "Abnormal", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger abnormal;
    @XmlElement(name = "GMS", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger gms;
    @XmlElement(name = "EventType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger eventType;
    @XmlElement(name = "TemplateID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger templateID;
    @XmlElement(name = "TemplateInstanceID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger templateInstanceID;
    @XmlElement(name = "TemplateComponentName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String templateComponentName;
    @XmlElement(name = "QualifiedTerm", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String qualifiedTerm;
    @XmlElement(name = "QualifiedCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String qualifiedCode;

    /**
     * Gets the value of the groupID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getGroupID() {
        return groupID;
    }

    /**
     * Sets the value of the groupID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setGroupID(BigInteger value) {
        this.groupID = value;
    }

    /**
     * Gets the value of the episodicity property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getEpisodicity() {
        return episodicity;
    }

    /**
     * Sets the value of the episodicity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setEpisodicity(Byte value) {
        this.episodicity = value;
    }

    /**
     * Gets the value of the numericValue property.
     * 
     * @return
     *     possible object is
     *     {@link NumericValueType }
     *     
     */
    public NumericValueType getNumericValue() {
        return numericValue;
    }

    /**
     * Sets the value of the numericValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link NumericValueType }
     *     
     */
    public void setNumericValue(NumericValueType value) {
        this.numericValue = value;
    }

    /**
     * Gets the value of the textValue property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getTextValue() {
        return textValue;
    }

    /**
     * Sets the value of the textValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setTextValue(Object value) {
        this.textValue = value;
    }

    /**
     * Gets the value of the abnormal property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAbnormal() {
        return abnormal;
    }

    /**
     * Sets the value of the abnormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAbnormal(BigInteger value) {
        this.abnormal = value;
    }

    /**
     * Gets the value of the gms property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getGMS() {
        return gms;
    }

    /**
     * Sets the value of the gms property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setGMS(BigInteger value) {
        this.gms = value;
    }

    /**
     * Gets the value of the eventType property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getEventType() {
        return eventType;
    }

    /**
     * Sets the value of the eventType property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setEventType(BigInteger value) {
        this.eventType = value;
    }

    /**
     * Gets the value of the templateID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTemplateID() {
        return templateID;
    }

    /**
     * Sets the value of the templateID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTemplateID(BigInteger value) {
        this.templateID = value;
    }

    /**
     * Gets the value of the templateInstanceID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTemplateInstanceID() {
        return templateInstanceID;
    }

    /**
     * Sets the value of the templateInstanceID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTemplateInstanceID(BigInteger value) {
        this.templateInstanceID = value;
    }

    /**
     * Gets the value of the templateComponentName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateComponentName() {
        return templateComponentName;
    }

    /**
     * Sets the value of the templateComponentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateComponentName(String value) {
        this.templateComponentName = value;
    }

    /**
     * Gets the value of the qualifiedTerm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQualifiedTerm() {
        return qualifiedTerm;
    }

    /**
     * Sets the value of the qualifiedTerm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQualifiedTerm(String value) {
        this.qualifiedTerm = value;
    }

    /**
     * Gets the value of the qualifiedCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQualifiedCode() {
        return qualifiedCode;
    }

    /**
     * Sets the value of the qualifiedCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQualifiedCode(String value) {
        this.qualifiedCode = value;
    }

}

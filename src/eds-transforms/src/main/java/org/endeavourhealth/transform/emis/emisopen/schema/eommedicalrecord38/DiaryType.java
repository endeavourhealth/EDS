
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DiaryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DiaryType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}CodedItemBaseType">
 *       &lt;sequence>
 *         &lt;element name="TemplateID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="TemplateInstanceID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="TemplateComponentName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DurationTerm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Reminder" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ReminderType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LocationTypeID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DiaryType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "templateID",
    "templateInstanceID",
    "templateComponentName",
    "durationTerm",
    "reminder",
    "reminderType",
    "locationTypeID"
})
public class DiaryType
    extends CodedItemBaseType
{

    @XmlElement(name = "TemplateID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger templateID;
    @XmlElement(name = "TemplateInstanceID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger templateInstanceID;
    @XmlElement(name = "TemplateComponentName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String templateComponentName;
    @XmlElement(name = "DurationTerm", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String durationTerm;
    @XmlElement(name = "Reminder", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String reminder;
    @XmlElement(name = "ReminderType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String reminderType;
    @XmlElement(name = "LocationTypeID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType locationTypeID;

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
     * Gets the value of the durationTerm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDurationTerm() {
        return durationTerm;
    }

    /**
     * Sets the value of the durationTerm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDurationTerm(String value) {
        this.durationTerm = value;
    }

    /**
     * Gets the value of the reminder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReminder() {
        return reminder;
    }

    /**
     * Sets the value of the reminder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReminder(String value) {
        this.reminder = value;
    }

    /**
     * Gets the value of the reminderType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReminderType() {
        return reminderType;
    }

    /**
     * Sets the value of the reminderType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReminderType(String value) {
        this.reminderType = value;
    }

    /**
     * Gets the value of the locationTypeID property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getLocationTypeID() {
        return locationTypeID;
    }

    /**
     * Sets the value of the locationTypeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setLocationTypeID(IdentType value) {
        this.locationTypeID = value;
    }

}

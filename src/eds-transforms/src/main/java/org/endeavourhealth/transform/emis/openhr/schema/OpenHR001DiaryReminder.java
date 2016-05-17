
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.DiaryReminder complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.DiaryReminder">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="reminderType" type="{http://www.e-mis.com/emisopen}voc.ReminderType"/>
 *         &lt;element name="reminderTime" type="{http://www.e-mis.com/emisopen}dt.DateTime"/>
 *         &lt;element name="userInRole" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.DiaryReminder", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "reminderType",
    "reminderTime",
    "userInRole"
})
public class OpenHR001DiaryReminder
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocReminderType reminderType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar reminderTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String userInRole;

    /**
     * Gets the value of the reminderType property.
     * 
     * @return
     *     possible object is
     *     {@link VocReminderType }
     *     
     */
    public VocReminderType getReminderType() {
        return reminderType;
    }

    /**
     * Sets the value of the reminderType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocReminderType }
     *     
     */
    public void setReminderType(VocReminderType value) {
        this.reminderType = value;
    }

    /**
     * Gets the value of the reminderTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getReminderTime() {
        return reminderTime;
    }

    /**
     * Sets the value of the reminderTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setReminderTime(XMLGregorianCalendar value) {
        this.reminderTime = value;
    }

    /**
     * Gets the value of the userInRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserInRole() {
        return userInRole;
    }

    /**
     * Sets the value of the userInRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserInRole(String value) {
        this.userInRole = value;
    }

}

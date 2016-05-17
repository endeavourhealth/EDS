
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.Diary complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Diary">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="durationTerm" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="locationType" type="{http://www.e-mis.com/emisopen}OpenHR001.LocationType" minOccurs="0"/>
 *         &lt;element name="reminder" type="{http://www.e-mis.com/emisopen}OpenHR001.DiaryReminder" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Diary", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "durationTerm",
    "locationType",
    "reminder"
})
public class OpenHR001Diary {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected String durationTerm;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001LocationType locationType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001DiaryReminder> reminder;

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
     * Gets the value of the locationType property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001LocationType }
     *     
     */
    public OpenHR001LocationType getLocationType() {
        return locationType;
    }

    /**
     * Sets the value of the locationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001LocationType }
     *     
     */
    public void setLocationType(OpenHR001LocationType value) {
        this.locationType = value;
    }

    /**
     * Gets the value of the reminder property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reminder property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReminder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001DiaryReminder }
     * 
     * 
     */
    public List<OpenHR001DiaryReminder> getReminder() {
        if (reminder == null) {
            reminder = new ArrayList<OpenHR001DiaryReminder>();
        }
        return this.reminder;
    }

}

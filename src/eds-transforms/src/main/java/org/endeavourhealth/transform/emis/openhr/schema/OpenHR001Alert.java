
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OpenHR001.Alert complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Alert">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="triggerBookSlot" type="{http://www.e-mis.com/emisopen}dt.bool"/>
 *         &lt;element name="triggerAriveSwap" type="{http://www.e-mis.com/emisopen}dt.bool"/>
 *         &lt;element name="showConsultation" type="{http://www.e-mis.com/emisopen}dt.bool"/>
 *         &lt;element name="expires" type="{http://www.e-mis.com/emisopen}dt.Date" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Alert", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "triggerBookSlot",
    "triggerAriveSwap",
    "showConsultation",
    "expires"
})
public class OpenHR001Alert {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected boolean triggerBookSlot;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected boolean triggerAriveSwap;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected boolean showConsultation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar expires;

    /**
     * Gets the value of the triggerBookSlot property.
     * 
     */
    public boolean isTriggerBookSlot() {
        return triggerBookSlot;
    }

    /**
     * Sets the value of the triggerBookSlot property.
     * 
     */
    public void setTriggerBookSlot(boolean value) {
        this.triggerBookSlot = value;
    }

    /**
     * Gets the value of the triggerAriveSwap property.
     * 
     */
    public boolean isTriggerAriveSwap() {
        return triggerAriveSwap;
    }

    /**
     * Sets the value of the triggerAriveSwap property.
     * 
     */
    public void setTriggerAriveSwap(boolean value) {
        this.triggerAriveSwap = value;
    }

    /**
     * Gets the value of the showConsultation property.
     * 
     */
    public boolean isShowConsultation() {
        return showConsultation;
    }

    /**
     * Sets the value of the showConsultation property.
     * 
     */
    public void setShowConsultation(boolean value) {
        this.showConsultation = value;
    }

    /**
     * Gets the value of the expires property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getExpires() {
        return expires;
    }

    /**
     * Sets the value of the expires property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setExpires(XMLGregorianCalendar value) {
        this.expires = value;
    }

}

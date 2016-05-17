
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.ObservationValue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.ObservationValue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="availableRange" type="{http://www.e-mis.com/emisopen}OpenHR001.Range" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="numeric" type="{http://www.e-mis.com/emisopen}OpenHR001.NumericValue"/>
 *           &lt;element name="text" type="{http://www.e-mis.com/emisopen}OpenHR001.TextValue"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.ObservationValue", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "availableRange",
    "numeric",
    "text"
})
public class OpenHR001ObservationValue {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<OpenHR001Range> availableRange;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001NumericValue numeric;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected OpenHR001TextValue text;

    /**
     * Gets the value of the availableRange property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the availableRange property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAvailableRange().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpenHR001Range }
     * 
     * 
     */
    public List<OpenHR001Range> getAvailableRange() {
        if (availableRange == null) {
            availableRange = new ArrayList<OpenHR001Range>();
        }
        return this.availableRange;
    }

    /**
     * Gets the value of the numeric property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001NumericValue }
     *     
     */
    public OpenHR001NumericValue getNumeric() {
        return numeric;
    }

    /**
     * Sets the value of the numeric property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001NumericValue }
     *     
     */
    public void setNumeric(OpenHR001NumericValue value) {
        this.numeric = value;
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHR001TextValue }
     *     
     */
    public OpenHR001TextValue getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHR001TextValue }
     *     
     */
    public void setText(OpenHR001TextValue value) {
        this.text = value;
    }

}

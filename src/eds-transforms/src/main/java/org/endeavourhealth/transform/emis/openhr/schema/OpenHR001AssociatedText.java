
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.AssociatedText complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.AssociatedText">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="associatedTextType" use="required" type="{http://www.e-mis.com/emisopen}voc.AssociatedTextType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.AssociatedText", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "value"
})
@XmlSeeAlso({
    OpenHR001Event.AssociatedText.class
})
public class OpenHR001AssociatedText {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String value;
    @XmlAttribute(name = "associatedTextType", required = true)
    protected VocAssociatedTextType associatedTextType;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the associatedTextType property.
     * 
     * @return
     *     possible object is
     *     {@link VocAssociatedTextType }
     *     
     */
    public VocAssociatedTextType getAssociatedTextType() {
        return associatedTextType;
    }

    /**
     * Sets the value of the associatedTextType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocAssociatedTextType }
     *     
     */
    public void setAssociatedTextType(VocAssociatedTextType value) {
        this.associatedTextType = value;
    }

}

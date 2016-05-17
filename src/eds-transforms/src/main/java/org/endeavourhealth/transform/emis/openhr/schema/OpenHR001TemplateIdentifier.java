
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.TemplateIdentifier complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.TemplateIdentifier">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="instance" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *         &lt;element name="inputPrompt" type="{http://www.e-mis.com/emisopen}dt.uid" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.TemplateIdentifier", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "id",
    "instance",
    "inputPrompt"
})
public class OpenHR001TemplateIdentifier {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String instance;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String inputPrompt;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the instance property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstance() {
        return instance;
    }

    /**
     * Sets the value of the instance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstance(String value) {
        this.instance = value;
    }

    /**
     * Gets the value of the inputPrompt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInputPrompt() {
        return inputPrompt;
    }

    /**
     * Sets the value of the inputPrompt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInputPrompt(String value) {
        this.inputPrompt = value;
    }

}

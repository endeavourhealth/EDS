
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.Annotations complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.Annotations">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="annotation" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="imageGUID" type="{http://www.e-mis.com/emisopen}dt.uid"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.Annotations", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "annotation",
    "imageGUID"
})
public class OpenHR001Annotations {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String annotation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String imageGUID;

    /**
     * Gets the value of the annotation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnnotation() {
        return annotation;
    }

    /**
     * Sets the value of the annotation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnnotation(String value) {
        this.annotation = value;
    }

    /**
     * Gets the value of the imageGUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageGUID() {
        return imageGUID;
    }

    /**
     * Sets the value of the imageGUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageGUID(String value) {
        this.imageGUID = value;
    }

}

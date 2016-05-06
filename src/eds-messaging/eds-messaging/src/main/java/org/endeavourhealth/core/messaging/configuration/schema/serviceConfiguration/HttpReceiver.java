
package org.endeavourhealth.core.messaging.configuration.schema.serviceConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HttpReceiver complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HttpReceiver">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Path" type="{}nonEmptyString"/>
 *         &lt;element name="Methods" type="{}nonEmptyString" minOccurs="0"/>
 *         &lt;element name="IncludeSubPaths" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ReceiverClass" type="{}nonEmptyString"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HttpReceiver", propOrder = {
    "path",
    "methods",
    "includeSubPaths",
    "receiverClass"
})
public class HttpReceiver {

    @XmlElement(name = "Path", required = true)
    protected String path;
    @XmlElement(name = "Methods")
    protected String methods;
    @XmlElement(name = "IncludeSubPaths", defaultValue = "false")
    protected Boolean includeSubPaths;
    @XmlElement(name = "ReceiverClass", required = true)
    protected String receiverClass;

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Gets the value of the methods property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMethods() {
        return methods;
    }

    /**
     * Sets the value of the methods property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMethods(String value) {
        this.methods = value;
    }

    /**
     * Gets the value of the includeSubPaths property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeSubPaths() {
        return includeSubPaths;
    }

    /**
     * Sets the value of the includeSubPaths property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeSubPaths(Boolean value) {
        this.includeSubPaths = value;
    }

    /**
     * Gets the value of the receiverClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiverClass() {
        return receiverClass;
    }

    /**
     * Sets the value of the receiverClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiverClass(String value) {
        this.receiverClass = value;
    }

}


package org.endeavourhealth.messaging.configuration.schema.pluginConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ReceiverId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Path" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="IncludeSubPaths" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Methods" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ReceiverClass" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "receiverId",
    "path",
    "includeSubPaths",
    "methods",
    "receiverClass"
})
@XmlRootElement(name = "Receiver")
public class Receiver {

    @XmlElement(name = "ReceiverId", required = true)
    protected String receiverId;
    @XmlElement(name = "Path", required = true)
    protected String path;
    @XmlElement(name = "IncludeSubPaths")
    protected boolean includeSubPaths;
    @XmlElement(name = "Methods", required = true)
    protected String methods;
    @XmlElement(name = "ReceiverClass", required = true)
    protected String receiverClass;

    /**
     * Gets the value of the receiverId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiverId() {
        return receiverId;
    }

    /**
     * Sets the value of the receiverId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiverId(String value) {
        this.receiverId = value;
    }

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
     * Gets the value of the includeSubPaths property.
     * 
     */
    public boolean isIncludeSubPaths() {
        return includeSubPaths;
    }

    /**
     * Sets the value of the includeSubPaths property.
     * 
     */
    public void setIncludeSubPaths(boolean value) {
        this.includeSubPaths = value;
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

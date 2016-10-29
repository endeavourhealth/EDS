
package org.endeavourhealth.core.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MessageTransformInboundConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MessageTransformInboundConfig">
 *   &lt;complexContent>
 *     &lt;extension base="{}ComponentConfig">
 *       &lt;sequence>
 *         &lt;element name="SharedStoragePath" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FilingThreadLimit" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageTransformInboundConfig", propOrder = {
    "sharedStoragePath",
    "filingThreadLimit"
})
public class MessageTransformInboundConfig
    extends ComponentConfig
{

    @XmlElement(name = "SharedStoragePath", required = true)
    protected String sharedStoragePath;
    @XmlElement(name = "FilingThreadLimit")
    protected int filingThreadLimit;

    /**
     * Gets the value of the sharedStoragePath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSharedStoragePath() {
        return sharedStoragePath;
    }

    /**
     * Sets the value of the sharedStoragePath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSharedStoragePath(String value) {
        this.sharedStoragePath = value;
    }

    /**
     * Gets the value of the filingThreadLimit property.
     * 
     */
    public int getFilingThreadLimit() {
        return filingThreadLimit;
    }

    /**
     * Sets the value of the filingThreadLimit property.
     * 
     */
    public void setFilingThreadLimit(int value) {
        this.filingThreadLimit = value;
    }

}

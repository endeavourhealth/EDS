
package org.endeavourhealth.ui.entitymap.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dataValueType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dataValueType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="logicalValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="physicalValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataValueType", propOrder = {
    "logicalValue",
    "physicalValue",
    "displayName"
})
public class DataValueType {

    @XmlElement(required = true)
    protected String logicalValue;
    @XmlElement(required = true)
    protected String physicalValue;
    @XmlElement(required = true)
    protected String displayName;

    /**
     * Gets the value of the logicalValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogicalValue() {
        return logicalValue;
    }

    /**
     * Sets the value of the logicalValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogicalValue(String value) {
        this.logicalValue = value;
    }

    /**
     * Gets the value of the physicalValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhysicalValue() {
        return physicalValue;
    }

    /**
     * Sets the value of the physicalValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhysicalValue(String value) {
        this.physicalValue = value;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

}

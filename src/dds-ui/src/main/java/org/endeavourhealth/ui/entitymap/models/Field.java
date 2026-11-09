
package org.endeavourhealth.ui.entitymap.models;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for field complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="field">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="logicalName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="index" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="availability" type="{}availabilities"/>
 *         &lt;element name="logicalDataType" type="{}logicalDataType"/>
 *         &lt;element name="dataValues" type="{}dataValueType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "field", propOrder = {
    "logicalName",
    "displayName",
    "index",
    "availability",
    "logicalDataType",
    "dataValues"
})
public class Field {

    @XmlElement(required = true)
    protected String logicalName;
    @XmlElement(required = true)
    protected String displayName;
    protected int index;
    @XmlList
    @XmlElement(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected List<String> availability;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected LogicalDataType logicalDataType;
    protected List<DataValueType> dataValues;

    /**
     * Gets the value of the logicalName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogicalName() {
        return logicalName;
    }

    /**
     * Sets the value of the logicalName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogicalName(String value) {
        this.logicalName = value;
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

    /**
     * Gets the value of the index property.
     * 
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     * 
     */
    public void setIndex(int value) {
        this.index = value;
    }

    /**
     * Gets the value of the availability property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the availability property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAvailability().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAvailability() {
        if (availability == null) {
            availability = new ArrayList<String>();
        }
        return this.availability;
    }

    /**
     * Gets the value of the logicalDataType property.
     * 
     * @return
     *     possible object is
     *     {@link LogicalDataType }
     *     
     */
    public LogicalDataType getLogicalDataType() {
        return logicalDataType;
    }

    /**
     * Sets the value of the logicalDataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link LogicalDataType }
     *     
     */
    public void setLogicalDataType(LogicalDataType value) {
        this.logicalDataType = value;
    }

    /**
     * Gets the value of the dataValues property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataValues property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataValues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataValueType }
     * 
     * 
     */
    public List<DataValueType> getDataValues() {
        if (dataValues == null) {
            dataValues = new ArrayList<DataValueType>();
        }
        return this.dataValues;
    }

}


package org.endeavourhealth.ui.entitymap.models;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for entity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="entity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="logicalName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="resultSetIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="cardinality" type="{}cardinality"/>
 *         &lt;element name="populationFieldIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="organisationOdsFieldIndex" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="field" type="{}field" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity", propOrder = {
    "logicalName",
    "displayName",
    "resultSetIndex",
    "cardinality",
    "populationFieldIndex",
    "organisationOdsFieldIndex",
    "field"
})
public class Entity {

    @XmlElement(required = true)
    protected String logicalName;
    @XmlElement(required = true)
    protected String displayName;
    protected int resultSetIndex;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected Cardinality cardinality;
    protected int populationFieldIndex;
    protected Integer organisationOdsFieldIndex;
    @XmlElement(required = true)
    protected List<Field> field;

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
     * Gets the value of the resultSetIndex property.
     * 
     */
    public int getResultSetIndex() {
        return resultSetIndex;
    }

    /**
     * Sets the value of the resultSetIndex property.
     * 
     */
    public void setResultSetIndex(int value) {
        this.resultSetIndex = value;
    }

    /**
     * Gets the value of the cardinality property.
     * 
     * @return
     *     possible object is
     *     {@link Cardinality }
     *     
     */
    public Cardinality getCardinality() {
        return cardinality;
    }

    /**
     * Sets the value of the cardinality property.
     * 
     * @param value
     *     allowed object is
     *     {@link Cardinality }
     *     
     */
    public void setCardinality(Cardinality value) {
        this.cardinality = value;
    }

    /**
     * Gets the value of the populationFieldIndex property.
     * 
     */
    public int getPopulationFieldIndex() {
        return populationFieldIndex;
    }

    /**
     * Sets the value of the populationFieldIndex property.
     * 
     */
    public void setPopulationFieldIndex(int value) {
        this.populationFieldIndex = value;
    }

    /**
     * Gets the value of the organisationOdsFieldIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getOrganisationOdsFieldIndex() {
        return organisationOdsFieldIndex;
    }

    /**
     * Sets the value of the organisationOdsFieldIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOrganisationOdsFieldIndex(Integer value) {
        this.organisationOdsFieldIndex = value;
    }

    /**
     * Gets the value of the field property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the field property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Field }
     * 
     * 
     */
    public List<Field> getField() {
        if (field == null) {
            field = new ArrayList<Field>();
        }
        return this.field;
    }

}

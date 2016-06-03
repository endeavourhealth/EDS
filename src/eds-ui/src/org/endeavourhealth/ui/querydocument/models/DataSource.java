
package org.endeavourhealth.ui.querydocument.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for dataSource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dataSource">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="entity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="dataSourceUuid" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *           &lt;element name="calculation" type="{}calculationType"/>
 *         &lt;/choice>
 *         &lt;element name="filter" type="{}fieldTest" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="restriction" type="{}restriction" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataSource", propOrder = {
    "entity",
    "dataSourceUuid",
    "calculation",
    "filter",
    "restriction"
})
public class DataSource {

    protected String entity;
    protected List<String> dataSourceUuid;
    protected CalculationType calculation;
    protected List<FieldTest> filter;
    protected Restriction restriction;

    /**
     * Gets the value of the entity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Sets the value of the entity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntity(String value) {
        this.entity = value;
    }

    /**
     * Gets the value of the dataSourceUuid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataSourceUuid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataSourceUuid().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDataSourceUuid() {
        if (dataSourceUuid == null) {
            dataSourceUuid = new ArrayList<String>();
        }
        return this.dataSourceUuid;
    }

    /**
     * Gets the value of the calculation property.
     * 
     * @return
     *     possible object is
     *     {@link CalculationType }
     *     
     */
    public CalculationType getCalculation() {
        return calculation;
    }

    /**
     * Sets the value of the calculation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CalculationType }
     *     
     */
    public void setCalculation(CalculationType value) {
        this.calculation = value;
    }

    /**
     * Gets the value of the filter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the filter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFilter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FieldTest }
     * 
     * 
     */
    public List<FieldTest> getFilter() {
        if (filter == null) {
            filter = new ArrayList<FieldTest>();
        }
        return this.filter;
    }

    /**
     * Gets the value of the restriction property.
     * 
     * @return
     *     possible object is
     *     {@link Restriction }
     *     
     */
    public Restriction getRestriction() {
        return restriction;
    }

    /**
     * Sets the value of the restriction property.
     * 
     * @param value
     *     allowed object is
     *     {@link Restriction }
     *     
     */
    public void setRestriction(Restriction value) {
        this.restriction = value;
    }

}

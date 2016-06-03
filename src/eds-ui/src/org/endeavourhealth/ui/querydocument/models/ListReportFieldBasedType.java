
package org.endeavourhealth.ui.querydocument.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for listReportFieldBasedType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="listReportFieldBasedType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataSource" type="{}dataSource"/>
 *         &lt;element name="fieldOutput" type="{}fieldOutput" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listReportFieldBasedType", propOrder = {
    "dataSource",
    "fieldOutput"
})
public class ListReportFieldBasedType {

    @XmlElement(required = true)
    protected DataSource dataSource;
    @XmlElement(required = true)
    protected List<FieldOutput> fieldOutput;

    /**
     * Gets the value of the dataSource property.
     * 
     * @return
     *     possible object is
     *     {@link DataSource }
     *     
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets the value of the dataSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSource }
     *     
     */
    public void setDataSource(DataSource value) {
        this.dataSource = value;
    }

    /**
     * Gets the value of the fieldOutput property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fieldOutput property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFieldOutput().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FieldOutput }
     * 
     * 
     */
    public List<FieldOutput> getFieldOutput() {
        if (fieldOutput == null) {
            fieldOutput = new ArrayList<FieldOutput>();
        }
        return this.fieldOutput;
    }

}


package org.endeavourhealth.core.xml.QueryDocument;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for calculationParameter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="calculationParameter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameterName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="resourceUuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="fieldName" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "calculationParameter", propOrder = {
    "parameterName",
    "resourceUuid",
    "fieldName"
})
public class CalculationParameter {

    @XmlElement(required = true)
    protected String parameterName;
    @XmlElement(required = true)
    protected String resourceUuid;
    @XmlElement(required = true)
    protected List<String> fieldName;

    /**
     * Gets the value of the parameterName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Sets the value of the parameterName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParameterName(String value) {
        this.parameterName = value;
    }

    /**
     * Gets the value of the resourceUuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResourceUuid() {
        return resourceUuid;
    }

    /**
     * Sets the value of the resourceUuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResourceUuid(String value) {
        this.resourceUuid = value;
    }

    /**
     * Gets the value of the fieldName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fieldName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFieldName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFieldName() {
        if (fieldName == null) {
            fieldName = new ArrayList<String>();
        }
        return this.fieldName;
    }

}

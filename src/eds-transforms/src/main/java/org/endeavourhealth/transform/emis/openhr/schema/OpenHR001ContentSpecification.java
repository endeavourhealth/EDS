
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for OpenHR001.ContentSpecification complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.ContentSpecification">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="specification" type="{http://www.e-mis.com/emisopen}voc.ContentSpecification"/>
 *         &lt;element name="effectiveTime" type="{http://www.e-mis.com/emisopen}dt.TimeRange" minOccurs="0"/>
 *         &lt;element name="dataSet" type="{http://www.e-mis.com/emisopen}voc.DataSet" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.ContentSpecification", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "specification",
    "effectiveTime",
    "dataSet"
})
public class OpenHR001ContentSpecification {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String specification;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtTimeRange effectiveTime;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    @XmlSchemaType(name = "token")
    protected VocDataSet dataSet;

    /**
     * Gets the value of the specification property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecification() {
        return specification;
    }

    /**
     * Sets the value of the specification property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecification(String value) {
        this.specification = value;
    }

    /**
     * Gets the value of the effectiveTime property.
     * 
     * @return
     *     possible object is
     *     {@link DtTimeRange }
     *     
     */
    public DtTimeRange getEffectiveTime() {
        return effectiveTime;
    }

    /**
     * Sets the value of the effectiveTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtTimeRange }
     *     
     */
    public void setEffectiveTime(DtTimeRange value) {
        this.effectiveTime = value;
    }

    /**
     * Gets the value of the dataSet property.
     * 
     * @return
     *     possible object is
     *     {@link VocDataSet }
     *     
     */
    public VocDataSet getDataSet() {
        return dataSet;
    }

    /**
     * Sets the value of the dataSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocDataSet }
     *     
     */
    public void setDataSet(VocDataSet value) {
        this.dataSet = value;
    }

}


package org.endeavourhealth.ui.querydocument.models;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for listReportSummaryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="listReportSummaryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="summaryType" type="{}summaryType"/>
 *         &lt;choice>
 *           &lt;element name="dataSourceUuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="testUuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listReportSummaryType", propOrder = {
    "summaryType",
    "dataSourceUuid",
    "testUuid"
})
public class ListReportSummaryType {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected SummaryType summaryType;
    protected String dataSourceUuid;
    protected String testUuid;

    /**
     * Gets the value of the summaryType property.
     * 
     * @return
     *     possible object is
     *     {@link SummaryType }
     *     
     */
    public SummaryType getSummaryType() {
        return summaryType;
    }

    /**
     * Sets the value of the summaryType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SummaryType }
     *     
     */
    public void setSummaryType(SummaryType value) {
        this.summaryType = value;
    }

    /**
     * Gets the value of the dataSourceUuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataSourceUuid() {
        return dataSourceUuid;
    }

    /**
     * Sets the value of the dataSourceUuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataSourceUuid(String value) {
        this.dataSourceUuid = value;
    }

    /**
     * Gets the value of the testUuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTestUuid() {
        return testUuid;
    }

    /**
     * Sets the value of the testUuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTestUuid(String value) {
        this.testUuid = value;
    }

}


package org.endeavourhealth.ui.querydocument.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for listReportGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="listReportGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="heading" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="summary" type="{}listReportSummaryType"/>
 *           &lt;element name="fieldBased" type="{}listReportFieldBasedType"/>
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
@XmlType(name = "listReportGroup", propOrder = {
    "heading",
    "summary",
    "fieldBased"
})
public class ListReportGroup {

    protected String heading;
    protected ListReportSummaryType summary;
    protected ListReportFieldBasedType fieldBased;

    /**
     * Gets the value of the heading property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeading() {
        return heading;
    }

    /**
     * Sets the value of the heading property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeading(String value) {
        this.heading = value;
    }

    /**
     * Gets the value of the summary property.
     * 
     * @return
     *     possible object is
     *     {@link ListReportSummaryType }
     *     
     */
    public ListReportSummaryType getSummary() {
        return summary;
    }

    /**
     * Sets the value of the summary property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListReportSummaryType }
     *     
     */
    public void setSummary(ListReportSummaryType value) {
        this.summary = value;
    }

    /**
     * Gets the value of the fieldBased property.
     * 
     * @return
     *     possible object is
     *     {@link ListReportFieldBasedType }
     *     
     */
    public ListReportFieldBasedType getFieldBased() {
        return fieldBased;
    }

    /**
     * Sets the value of the fieldBased property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListReportFieldBasedType }
     *     
     */
    public void setFieldBased(ListReportFieldBasedType value) {
        this.fieldBased = value;
    }

}


package org.endeavourhealth.ui.querydocument.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for reportItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="reportItem">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="queryLibraryItemUuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="listReportLibraryItemUuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *         &lt;element name="reportItem" type="{}reportItem" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reportItem", propOrder = {
    "queryLibraryItemUuid",
    "listReportLibraryItemUuid",
    "reportItem"
})
public class ReportItem {

    protected String queryLibraryItemUuid;
    protected String listReportLibraryItemUuid;
    protected List<ReportItem> reportItem;

    /**
     * Gets the value of the queryLibraryItemUuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryLibraryItemUuid() {
        return queryLibraryItemUuid;
    }

    /**
     * Sets the value of the queryLibraryItemUuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryLibraryItemUuid(String value) {
        this.queryLibraryItemUuid = value;
    }

    /**
     * Gets the value of the listReportLibraryItemUuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getListReportLibraryItemUuid() {
        return listReportLibraryItemUuid;
    }

    /**
     * Sets the value of the listReportLibraryItemUuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setListReportLibraryItemUuid(String value) {
        this.listReportLibraryItemUuid = value;
    }

    /**
     * Gets the value of the reportItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reportItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReportItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReportItem }
     * 
     * 
     */
    public List<ReportItem> getReportItem() {
        if (reportItem == null) {
            reportItem = new ArrayList<ReportItem>();
        }
        return this.reportItem;
    }

}

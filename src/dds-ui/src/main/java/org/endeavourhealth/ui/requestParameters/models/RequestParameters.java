
package org.endeavourhealth.ui.requestParameters.models;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for requestParameters complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="requestParameters">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="reportUuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="baselineDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="patientType">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="regular"/>
 *               &lt;enumeration value="nonRegular"/>
 *               &lt;enumeration value="all"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="patientStatus">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="active"/>
 *               &lt;enumeration value="all"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="organisation" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "requestParameters", propOrder = {
    "reportUuid",
    "baselineDate",
    "patientType",
    "patientStatus",
    "organisation"
})
public class RequestParameters {

    @XmlElement(required = true)
    protected String reportUuid;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar baselineDate;
    @XmlElement(required = true)
    protected String patientType;
    @XmlElement(required = true)
    protected String patientStatus;
    protected List<String> organisation;

    /**
     * Gets the value of the reportUuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportUuid() {
        return reportUuid;
    }

    /**
     * Sets the value of the reportUuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportUuid(String value) {
        this.reportUuid = value;
    }

    /**
     * Gets the value of the baselineDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBaselineDate() {
        return baselineDate;
    }

    /**
     * Sets the value of the baselineDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBaselineDate(XMLGregorianCalendar value) {
        this.baselineDate = value;
    }

    /**
     * Gets the value of the patientType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientType() {
        return patientType;
    }

    /**
     * Sets the value of the patientType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientType(String value) {
        this.patientType = value;
    }

    /**
     * Gets the value of the patientStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientStatus() {
        return patientStatus;
    }

    /**
     * Sets the value of the patientStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientStatus(String value) {
        this.patientStatus = value;
    }

    /**
     * Gets the value of the organisation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the organisation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrganisation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOrganisation() {
        if (organisation == null) {
            organisation = new ArrayList<String>();
        }
        return this.organisation;
    }

}

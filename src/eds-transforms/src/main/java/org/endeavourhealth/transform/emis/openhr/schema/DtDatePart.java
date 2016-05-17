
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Represents a specific instant of time (ISO 8601). Combinations of date and time of day values are defined by the datepart property.
 * 
 * <p>Java class for dt.DatePart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.DatePart">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="datepart" use="required" type="{http://www.e-mis.com/emisopen}voc.DatePart" />
 *       &lt;attribute name="value" type="{http://www.e-mis.com/emisopen}dt.DateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dt.DatePart", namespace = "http://www.e-mis.com/emisopen")
public class DtDatePart {

    @XmlAttribute(name = "datepart", required = true)
    protected VocDatePart datepart;
    @XmlAttribute(name = "value")
    protected XMLGregorianCalendar value;

    /**
     * Gets the value of the datepart property.
     * 
     * @return
     *     possible object is
     *     {@link VocDatePart }
     *     
     */
    public VocDatePart getDatepart() {
        return datepart;
    }

    /**
     * Sets the value of the datepart property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocDatePart }
     *     
     */
    public void setDatepart(VocDatePart value) {
        this.datepart = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setValue(XMLGregorianCalendar value) {
        this.value = value;
    }

}

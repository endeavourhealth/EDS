
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EDIComment complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EDIComment">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FTX" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EDIComment", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "ftx"
})
public class EDIComment {

    @XmlElement(name = "FTX", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String ftx;

    /**
     * Gets the value of the ftx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFTX() {
        return ftx;
    }

    /**
     * Sets the value of the ftx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFTX(String value) {
        this.ftx = value;
    }

}

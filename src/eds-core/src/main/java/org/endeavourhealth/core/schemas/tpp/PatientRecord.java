
package org.endeavourhealth.core.schemas.tpp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PatientRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PatientRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RequestUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Body" type="{}Body"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PatientRecord", propOrder = {
    "requestUID",
    "body"
})
public class PatientRecord {

    @XmlElement(name = "RequestUID", required = true)
    protected String requestUID;
    @XmlElement(name = "Body", required = true)
    protected Body body;

    /**
     * Gets the value of the requestUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestUID() {
        return requestUID;
    }

    /**
     * Sets the value of the requestUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestUID(String value) {
        this.requestUID = value;
    }

    /**
     * Gets the value of the body property.
     * 
     * @return
     *     possible object is
     *     {@link Body }
     *     
     */
    public Body getBody() {
        return body;
    }

    /**
     * Sets the value of the body property.
     * 
     * @param value
     *     allowed object is
     *     {@link Body }
     *     
     */
    public void setBody(Body value) {
        this.body = value;
    }

}

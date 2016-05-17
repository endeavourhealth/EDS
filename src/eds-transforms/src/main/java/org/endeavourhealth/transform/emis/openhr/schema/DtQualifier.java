
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dt.Qualifier complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.Qualifier">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.e-mis.com/emisopen}dt.Code"/>
 *         &lt;element name="value" type="{http://www.e-mis.com/emisopen}dt.Code"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dt.Qualifier", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "name",
    "value"
})
public class DtQualifier {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected DtCode name;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected DtCode value;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link DtCode }
     *     
     */
    public DtCode getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCode }
     *     
     */
    public void setName(DtCode value) {
        this.name = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link DtCode }
     *     
     */
    public DtCode getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCode }
     *     
     */
    public void setValue(DtCode value) {
        this.value = value;
    }

}

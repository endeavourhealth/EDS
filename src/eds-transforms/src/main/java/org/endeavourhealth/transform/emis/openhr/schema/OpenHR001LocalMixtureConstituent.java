
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.LocalMixtureConstituent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.LocalMixtureConstituent">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="preparation" type="{http://www.e-mis.com/emisopen}dt.Code"/>
 *         &lt;element name="strength">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="20"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenHR001.LocalMixtureConstituent", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "preparation",
    "strength"
})
public class OpenHR001LocalMixtureConstituent
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected DtCode preparation;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String strength;

    /**
     * Gets the value of the preparation property.
     * 
     * @return
     *     possible object is
     *     {@link DtCode }
     *     
     */
    public DtCode getPreparation() {
        return preparation;
    }

    /**
     * Sets the value of the preparation property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtCode }
     *     
     */
    public void setPreparation(DtCode value) {
        this.preparation = value;
    }

    /**
     * Gets the value of the strength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrength() {
        return strength;
    }

    /**
     * Sets the value of the strength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrength(String value) {
        this.strength = value;
    }

}

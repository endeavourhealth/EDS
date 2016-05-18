
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LinkType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LinkType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Target" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType"/>
 *         &lt;element name="LinkType">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LinkType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "target",
    "linkType"
})
public class LinkType {

    @XmlElement(name = "Target", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected IdentType target;
    @XmlElement(name = "LinkType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected BigInteger linkType;

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setTarget(IdentType value) {
        this.target = value;
    }

    /**
     * Gets the value of the linkType property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLinkType() {
        return linkType;
    }

    /**
     * Sets the value of the linkType property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLinkType(BigInteger value) {
        this.linkType = value;
    }

}

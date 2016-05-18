
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MixtureItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MixtureItemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="PreparationID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType" minOccurs="0"/>
 *         &lt;element name="StrengthQuantity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MixtureItemType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "preparationID",
    "strengthQuantity"
})
public class MixtureItemType
    extends IdentType
{

    @XmlElement(name = "PreparationID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IntegerCodeType preparationID;
    @XmlElement(name = "StrengthQuantity", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String strengthQuantity;

    /**
     * Gets the value of the preparationID property.
     * 
     * @return
     *     possible object is
     *     {@link IntegerCodeType }
     *     
     */
    public IntegerCodeType getPreparationID() {
        return preparationID;
    }

    /**
     * Sets the value of the preparationID property.
     * 
     * @param value
     *     allowed object is
     *     {@link IntegerCodeType }
     *     
     */
    public void setPreparationID(IntegerCodeType value) {
        this.preparationID = value;
    }

    /**
     * Gets the value of the strengthQuantity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrengthQuantity() {
        return strengthQuantity;
    }

    /**
     * Sets the value of the strengthQuantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrengthQuantity(String value) {
        this.strengthQuantity = value;
    }

}

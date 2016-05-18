
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MedicationLinkType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MedicationLinkType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="GUID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PreparationID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="SystemDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MedicationLinkType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "guid",
    "preparationID",
    "systemDate"
})
public class MedicationLinkType {

    @XmlElement(name = "GUID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String guid;
    @XmlElement(name = "PreparationID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger preparationID;
    @XmlElement(name = "SystemDate", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String systemDate;

    /**
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGUID() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGUID(String value) {
        this.guid = value;
    }

    /**
     * Gets the value of the preparationID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPreparationID() {
        return preparationID;
    }

    /**
     * Sets the value of the preparationID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPreparationID(BigInteger value) {
        this.preparationID = value;
    }

    /**
     * Gets the value of the systemDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystemDate() {
        return systemDate;
    }

    /**
     * Sets the value of the systemDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystemDate(String value) {
        this.systemDate = value;
    }

}

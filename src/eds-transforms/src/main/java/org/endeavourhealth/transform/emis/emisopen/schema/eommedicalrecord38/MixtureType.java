
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MixtureType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MixtureType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LocalMixture" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType"/>
 *         &lt;sequence>
 *           &lt;element name="MixtureName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="Constituents" maxOccurs="unbounded">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="PreparationID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType"/>
 *                     &lt;element name="StrengthQuantity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MixtureType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "localMixture",
    "mixtureName",
    "constituents"
})
public class MixtureType {

    @XmlElement(name = "LocalMixture", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected IdentType localMixture;
    @XmlElement(name = "MixtureName", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected String mixtureName;
    @XmlElement(name = "Constituents", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
    protected List<MixtureType.Constituents> constituents;

    /**
     * Gets the value of the localMixture property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getLocalMixture() {
        return localMixture;
    }

    /**
     * Sets the value of the localMixture property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setLocalMixture(IdentType value) {
        this.localMixture = value;
    }

    /**
     * Gets the value of the mixtureName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMixtureName() {
        return mixtureName;
    }

    /**
     * Sets the value of the mixtureName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMixtureName(String value) {
        this.mixtureName = value;
    }

    /**
     * Gets the value of the constituents property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the constituents property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConstituents().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MixtureType.Constituents }
     * 
     * 
     */
    public List<MixtureType.Constituents> getConstituents() {
        if (constituents == null) {
            constituents = new ArrayList<MixtureType.Constituents>();
        }
        return this.constituents;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="PreparationID" type="{http://www.e-mis.com/emisopen/MedicalRecord}IntegerCodeType"/>
     *         &lt;element name="StrengthQuantity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "preparationID",
        "strengthQuantity"
    })
    public static class Constituents {

        @XmlElement(name = "PreparationID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", required = true)
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

}

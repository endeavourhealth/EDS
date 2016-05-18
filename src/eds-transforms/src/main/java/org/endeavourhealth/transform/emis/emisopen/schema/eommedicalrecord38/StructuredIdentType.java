
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StructuredIdentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StructuredIdentType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="NationalCode" minOccurs="0">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="CodeScheme" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                     &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="AgreedID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StructuredIdentType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "nationalCode",
    "agreedID",
    "name"
})
public class StructuredIdentType
    extends IdentType
{

    @XmlElement(name = "NationalCode", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StructuredIdentType.NationalCode nationalCode;
    @XmlElement(name = "AgreedID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String agreedID;
    @XmlElement(name = "Name", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String name;

    /**
     * Gets the value of the nationalCode property.
     * 
     * @return
     *     possible object is
     *     {@link StructuredIdentType.NationalCode }
     *     
     */
    public StructuredIdentType.NationalCode getNationalCode() {
        return nationalCode;
    }

    /**
     * Sets the value of the nationalCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link StructuredIdentType.NationalCode }
     *     
     */
    public void setNationalCode(StructuredIdentType.NationalCode value) {
        this.nationalCode = value;
    }

    /**
     * Gets the value of the agreedID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAgreedID() {
        return agreedID;
    }

    /**
     * Sets the value of the agreedID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAgreedID(String value) {
        this.agreedID = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
     *         &lt;element name="CodeScheme" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
        "codeScheme",
        "code"
    })
    public static class NationalCode {

        @XmlElement(name = "CodeScheme", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String codeScheme;
        @XmlElement(name = "Code", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String code;

        /**
         * Gets the value of the codeScheme property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCodeScheme() {
            return codeScheme;
        }

        /**
         * Sets the value of the codeScheme property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCodeScheme(String value) {
            this.codeScheme = value;
        }

        /**
         * Gets the value of the code property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCode() {
            return code;
        }

        /**
         * Sets the value of the code property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCode(String value) {
            this.code = value;
        }

    }

}

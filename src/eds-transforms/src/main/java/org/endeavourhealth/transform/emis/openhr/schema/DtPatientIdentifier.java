
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dt.PatientIdentifier complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.PatientIdentifier">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="identifierType" type="{http://www.e-mis.com/emisopen}voc.PatientIdentifierType"/>
 *         &lt;element name="value">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="50"/>
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
@XmlType(name = "dt.PatientIdentifier", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "identifierType",
    "value"
})
public class DtPatientIdentifier
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocPatientIdentifierType identifierType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String value;

    /**
     * Gets the value of the identifierType property.
     * 
     * @return
     *     possible object is
     *     {@link VocPatientIdentifierType }
     *     
     */
    public VocPatientIdentifierType getIdentifierType() {
        return identifierType;
    }

    /**
     * Sets the value of the identifierType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocPatientIdentifierType }
     *     
     */
    public void setIdentifierType(VocPatientIdentifierType value) {
        this.identifierType = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

}

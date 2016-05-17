
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dt.UserIdentifier complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.UserIdentifier">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="identifierType" type="{http://www.e-mis.com/emisopen}voc.UserIdentifierType"/>
 *         &lt;element name="value">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="15"/>
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
@XmlType(name = "dt.UserIdentifier", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "identifierType",
    "value"
})
public class DtUserIdentifier
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocUserIdentifierType identifierType;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String value;

    /**
     * Gets the value of the identifierType property.
     * 
     * @return
     *     possible object is
     *     {@link VocUserIdentifierType }
     *     
     */
    public VocUserIdentifierType getIdentifierType() {
        return identifierType;
    }

    /**
     * Sets the value of the identifierType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocUserIdentifierType }
     *     
     */
    public void setIdentifierType(VocUserIdentifierType value) {
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

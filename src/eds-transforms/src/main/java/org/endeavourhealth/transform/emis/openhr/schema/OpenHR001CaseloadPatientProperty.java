
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OpenHR001.CaseloadPatientProperty complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OpenHR001.CaseloadPatientProperty">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.dbo">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.e-mis.com/emisopen}voc.CaseloadPatientProperty"/>
 *         &lt;element name="value">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="255"/>
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
@XmlType(name = "OpenHR001.CaseloadPatientProperty", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "name",
    "value"
})
public class OpenHR001CaseloadPatientProperty
    extends DtDbo
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    @XmlSchemaType(name = "token")
    protected VocCaseloadPatientProperty name;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String value;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link VocCaseloadPatientProperty }
     *     
     */
    public VocCaseloadPatientProperty getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocCaseloadPatientProperty }
     *     
     */
    public void setName(VocCaseloadPatientProperty value) {
        this.name = value;
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

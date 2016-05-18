
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TeamType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TeamType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Speciality" type="{http://www.e-mis.com/emisopen/MedicalRecord}StringCodeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TeamType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "name",
    "speciality"
})
public class TeamType
    extends IdentType
{

    @XmlElement(name = "Name", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String name;
    @XmlElement(name = "Speciality", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected StringCodeType speciality;

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
     * Gets the value of the speciality property.
     * 
     * @return
     *     possible object is
     *     {@link StringCodeType }
     *     
     */
    public StringCodeType getSpeciality() {
        return speciality;
    }

    /**
     * Sets the value of the speciality property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringCodeType }
     *     
     */
    public void setSpeciality(StringCodeType value) {
        this.speciality = value;
    }

}

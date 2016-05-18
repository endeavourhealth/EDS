
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Role as defined by application type, location and team
 * 
 * <p>Java class for RoleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RoleType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType">
 *       &lt;sequence>
 *         &lt;element name="Application" type="{http://www.e-mis.com/emisopen/MedicalRecord}ApplicationType" minOccurs="0"/>
 *         &lt;element name="Team" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *         &lt;element name="Location" type="{http://www.e-mis.com/emisopen/MedicalRecord}IdentType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoleType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "application",
    "team",
    "location"
})
public class RoleType
    extends IdentType
{

    @XmlElement(name = "Application", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected ApplicationType application;
    @XmlElement(name = "Team", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType team;
    @XmlElement(name = "Location", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected IdentType location;

    /**
     * Gets the value of the application property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationType }
     *     
     */
    public ApplicationType getApplication() {
        return application;
    }

    /**
     * Sets the value of the application property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationType }
     *     
     */
    public void setApplication(ApplicationType value) {
        this.application = value;
    }

    /**
     * Gets the value of the team property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getTeam() {
        return team;
    }

    /**
     * Sets the value of the team property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setTeam(IdentType value) {
        this.team = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link IdentType }
     *     
     */
    public IdentType getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentType }
     *     
     */
    public void setLocation(IdentType value) {
        this.location = value;
    }

}

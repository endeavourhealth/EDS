
package org.endeavourhealth.transform.enterprise.schema;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for baseRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="baseRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mode">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="insert"/>
 *               &lt;enumeration value="update"/>
 *               &lt;enumeration value="delete"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "baseRecord", propOrder = {
    "mode",
    "id"
})
@XmlSeeAlso({
    AllergyIntolerance.class,
    Organisation.class,
    Appointment.class,
    Patient.class,
    MedicationOrder.class,
    Practitioner.class,
    Observation.class,
    FamilyMemberHistory.class,
    MedicationStatement.class,
    Encounter.class,
    Immunisation.class,
    Procedure.class,
    Schedule.class,
    Condition.class,
    ReferralRequest.class,
    DiagnosticOrder.class,
    ProcedureRequest.class
})
public class BaseRecord {

    @XmlElement(required = true)
    protected String mode;
    @XmlElement(required = true)
    protected String id;

    /**
     * Gets the value of the mode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMode() {
        return mode;
    }

    /**
     * Sets the value of the mode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMode(String value) {
        this.mode = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}

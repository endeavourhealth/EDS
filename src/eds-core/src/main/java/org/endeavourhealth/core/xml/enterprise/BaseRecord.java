
package org.endeavourhealth.core.xml.enterprise;

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
 *         &lt;element name="save_mode" type="{}save_mode"/>
 *         &lt;element name="id" type="{}uuid"/>
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
    "saveMode",
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

    @XmlElement(name = "save_mode", required = true)
    @XmlSchemaType(name = "string")
    protected SaveMode saveMode;
    @XmlElement(required = true)
    protected String id;

    /**
     * Gets the value of the saveMode property.
     * 
     * @return
     *     possible object is
     *     {@link SaveMode }
     *     
     */
    public SaveMode getSaveMode() {
        return saveMode;
    }

    /**
     * Sets the value of the saveMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link SaveMode }
     *     
     */
    public void setSaveMode(SaveMode value) {
        this.saveMode = value;
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

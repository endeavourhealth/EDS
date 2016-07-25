
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CarePlanInstructionResponsibility.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CarePlanInstructionResponsibility">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Nurse"/>
 *     &lt;enumeration value="CsvPatient"/>
 *     &lt;enumeration value="Carer"/>
 *     &lt;enumeration value="Therapist"/>
 *     &lt;enumeration value="Assistant"/>
 *     &lt;enumeration value="Practitioner"/>
 *     &lt;enumeration value="Social Care Co-ordinator"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CarePlanInstructionResponsibility")
@XmlEnum
public enum CarePlanInstructionResponsibility {

    @XmlEnumValue("Nurse")
    NURSE("Nurse"),
    @XmlEnumValue("CsvPatient")
    PATIENT("CsvPatient"),
    @XmlEnumValue("Carer")
    CARER("Carer"),
    @XmlEnumValue("Therapist")
    THERAPIST("Therapist"),
    @XmlEnumValue("Assistant")
    ASSISTANT("Assistant"),
    @XmlEnumValue("Practitioner")
    PRACTITIONER("Practitioner"),
    @XmlEnumValue("Social Care Co-ordinator")
    SOCIAL_CARE_CO_ORDINATOR("Social Care Co-ordinator");
    private final String value;

    CarePlanInstructionResponsibility(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CarePlanInstructionResponsibility fromValue(String v) {
        for (CarePlanInstructionResponsibility c: CarePlanInstructionResponsibility.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

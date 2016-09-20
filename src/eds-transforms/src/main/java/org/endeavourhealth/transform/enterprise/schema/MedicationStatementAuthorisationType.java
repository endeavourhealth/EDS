
package org.endeavourhealth.transform.enterprise.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for medication_statement_authorisation_type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="medication_statement_authorisation_type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="acute"/>
 *     &lt;enumeration value="repeat"/>
 *     &lt;enumeration value="repeat-dispensing"/>
 *     &lt;enumeration value="automatic"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "medication_statement_authorisation_type")
@XmlEnum
public enum MedicationStatementAuthorisationType {

    @XmlEnumValue("acute")
    ACUTE("acute"),
    @XmlEnumValue("repeat")
    REPEAT("repeat"),
    @XmlEnumValue("repeat-dispensing")
    REPEAT_DISPENSING("repeat-dispensing"),
    @XmlEnumValue("automatic")
    AUTOMATIC("automatic");
    private final String value;

    MedicationStatementAuthorisationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MedicationStatementAuthorisationType fromValue(String v) {
        for (MedicationStatementAuthorisationType c: MedicationStatementAuthorisationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

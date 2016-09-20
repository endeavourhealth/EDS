
package org.endeavourhealth.transform.enterprise.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for medication_statement_status.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="medication_statement_status">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="active"/>
 *     &lt;enumeration value="completed"/>
 *     &lt;enumeration value="entered-in-error"/>
 *     &lt;enumeration value="intended"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "medication_statement_status")
@XmlEnum
public enum MedicationStatementStatus {

    @XmlEnumValue("active")
    ACTIVE("active"),
    @XmlEnumValue("completed")
    COMPLETED("completed"),
    @XmlEnumValue("entered-in-error")
    ENTERED_IN_ERROR("entered-in-error"),
    @XmlEnumValue("intended")
    INTENDED("intended");
    private final String value;

    MedicationStatementStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MedicationStatementStatus fromValue(String v) {
        for (MedicationStatementStatus c: MedicationStatementStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

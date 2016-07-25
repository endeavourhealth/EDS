
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MedicationEndReason.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MedicationEndReason">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="End of Course"/>
 *     &lt;enumeration value="Side Effect"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MedicationEndReason")
@XmlEnum
public enum MedicationEndReason {

    @XmlEnumValue("End of Course")
    END_OF_COURSE("End of Course"),
    @XmlEnumValue("Side Effect")
    SIDE_EFFECT("Side Effect");
    private final String value;

    MedicationEndReason(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MedicationEndReason fromValue(String v) {
        for (MedicationEndReason c: MedicationEndReason.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

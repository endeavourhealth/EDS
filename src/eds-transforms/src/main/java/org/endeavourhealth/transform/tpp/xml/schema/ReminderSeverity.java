
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReminderSeverity.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ReminderSeverity">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Low"/>
 *     &lt;enumeration value="Normal"/>
 *     &lt;enumeration value="High"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ReminderSeverity")
@XmlEnum
public enum ReminderSeverity {

    @XmlEnumValue("Low")
    LOW("Low"),
    @XmlEnumValue("Normal")
    NORMAL("Normal"),
    @XmlEnumValue("High")
    HIGH("High");
    private final String value;

    ReminderSeverity(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReminderSeverity fromValue(String v) {
        for (ReminderSeverity c: ReminderSeverity.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

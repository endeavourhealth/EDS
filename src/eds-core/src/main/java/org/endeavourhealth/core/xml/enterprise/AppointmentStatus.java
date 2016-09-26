
package org.endeavourhealth.core.xml.enterprise;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for appointment_status.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="appointment_status">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="proposed"/>
 *     &lt;enumeration value="pending"/>
 *     &lt;enumeration value="booked"/>
 *     &lt;enumeration value="arrived"/>
 *     &lt;enumeration value="fulfilled"/>
 *     &lt;enumeration value="cancelled"/>
 *     &lt;enumeration value="noshow"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "appointment_status")
@XmlEnum
public enum AppointmentStatus {

    @XmlEnumValue("proposed")
    PROPOSED("proposed"),
    @XmlEnumValue("pending")
    PENDING("pending"),
    @XmlEnumValue("booked")
    BOOKED("booked"),
    @XmlEnumValue("arrived")
    ARRIVED("arrived"),
    @XmlEnumValue("fulfilled")
    FULFILLED("fulfilled"),
    @XmlEnumValue("cancelled")
    CANCELLED("cancelled"),
    @XmlEnumValue("noshow")
    NOSHOW("noshow");
    private final String value;

    AppointmentStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AppointmentStatus fromValue(String v) {
        for (AppointmentStatus c: AppointmentStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

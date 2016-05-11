
package org.endeavourhealth.core.transform.tpp.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AppointmentStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AppointmentStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Booked"/>
 *     &lt;enumeration value="Waiting"/>
 *     &lt;enumeration value="In Progress"/>
 *     &lt;enumeration value="Finished"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AppointmentStatus")
@XmlEnum
public enum AppointmentStatus {

    @XmlEnumValue("Booked")
    BOOKED("Booked"),
    @XmlEnumValue("Waiting")
    WAITING("Waiting"),
    @XmlEnumValue("In Progress")
    IN_PROGRESS("In Progress"),
    @XmlEnumValue("Finished")
    FINISHED("Finished");
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

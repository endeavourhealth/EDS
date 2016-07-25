
package org.endeavourhealth.transform.tpp.xml.schema;

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
 *     &lt;enumeration value="Arrived"/>
 *     &lt;enumeration value="Waiting"/>
 *     &lt;enumeration value="In Progress"/>
 *     &lt;enumeration value="Finished"/>
 *     &lt;enumeration value="Did Not Attend"/>
 *     &lt;enumeration value="CsvPatient Walked Out"/>
 *     &lt;enumeration value="Cancelled by CsvPatient"/>
 *     &lt;enumeration value="Cancelled by Organisation"/>
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
    @XmlEnumValue("Arrived")
    ARRIVED("Arrived"),
    @XmlEnumValue("Waiting")
    WAITING("Waiting"),
    @XmlEnumValue("In Progress")
    IN_PROGRESS("In Progress"),
    @XmlEnumValue("Finished")
    FINISHED("Finished"),
    @XmlEnumValue("Did Not Attend")
    DID_NOT_ATTEND("Did Not Attend"),
    @XmlEnumValue("CsvPatient Walked Out")
    PATIENT_WALKED_OUT("CsvPatient Walked Out"),
    @XmlEnumValue("Cancelled by CsvPatient")
    CANCELLED_BY_PATIENT("Cancelled by CsvPatient"),
    @XmlEnumValue("Cancelled by Organisation")
    CANCELLED_BY_ORGANISATION("Cancelled by Organisation");
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

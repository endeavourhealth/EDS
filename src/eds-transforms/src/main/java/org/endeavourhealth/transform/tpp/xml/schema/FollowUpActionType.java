
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FollowUpActionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="FollowUpActionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="No Further Action"/>
 *     &lt;enumeration value="Make an appointment to see doctor"/>
 *     &lt;enumeration value="Make an appointment to see nurse"/>
 *     &lt;enumeration value="Need to speak to doctor"/>
 *     &lt;enumeration value="Need to repeat test"/>
 *     &lt;enumeration value="Request Notes"/>
 *     &lt;enumeration value="CsvPatient To Pick Up Script"/>
 *     &lt;enumeration value="Communicate CsvPatient"/>
 *     &lt;enumeration value="Need to speak to nurse"/>
 *     &lt;enumeration value="Other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FollowUpActionType")
@XmlEnum
public enum FollowUpActionType {

    @XmlEnumValue("No Further Action")
    NO_FURTHER_ACTION("No Further Action"),
    @XmlEnumValue("Make an appointment to see doctor")
    MAKE_AN_APPOINTMENT_TO_SEE_DOCTOR("Make an appointment to see doctor"),
    @XmlEnumValue("Make an appointment to see nurse")
    MAKE_AN_APPOINTMENT_TO_SEE_NURSE("Make an appointment to see nurse"),
    @XmlEnumValue("Need to speak to doctor")
    NEED_TO_SPEAK_TO_DOCTOR("Need to speak to doctor"),
    @XmlEnumValue("Need to repeat test")
    NEED_TO_REPEAT_TEST("Need to repeat test"),
    @XmlEnumValue("Request Notes")
    REQUEST_NOTES("Request Notes"),
    @XmlEnumValue("CsvPatient To Pick Up Script")
    PATIENT_TO_PICK_UP_SCRIPT("CsvPatient To Pick Up Script"),
    @XmlEnumValue("Communicate CsvPatient")
    COMMUNICATE_PATIENT("Communicate CsvPatient"),
    @XmlEnumValue("Need to speak to nurse")
    NEED_TO_SPEAK_TO_NURSE("Need to speak to nurse"),
    @XmlEnumValue("Other")
    OTHER("Other");
    private final String value;

    FollowUpActionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FollowUpActionType fromValue(String v) {
        for (FollowUpActionType c: FollowUpActionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

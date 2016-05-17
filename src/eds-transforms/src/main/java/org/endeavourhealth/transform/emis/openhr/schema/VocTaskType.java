
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.TaskType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.TaskType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BOOK_APPOINTMENT"/>
 *     &lt;enumeration value="BOOK_APPOINTMENT_DOCTOR"/>
 *     &lt;enumeration value="BOOK_APPOINTMENT_NURSE"/>
 *     &lt;enumeration value="TELEPHONE_PATIENT"/>
 *     &lt;enumeration value="SCREEN_MESSAGE"/>
 *     &lt;enumeration value="RESULTS_FOR_INFO"/>
 *     &lt;enumeration value="MEETING_NOTIFICATION"/>
 *     &lt;enumeration value="PATIENT_NOTE"/>
 *     &lt;enumeration value="ADMIN_NOTE"/>
 *     &lt;enumeration value="FORM_TO_COMPLETE"/>
 *     &lt;enumeration value="REPEAT_TEST"/>
 *     &lt;enumeration value="ESCALATION_NOTIFICATION"/>
 *     &lt;enumeration value="CONFIDENTIALITY_POLICIES_OVERRIDDEN"/>
 *     &lt;enumeration value="DTS_TRANSMISSION_FAILURE"/>
 *     &lt;enumeration value="LEGITIMATE_RELATIONSHIP_NOTIFICATION"/>
 *     &lt;enumeration value="DTS_TRANSMISSION_REPORT"/>
 *     &lt;enumeration value="OVERDUE_TASK_NOTIFICATION"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.TaskType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocTaskType {

    BOOK_APPOINTMENT,
    BOOK_APPOINTMENT_DOCTOR,
    BOOK_APPOINTMENT_NURSE,
    TELEPHONE_PATIENT,
    SCREEN_MESSAGE,
    RESULTS_FOR_INFO,
    MEETING_NOTIFICATION,
    PATIENT_NOTE,
    ADMIN_NOTE,
    FORM_TO_COMPLETE,
    REPEAT_TEST,
    ESCALATION_NOTIFICATION,
    CONFIDENTIALITY_POLICIES_OVERRIDDEN,
    DTS_TRANSMISSION_FAILURE,
    LEGITIMATE_RELATIONSHIP_NOTIFICATION,
    DTS_TRANSMISSION_REPORT,
    OVERDUE_TASK_NOTIFICATION;

    public String value() {
        return name();
    }

    public static VocTaskType fromValue(String v) {
        return valueOf(v);
    }

}

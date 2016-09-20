
package org.endeavourhealth.transform.enterprise.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for procedure_request_status.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="procedure_request_status">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="proposed"/>
 *     &lt;enumeration value="draft"/>
 *     &lt;enumeration value="requested"/>
 *     &lt;enumeration value="received"/>
 *     &lt;enumeration value="accepted"/>
 *     &lt;enumeration value="in-progress"/>
 *     &lt;enumeration value="completed"/>
 *     &lt;enumeration value="suspended"/>
 *     &lt;enumeration value="rejected"/>
 *     &lt;enumeration value="aborted"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "procedure_request_status")
@XmlEnum
public enum ProcedureRequestStatus {

    @XmlEnumValue("proposed")
    PROPOSED("proposed"),
    @XmlEnumValue("draft")
    DRAFT("draft"),
    @XmlEnumValue("requested")
    REQUESTED("requested"),
    @XmlEnumValue("received")
    RECEIVED("received"),
    @XmlEnumValue("accepted")
    ACCEPTED("accepted"),
    @XmlEnumValue("in-progress")
    IN_PROGRESS("in-progress"),
    @XmlEnumValue("completed")
    COMPLETED("completed"),
    @XmlEnumValue("suspended")
    SUSPENDED("suspended"),
    @XmlEnumValue("rejected")
    REJECTED("rejected"),
    @XmlEnumValue("aborted")
    ABORTED("aborted");
    private final String value;

    ProcedureRequestStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProcedureRequestStatus fromValue(String v) {
        for (ProcedureRequestStatus c: ProcedureRequestStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

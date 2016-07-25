
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VisitStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="VisitStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Booked"/>
 *     &lt;enumeration value="Finished"/>
 *     &lt;enumeration value="Cancelled by CsvPatient"/>
 *     &lt;enumeration value="Cancelled by Organisation"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "VisitStatus")
@XmlEnum
public enum VisitStatus {

    @XmlEnumValue("Booked")
    BOOKED("Booked"),
    @XmlEnumValue("Finished")
    FINISHED("Finished"),
    @XmlEnumValue("Cancelled by CsvPatient")
    CANCELLED_BY_PATIENT("Cancelled by CsvPatient"),
    @XmlEnumValue("Cancelled by Organisation")
    CANCELLED_BY_ORGANISATION("Cancelled by Organisation");
    private final String value;

    VisitStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VisitStatus fromValue(String v) {
        for (VisitStatus c: VisitStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

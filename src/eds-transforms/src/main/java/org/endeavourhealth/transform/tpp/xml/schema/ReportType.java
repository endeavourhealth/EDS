
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReportType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ReportType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Pathology"/>
 *     &lt;enumeration value="Radiology"/>
 *     &lt;enumeration value="Blood Spot"/>
 *     &lt;enumeration value="Screening"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ReportType")
@XmlEnum
public enum ReportType {

    @XmlEnumValue("Pathology")
    PATHOLOGY("Pathology"),
    @XmlEnumValue("Radiology")
    RADIOLOGY("Radiology"),
    @XmlEnumValue("Blood Spot")
    BLOOD_SPOT("Blood Spot"),
    @XmlEnumValue("Screening")
    SCREENING("Screening");
    private final String value;

    ReportType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReportType fromValue(String v) {
        for (ReportType c: ReportType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

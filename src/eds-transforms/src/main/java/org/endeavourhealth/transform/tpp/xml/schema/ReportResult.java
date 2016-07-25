
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReportResult.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ReportResult">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Normal"/>
 *     &lt;enumeration value="Normal, but unexpected"/>
 *     &lt;enumeration value="Satisfactory"/>
 *     &lt;enumeration value="Borderline"/>
 *     &lt;enumeration value="Abnormal, but expected"/>
 *     &lt;enumeration value="Abnormal"/>
 *     &lt;enumeration value="Specimen lost / unusable"/>
 *     &lt;enumeration value="Positive"/>
 *     &lt;enumeration value="Negative"/>
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="Not responded to invitation"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ReportResult")
@XmlEnum
public enum ReportResult {

    @XmlEnumValue("Normal")
    NORMAL("Normal"),
    @XmlEnumValue("Normal, but unexpected")
    NORMAL_BUT_UNEXPECTED("Normal, but unexpected"),
    @XmlEnumValue("Satisfactory")
    SATISFACTORY("Satisfactory"),
    @XmlEnumValue("Borderline")
    BORDERLINE("Borderline"),
    @XmlEnumValue("Abnormal, but expected")
    ABNORMAL_BUT_EXPECTED("Abnormal, but expected"),
    @XmlEnumValue("Abnormal")
    ABNORMAL("Abnormal"),
    @XmlEnumValue("Specimen lost / unusable")
    SPECIMEN_LOST_UNUSABLE("Specimen lost / unusable"),
    @XmlEnumValue("Positive")
    POSITIVE("Positive"),
    @XmlEnumValue("Negative")
    NEGATIVE("Negative"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown"),
    @XmlEnumValue("Not responded to invitation")
    NOT_RESPONDED_TO_INVITATION("Not responded to invitation");
    private final String value;

    ReportResult(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReportResult fromValue(String v) {
        for (ReportResult c: ReportResult.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

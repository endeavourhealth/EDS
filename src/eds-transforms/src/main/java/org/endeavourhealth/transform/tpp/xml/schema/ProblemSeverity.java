
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProblemSeverity.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ProblemSeverity">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Major Episode"/>
 *     &lt;enumeration value="Minor Episode"/>
 *     &lt;enumeration value="Major CsvEvent"/>
 *     &lt;enumeration value="Minor CsvEvent"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ProblemSeverity")
@XmlEnum
public enum ProblemSeverity {

    @XmlEnumValue("Major Episode")
    MAJOR_EPISODE("Major Episode"),
    @XmlEnumValue("Minor Episode")
    MINOR_EPISODE("Minor Episode"),
    @XmlEnumValue("Major CsvEvent")
    MAJOR_EVENT("Major CsvEvent"),
    @XmlEnumValue("Minor CsvEvent")
    MINOR_EVENT("Minor CsvEvent");
    private final String value;

    ProblemSeverity(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProblemSeverity fromValue(String v) {
        for (ProblemSeverity c: ProblemSeverity.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

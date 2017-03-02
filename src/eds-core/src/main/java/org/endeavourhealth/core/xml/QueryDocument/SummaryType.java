
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for summaryType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="summaryType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="any"/>
 *     &lt;enumeration value="count"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "summaryType")
@XmlEnum
public enum SummaryType {

    @XmlEnumValue("any")
    ANY("any"),
    @XmlEnumValue("count")
    COUNT("count");
    private final String value;

    SummaryType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SummaryType fromValue(String v) {
        for (SummaryType c: SummaryType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}


package org.endeavourhealth.transform.enterprise.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for date_precision.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="date_precision">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="year"/>
 *     &lt;enumeration value="month"/>
 *     &lt;enumeration value="day"/>
 *     &lt;enumeration value="minute"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "date_precision")
@XmlEnum
public enum DatePrecision {

    @XmlEnumValue("year")
    YEAR("year"),
    @XmlEnumValue("month")
    MONTH("month"),
    @XmlEnumValue("day")
    DAY("day"),
    @XmlEnumValue("minute")
    MINUTE("minute");
    private final String value;

    DatePrecision(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DatePrecision fromValue(String v) {
        for (DatePrecision c: DatePrecision.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

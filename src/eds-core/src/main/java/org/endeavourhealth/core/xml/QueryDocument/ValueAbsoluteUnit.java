
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for valueAbsoluteUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="valueAbsoluteUnit">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="numeric"/>
 *     &lt;enumeration value="date"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "valueAbsoluteUnit")
@XmlEnum
public enum ValueAbsoluteUnit {

    @XmlEnumValue("numeric")
    NUMERIC("numeric"),
    @XmlEnumValue("date")
    DATE("date");
    private final String value;

    ValueAbsoluteUnit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ValueAbsoluteUnit fromValue(String v) {
        for (ValueAbsoluteUnit c: ValueAbsoluteUnit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

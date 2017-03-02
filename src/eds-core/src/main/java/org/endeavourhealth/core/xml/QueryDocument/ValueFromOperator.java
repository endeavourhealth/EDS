
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for valueFromOperator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="valueFromOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="greaterThan"/>
 *     &lt;enumeration value="greaterThanOrEqualTo"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "valueFromOperator")
@XmlEnum
public enum ValueFromOperator {

    @XmlEnumValue("greaterThan")
    GREATER_THAN("greaterThan"),
    @XmlEnumValue("greaterThanOrEqualTo")
    GREATER_THAN_OR_EQUAL_TO("greaterThanOrEqualTo");
    private final String value;

    ValueFromOperator(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ValueFromOperator fromValue(String v) {
        for (ValueFromOperator c: ValueFromOperator.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

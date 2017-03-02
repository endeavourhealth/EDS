
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for valueToOperator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="valueToOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="lessThan"/>
 *     &lt;enumeration value="lessThanOrEqualTo"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "valueToOperator")
@XmlEnum
public enum ValueToOperator {

    @XmlEnumValue("lessThan")
    LESS_THAN("lessThan"),
    @XmlEnumValue("lessThanOrEqualTo")
    LESS_THAN_OR_EQUAL_TO("lessThanOrEqualTo");
    private final String value;

    ValueToOperator(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ValueToOperator fromValue(String v) {
        for (ValueToOperator c: ValueToOperator.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}


package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for variableFunction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="variableFunction">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="count"/>
 *     &lt;enumeration value="average"/>
 *     &lt;enumeration value="minimum"/>
 *     &lt;enumeration value="maximum"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "variableFunction")
@XmlEnum
public enum VariableFunction {

    @XmlEnumValue("count")
    COUNT("count"),
    @XmlEnumValue("average")
    AVERAGE("average"),
    @XmlEnumValue("minimum")
    MINIMUM("minimum"),
    @XmlEnumValue("maximum")
    MAXIMUM("maximum");
    private final String value;

    VariableFunction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VariableFunction fromValue(String v) {
        for (VariableFunction c: VariableFunction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

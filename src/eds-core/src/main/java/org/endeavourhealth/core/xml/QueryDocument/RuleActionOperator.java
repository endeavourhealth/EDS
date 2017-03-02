
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ruleActionOperator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ruleActionOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="include"/>
 *     &lt;enumeration value="noAction"/>
 *     &lt;enumeration value="gotoRules"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ruleActionOperator")
@XmlEnum
public enum RuleActionOperator {

    @XmlEnumValue("include")
    INCLUDE("include"),
    @XmlEnumValue("noAction")
    NO_ACTION("noAction"),
    @XmlEnumValue("gotoRules")
    GOTO_RULES("gotoRules");
    private final String value;

    RuleActionOperator(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RuleActionOperator fromValue(String v) {
        for (RuleActionOperator c: RuleActionOperator.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

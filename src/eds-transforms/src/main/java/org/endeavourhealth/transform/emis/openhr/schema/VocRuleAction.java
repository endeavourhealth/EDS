
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.RuleAction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.RuleAction">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="NEXT"/>
 *     &lt;enumeration value="SELECT"/>
 *     &lt;enumeration value="REJECT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.RuleAction", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocRuleAction {


    /**
     * Move to next rule
     * 
     */
    NEXT,

    /**
     * Select items into final result
     * 
     */
    SELECT,

    /**
     * Reject Items from final result
     * 
     */
    REJECT;

    public String value() {
        return name();
    }

    public static VocRuleAction fromValue(String v) {
        return valueOf(v);
    }

}

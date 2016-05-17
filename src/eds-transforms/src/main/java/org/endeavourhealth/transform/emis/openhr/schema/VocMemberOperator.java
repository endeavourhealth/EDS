
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.MemberOperator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.MemberOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="AND"/>
 *     &lt;enumeration value="OR"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.MemberOperator", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocMemberOperator {


    /**
     * And operator
     * 
     */
    AND,

    /**
     * Or operator
     * 
     */
    OR;

    public String value() {
        return name();
    }

    public static VocMemberOperator fromValue(String v) {
        return valueOf(v);
    }

}

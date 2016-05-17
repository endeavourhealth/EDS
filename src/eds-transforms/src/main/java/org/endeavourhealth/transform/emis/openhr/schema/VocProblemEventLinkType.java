
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ProblemEventLinkType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ProblemEventLinkType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="ASS"/>
 *     &lt;enumeration value="FOL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ProblemEventLinkType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocProblemEventLinkType {


    /**
     * Associated
     * 
     */
    ASS,

    /**
     * Episode Follow On
     * 
     */
    FOL;

    public String value() {
        return name();
    }

    public static VocProblemEventLinkType fromValue(String v) {
        return valueOf(v);
    }

}


package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ProblemRelationshipType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ProblemRelationshipType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="COMB"/>
 *     &lt;enumeration value="GRP"/>
 *     &lt;enumeration value="REP"/>
 *     &lt;enumeration value="EVO"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ProblemRelationshipType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocProblemRelationshipType {


    /**
     * Combining
     * 
     */
    COMB,

    /**
     * Grouping
     * 
     */
    GRP,

    /**
     * Replacement
     * 
     */
    REP,

    /**
     * Evolution
     * 
     */
    EVO;

    public String value() {
        return name();
    }

    public static VocProblemRelationshipType fromValue(String v) {
        return valueOf(v);
    }

}

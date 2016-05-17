
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ProblemStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ProblemStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="I"/>
 *     &lt;enumeration value="HP"/>
 *     &lt;enumeration value="PP"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ProblemStatus", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocProblemStatus {


    /**
     * Active Problem
     * 
     */
    A,

    /**
     * Inactive Problem
     * 
     */
    I,

    /**
     * Health Promotion
     * 
     */
    HP,

    /**
     * Potential Problem
     * 
     */
    PP;

    public String value() {
        return name();
    }

    public static VocProblemStatus fromValue(String v) {
        return valueOf(v);
    }

}

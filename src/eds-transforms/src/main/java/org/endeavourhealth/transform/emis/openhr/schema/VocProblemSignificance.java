
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ProblemSignificance.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ProblemSignificance">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="S"/>
 *     &lt;enumeration value="M"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ProblemSignificance", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocProblemSignificance {


    /**
     * Significant
     * 
     */
    S,

    /**
     * Not Significant (Minor)
     * 
     */
    M;

    public String value() {
        return name();
    }

    public static VocProblemSignificance fromValue(String v) {
        return valueOf(v);
    }

}

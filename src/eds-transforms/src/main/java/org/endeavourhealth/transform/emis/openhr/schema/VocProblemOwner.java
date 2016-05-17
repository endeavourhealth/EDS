
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ProblemOwner.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ProblemOwner">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="IND"/>
 *     &lt;enumeration value="FAM"/>
 *     &lt;enumeration value="COM"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ProblemOwner", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocProblemOwner {


    /**
     * Individual
     * 
     */
    IND,

    /**
     * Family
     * 
     */
    FAM,

    /**
     * Community
     * 
     */
    COM;

    public String value() {
        return name();
    }

    public static VocProblemOwner fromValue(String v) {
        return valueOf(v);
    }

}

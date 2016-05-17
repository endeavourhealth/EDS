
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.CaseloadPatientStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.CaseloadPatientStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="REG"/>
 *     &lt;enumeration value="LEFT"/>
 *     &lt;enumeration value="DEAD"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.CaseloadPatientStatus", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocCaseloadPatientStatus {


    /**
     * Registered
     * 
     */
    REG,

    /**
     * Left
     * 
     */
    LEFT,

    /**
     * Dead
     * 
     */
    DEAD;

    public String value() {
        return name();
    }

    public static VocCaseloadPatientStatus fromValue(String v) {
        return valueOf(v);
    }

}

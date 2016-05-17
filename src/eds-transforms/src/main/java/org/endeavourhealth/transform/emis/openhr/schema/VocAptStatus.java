
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.AptStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.AptStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="SA"/>
 *     &lt;enumeration value="SB"/>
 *     &lt;enumeration value="PA"/>
 *     &lt;enumeration value="PSI"/>
 *     &lt;enumeration value="PL"/>
 *     &lt;enumeration value="DNA"/>
 *     &lt;enumeration value="PWO"/>
 *     &lt;enumeration value="VIS"/>
 *     &lt;enumeration value="TC"/>
 *     &lt;enumeration value="TNI"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.AptStatus", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocAptStatus {


    /**
     * Slot Available
     * 
     */
    SA,

    /**
     * Slot Booked
     * 
     */
    SB,

    /**
     * Patient Arrived
     * 
     */
    PA,

    /**
     * Patient Sent In
     * 
     */
    PSI,

    /**
     * Patient Left
     * 
     */
    PL,

    /**
     * Patient DNA
     * 
     */
    DNA,

    /**
     * Patient Walked Out
     * 
     */
    PWO,

    /**
     * Visited
     * 
     */
    VIS,

    /**
     * Telephone - Complete
     * 
     */
    TC,

    /**
     * Telephone - Not In
     * 
     */
    TNI;

    public String value() {
        return name();
    }

    public static VocAptStatus fromValue(String v) {
        return valueOf(v);
    }

}

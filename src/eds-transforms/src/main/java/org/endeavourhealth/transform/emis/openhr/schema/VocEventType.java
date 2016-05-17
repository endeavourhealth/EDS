
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.EventType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.EventType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="OBS"/>
 *     &lt;enumeration value="MED"/>
 *     &lt;enumeration value="TR"/>
 *     &lt;enumeration value="INV"/>
 *     &lt;enumeration value="VAL"/>
 *     &lt;enumeration value="ISS"/>
 *     &lt;enumeration value="ATT"/>
 *     &lt;enumeration value="REF"/>
 *     &lt;enumeration value="DRY"/>
 *     &lt;enumeration value="ALT"/>
 *     &lt;enumeration value="ALL"/>
 *     &lt;enumeration value="FH"/>
 *     &lt;enumeration value="IMM"/>
 *     &lt;enumeration value="REP"/>
 *     &lt;enumeration value="OH"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.EventType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocEventType {


    /**
     * Observation
     * 
     */
    OBS,

    /**
     * Medication
     * 
     */
    MED,

    /**
     * Test Request
     * 
     */
    TR,

    /**
     * Investigation
     * 
     */
    INV,

    /**
     * Value
     * 
     */
    VAL,

    /**
     * Medication Issue
     * 
     */
    ISS,

    /**
     * Attachment
     * 
     */
    ATT,

    /**
     * Referral
     * 
     */
    REF,

    /**
     * Diary
     * 
     */
    DRY,

    /**
     * Alert
     * 
     */
    ALT,

    /**
     * Allergy
     * 
     */
    ALL,

    /**
     * Family history
     * 
     */
    FH,

    /**
     * Immunisation
     * 
     */
    IMM,

    /**
     * Report
     * 
     */
    REP,

    /**
     * Order Header
     * 
     */
    OH;

    public String value() {
        return name();
    }

    public static VocEventType fromValue(String v) {
        return valueOf(v);
    }

}

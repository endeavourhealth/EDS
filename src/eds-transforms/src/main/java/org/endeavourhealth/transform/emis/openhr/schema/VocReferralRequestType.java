
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ReferralRequestType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ReferralRequestType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="INV"/>
 *     &lt;enumeration value="TRT"/>
 *     &lt;enumeration value="MA"/>
 *     &lt;enumeration value="PRC"/>
 *     &lt;enumeration value="PR"/>
 *     &lt;enumeration value="UNK"/>
 *     &lt;enumeration value="SELF"/>
 *     &lt;enumeration value="OUT"/>
 *     &lt;enumeration value="ADM"/>
 *     &lt;enumeration value="DAY"/>
 *     &lt;enumeration value="COM"/>
 *     &lt;enumeration value="DOM"/>
 *     &lt;enumeration value="ASS"/>
 *     &lt;enumeration value="ASED"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ReferralRequestType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocReferralRequestType {


    /**
     * Investigation
     * 
     */
    INV,

    /**
     * Treatment
     * 
     */
    TRT,

    /**
     * Management Advice
     * 
     */
    MA,

    /**
     * Performance of a Procedure or Operation
     * 
     */
    PRC,

    /**
     * Patient Reassurance
     * 
     */
    PR,

    /**
     * Unknown
     * 
     */
    UNK,

    /**
     * Self referral
     * 
     */
    SELF,

    /**
     * Outpatient
     * 
     */
    OUT,

    /**
     * Admission
     * 
     */
    ADM,

    /**
     * Day care
     * 
     */
    DAY,

    /**
     * Community Care
     * 
     */
    COM,

    /**
     * Domiciliary visit
     * 
     */
    DOM,

    /**
     * Assessment
     * 
     */
    ASS,

    /**
     * Assessment and Education
     * 
     */
    ASED;

    public String value() {
        return name();
    }

    public static VocReferralRequestType fromValue(String v) {
        return valueOf(v);
    }

}

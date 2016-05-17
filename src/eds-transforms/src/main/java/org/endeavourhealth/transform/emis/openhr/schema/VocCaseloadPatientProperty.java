
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.CaseloadPatientProperty.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.CaseloadPatientProperty">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="WQ"/>
 *     &lt;enumeration value="WM"/>
 *     &lt;enumeration value="RM"/>
 *     &lt;enumeration value="RDM"/>
 *     &lt;enumeration value="FM"/>
 *     &lt;enumeration value="BSM"/>
 *     &lt;enumeration value="TPHA"/>
 *     &lt;enumeration value="RIL"/>
 *     &lt;enumeration value="BS"/>
 *     &lt;enumeration value="RA"/>
 *     &lt;enumeration value="FZ"/>
 *     &lt;enumeration value="DISP"/>
 *     &lt;enumeration value="MSM"/>
 *     &lt;enumeration value="DQ"/>
 *     &lt;enumeration value="EED"/>
 *     &lt;enumeration value="RD"/>
 *     &lt;enumeration value="RS"/>
 *     &lt;enumeration value="RESS"/>
 *     &lt;enumeration value="AWN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.CaseloadPatientProperty", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocCaseloadPatientProperty {


    /**
     * Walking Quarters
     * 
     */
    WQ,

    /**
     * Water Miles
     * 
     */
    WM,

    /**
     * Rural Mileage
     * 
     */
    RM,

    /**
     * Road Mileage
     * 
     */
    RDM,

    /**
     * Foot Milage
     * 
     */
    FM,

    /**
     * Blocked Special Marker
     * 
     */
    BSM,

    /**
     * Trading Partner HA
     * 
     */
    TPHA,

    /**
     * Residential Institute Location
     * 
     */
    RIL,

    /**
     * Branch Surgery Organisation
     * 
     */
    BS,

    /**
     * Records At
     * 
     */
    RA,

    /**
     * Freeze Flag
     * 
     */
    FZ,

    /**
     * Dispensing
     * 
     */
    DISP,

    /**
     * Medication Screen Message
     * 
     */
    MSM,

    /**
     * Difficult Quarters
     * 
     */
    DQ,

    /**
     * Exemption Expiry Date
     * 
     */
    EED,

    /**
     * Review Date
     * 
     */
    RD,

    /**
     * Reminder Sent
     * 
     */
    RS,

    /**
     * Resident Status
     * 
     */
    RESS,

    /**
     * Automatic Week Number
     * 
     */
    AWN;

    public String value() {
        return name();
    }

    public static VocCaseloadPatientProperty fromValue(String v) {
        return valueOf(v);
    }

}

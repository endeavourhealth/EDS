
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.EncounterHeading.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.EncounterHeading">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="ADD"/>
 *     &lt;enumeration value="PROB"/>
 *     &lt;enumeration value="HIST"/>
 *     &lt;enumeration value="EXAM"/>
 *     &lt;enumeration value="FH"/>
 *     &lt;enumeration value="SOC"/>
 *     &lt;enumeration value="COM"/>
 *     &lt;enumeration value="RES"/>
 *     &lt;enumeration value="MED"/>
 *     &lt;enumeration value="DRY"/>
 *     &lt;enumeration value="TR"/>
 *     &lt;enumeration value="REF"/>
 *     &lt;enumeration value="ALL"/>
 *     &lt;enumeration value="RA"/>
 *     &lt;enumeration value="SS"/>
 *     &lt;enumeration value="PR"/>
 *     &lt;enumeration value="HTGC"/>
 *     &lt;enumeration value="TP"/>
 *     &lt;enumeration value="CM"/>
 *     &lt;enumeration value="SERV"/>
 *     &lt;enumeration value="AT"/>
 *     &lt;enumeration value="ATD"/>
 *     &lt;enumeration value="RFC"/>
 *     &lt;enumeration value="HSS"/>
 *     &lt;enumeration value="AS"/>
 *     &lt;enumeration value="HN"/>
 *     &lt;enumeration value="AP"/>
 *     &lt;enumeration value="PC"/>
 *     &lt;enumeration value="VAL"/>
 *     &lt;enumeration value="TEMP"/>
 *     &lt;enumeration value="PRO"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.EncounterHeading", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocEncounterHeading {


    /**
     * Additional
     * 
     */
    ADD,

    /**
     * Problem
     * 
     */
    PROB,

    /**
     * History
     * 
     */
    HIST,

    /**
     * Examination
     * 
     */
    EXAM,

    /**
     * Family History
     * 
     */
    FH,

    /**
     * Social
     * 
     */
    SOC,

    /**
     * Comment
     * 
     */
    COM,

    /**
     * Result
     * 
     */
    RES,

    /**
     * Medication
     * 
     */
    MED,

    /**
     * Follow up
     * 
     */
    DRY,

    /**
     * Test Request
     * 
     */
    TR,

    /**
     * New Referral
     * 
     */
    REF,

    /**
     * Allergy
     * 
     */
    ALL,

    /**
     * Referral Activity
     * 
     */
    RA,

    /**
     * Signs/Symptoms
     * 
     */
    SS,

    /**
     * Problem Ratings
     * 
     */
    PR,

    /**
     * Health Teaching, Guidance, and Counselling
     * 
     */
    HTGC,

    /**
     * Treatments and Procedures
     * 
     */
    TP,

    /**
     * Case Management
     * 
     */
    CM,

    /**
     * Surveillance
     * 
     */
    SERV,

    /**
     * Assessment Tool
     * 
     */
    AT,

    /**
     * Assessment Tool Details
     * 
     */
    ATD,

    /**
     * Reason for Care
     * 
     */
    RFC,

    /**
     * Hide Sign/Symptom
     * 
     */
    HSS,

    /**
     * Assessment Section
     * 
     */
    AS,

    /**
     * Health Needs (Community)
     * 
     */
    HN,

    /**
     * Action Plan (Community)
     * 
     */
    AP,

    /**
     * Procedure Codes (Community)
     * 
     */
    PC,

    /**
     * Values (Community)
     * 
     */
    VAL,

    /**
     * Template Entry
     * 
     */
    TEMP,

    /**
     * Protocol
     * 
     */
    PRO;

    public String value() {
        return name();
    }

    public static VocEncounterHeading fromValue(String v) {
        return valueOf(v);
    }

}

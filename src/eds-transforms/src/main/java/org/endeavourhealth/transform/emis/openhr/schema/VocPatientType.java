
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.PatientType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.PatientType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="E"/>
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="PRIV"/>
 *     &lt;enumeration value="REG"/>
 *     &lt;enumeration value="TMP"/>
 *     &lt;enumeration value="COMM"/>
 *     &lt;enumeration value="DUM"/>
 *     &lt;enumeration value="OTH"/>
 *     &lt;enumeration value="CON"/>
 *     &lt;enumeration value="MAT"/>
 *     &lt;enumeration value="CHS"/>
 *     &lt;enumeration value="WI"/>
 *     &lt;enumeration value="MIS"/>
 *     &lt;enumeration value="SEX"/>
 *     &lt;enumeration value="PRE"/>
 *     &lt;enumeration value="YEL"/>
 *     &lt;enumeration value="DER"/>
 *     &lt;enumeration value="DIA"/>
 *     &lt;enumeration value="RHM"/>
 *     &lt;enumeration value="CHR"/>
 *     &lt;enumeration value="CHC"/>
 *     &lt;enumeration value="ULT"/>
 *     &lt;enumeration value="BCG"/>
 *     &lt;enumeration value="VAS"/>
 *     &lt;enumeration value="ACU"/>
 *     &lt;enumeration value="REF"/>
 *     &lt;enumeration value="HYP"/>
 *     &lt;enumeration value="OOH"/>
 *     &lt;enumeration value="RBN"/>
 *     &lt;enumeration value="ANT"/>
 *     &lt;enumeration value="AUD"/>
 *     &lt;enumeration value="GYN"/>
 *     &lt;enumeration value="DOP"/>
 *     &lt;enumeration value="SEC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.PatientType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocPatientType {


    /**
     * EMG
     * 
     */
    E,

    /**
     * Immediately necessary
     * 
     */
    IN,

    /**
     * Private
     * 
     */
    PRIV,

    /**
     * Regular
     * 
     */
    REG,

    /**
     * Temporary
     * 
     */
    TMP,

    /**
     * Community Registered
     * 
     */
    COMM,

    /**
     * Dummy
     * 
     */
    DUM,

    /**
     * Other
     * 
     */
    OTH,

    /**
     * Contraceptive Services
     * 
     */
    CON,

    /**
     * Maternity Services
     * 
     */
    MAT,

    /**
     * Child Health Services
     * 
     */
    CHS,

    /**
     * Walk-In Patient
     * 
     */
    WI,

    /**
     * Minor Surgery
     * 
     */
    MIS,

    /**
     * Sexual Health
     * 
     */
    SEX,

    /**
     * Pre Registration
     * 
     */
    PRE,

    /**
     * Yellow Fever
     * 
     */
    YEL,

    /**
     * Dermatology
     * 
     */
    DER,

    /**
     * Diabetic
     * 
     */
    DIA,

    /**
     * Rheumatology
     * 
     */
    RHM,

    /**
     * Chiropody
     * 
     */
    CHR,

    /**
     * Coronary Health Checks
     * 
     */
    CHC,

    /**
     * Ultrasound
     * 
     */
    ULT,

    /**
     * BCG Clinic
     * 
     */
    BCG,

    /**
     * Vasectomy
     * 
     */
    VAS,

    /**
     * Acupuncture
     * 
     */
    ACU,

    /**
     * Relexology
     * 
     */
    REF,

    /**
     * Hypnotherapy
     * 
     */
    HYP,

    /**
     * Out of Hours
     * 
     */
    OOH,

    /**
     * Rehabilitation
     * 
     */
    RBN,

    /**
     * Ante Natal
     * 
     */
    ANT,

    /**
     * Audiology
     * 
     */
    AUD,

    /**
     * Gynaecology
     * 
     */
    GYN,

    /**
     * Doppler
     * 
     */
    DOP,

    /**
     * Secondary Registration
     * 
     */
    SEC;

    public String value() {
        return name();
    }

    public static VocPatientType fromValue(String v) {
        return valueOf(v);
    }

}

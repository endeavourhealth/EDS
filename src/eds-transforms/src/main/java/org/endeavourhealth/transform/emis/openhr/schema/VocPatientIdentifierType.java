
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.PatientIdentifierType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.PatientIdentifierType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="NHS"/>
 *     &lt;enumeration value="ONHS"/>
 *     &lt;enumeration value="CHI"/>
 *     &lt;enumeration value="HC"/>
 *     &lt;enumeration value="GRO"/>
 *     &lt;enumeration value="UPCI"/>
 *     &lt;enumeration value="MOD"/>
 *     &lt;enumeration value="SSH"/>
 *     &lt;enumeration value="INS"/>
 *     &lt;enumeration value="ARMY"/>
 *     &lt;enumeration value="RAF"/>
 *     &lt;enumeration value="NAVY"/>
 *     &lt;enumeration value="HOSP"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.PatientIdentifierType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocPatientIdentifierType {


    /**
     * NHS Number
     * 
     */
    NHS,

    /**
     * Old NHS Number
     * 
     */
    ONHS,

    /**
     * CHI Number
     * 
     */
    CHI,

    /**
     * HC Number
     * 
     */
    HC,

    /**
     * GRO Number
     * 
     */
    GRO,

    /**
     * UPCI Number
     * 
     */
    UPCI,

    /**
     * MOD Service Number
     * 
     */
    MOD,

    /**
     * SSH Number
     * 
     */
    SSH,

    /**
     * Insurance Number
     * 
     */
    INS,

    /**
     * Army Number
     * 
     */
    ARMY,

    /**
     * Royal Air Force Number
     * 
     */
    RAF,

    /**
     * Royal Navy Number
     * 
     */
    NAVY,

    /**
     * Hospital Number
     * 
     */
    HOSP;

    public String value() {
        return name();
    }

    public static VocPatientIdentifierType fromValue(String v) {
        return valueOf(v);
    }

}

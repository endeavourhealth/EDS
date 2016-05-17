
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ConsultationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ConsultationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="GP"/>
 *     &lt;enumeration value="ASS"/>
 *     &lt;enumeration value="INT"/>
 *     &lt;enumeration value="TFC"/>
 *     &lt;enumeration value="TTC"/>
 *     &lt;enumeration value="TFP"/>
 *     &lt;enumeration value="TTP"/>
 *     &lt;enumeration value="TFO"/>
 *     &lt;enumeration value="TTO"/>
 *     &lt;enumeration value="FFC"/>
 *     &lt;enumeration value="FFP"/>
 *     &lt;enumeration value="FFO"/>
 *     &lt;enumeration value="OC"/>
 *     &lt;enumeration value="HV"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ConsultationType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocConsultationType {


    /**
     * GP Consultation
     * 
     */
    GP,

    /**
     * Assessment
     * 
     */
    ASS,

    /**
     * Intervention
     * 
     */
    INT,

    /**
     * Telephone call from client
     * 
     */
    TFC,

    /**
     * Telephone call to client
     * 
     */
    TTC,

    /**
     * Telephone call from parent/guardian
     * 
     */
    TFP,

    /**
     * Telephone call to parent/guardian
     * 
     */
    TTP,

    /**
     * Telephone call from other
     * 
     */
    TFO,

    /**
     * Telephone call to other
     * 
     */
    TTO,

    /**
     * Face to face with client
     * 
     */
    FFC,

    /**
     * Face to face with parent/guardian
     * 
     */
    FFP,

    /**
     * Face to face with other
     * 
     */
    FFO,

    /**
     * Other communications
     * 
     */
    OC,

    /**
     * Home Visit
     * 
     */
    HV;

    public String value() {
        return name();
    }

    public static VocConsultationType fromValue(String v) {
        return valueOf(v);
    }

}

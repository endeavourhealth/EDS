
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.PopulationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.PopulationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="PATIENT"/>
 *     &lt;enumeration value="APPOINTMENT"/>
 *     &lt;enumeration value="USER"/>
 *     &lt;enumeration value="DRUG"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.PopulationType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocPopulationType {


    /**
     * Patient based population e.g. Patient IDs are stored as population results
     * 
     */
    PATIENT,

    /**
     * Appointmnt based population e.g. Appointment Slot IDs are stored as population results
     * 
     */
    APPOINTMENT,

    /**
     * User based population e.g. User IDs are stored as population results
     * 
     */
    USER,

    /**
     * Drug based population e.g. Drug IDs are stored as population results
     * 
     */
    DRUG;

    public String value() {
        return name();
    }

    public static VocPopulationType fromValue(String v) {
        return valueOf(v);
    }

}

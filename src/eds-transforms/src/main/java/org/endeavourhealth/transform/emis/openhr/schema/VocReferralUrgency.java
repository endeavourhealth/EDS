
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ReferralUrgency.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ReferralUrgency">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="R"/>
 *     &lt;enumeration value="S"/>
 *     &lt;enumeration value="U"/>
 *     &lt;enumeration value="D"/>
 *     &lt;enumeration value="W"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ReferralUrgency", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocReferralUrgency {


    /**
     * Routine
     * 
     */
    R,

    /**
     * Soon
     * 
     */
    S,

    /**
     * Urgent
     * 
     */
    U,

    /**
     * Dated
     * 
     */
    D,

    /**
     *  2 week wait
     * 
     */
    W;

    public String value() {
        return name();
    }

    public static VocReferralUrgency fromValue(String v) {
        return valueOf(v);
    }

}


package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ReferralTransport.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ReferralTransport">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="N"/>
 *     &lt;enumeration value="R"/>
 *     &lt;enumeration value="S"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ReferralTransport", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocReferralTransport {


    /**
     * None Required
     * 
     */
    N,

    /**
     * Required
     * 
     */
    R,

    /**
     * Stretcher
     * 
     */
    S;

    public String value() {
        return name();
    }

    public static VocReferralTransport fromValue(String v) {
        return valueOf(v);
    }

}


package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ReferralDirection.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ReferralDirection">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="NOTUSED"/>
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="OUT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ReferralDirection", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocReferralDirection {


    /**
     * This value is not used, it is used to allow c# to interact with our db whose Inbound =1 and Outbound = 2
     * 
     */
    NOTUSED,

    /**
     * Inbound Referral
     * 
     */
    IN,

    /**
     * Outbound Referral
     * 
     */
    OUT;

    public String value() {
        return name();
    }

    public static VocReferralDirection fromValue(String v) {
        return valueOf(v);
    }

}

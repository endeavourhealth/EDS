
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ConfidentialityPolicyNotificationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ConfidentialityPolicyNotificationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="NO"/>
 *     &lt;enumeration value="REPT"/>
 *     &lt;enumeration value="MESG"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ConfidentialityPolicyNotificationType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocConfidentialityPolicyNotificationType {


    /**
     * No Notification
     * 
     */
    NO,

    /**
     * Notify by Report
     * 
     */
    REPT,

    /**
     * Notify by Message
     * 
     */
    MESG;

    public String value() {
        return name();
    }

    public static VocConfidentialityPolicyNotificationType fromValue(String v) {
        return valueOf(v);
    }

}

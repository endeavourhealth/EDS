
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ReminderType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ReminderType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="UNK"/>
 *     &lt;enumeration value="L"/>
 *     &lt;enumeration value="T"/>
 *     &lt;enumeration value="FX"/>
 *     &lt;enumeration value="EM"/>
 *     &lt;enumeration value="SMS"/>
 *     &lt;enumeration value="RHS"/>
 *     &lt;enumeration value="V"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ReminderType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocReminderType {


    /**
     * Unknown
     * 
     */
    UNK,

    /**
     * Letter
     * 
     */
    L,

    /**
     * Telephone
     * 
     */
    T,

    /**
     * Fax
     * 
     */
    FX,

    /**
     * e-mail
     * 
     */
    EM,

    /**
     * SMS (Short Message Service)
     * 
     */
    SMS,

    /**
     * RHS (Right-hand side)
     * 
     */
    RHS,

    /**
     * Verbal
     * 
     */
    V;

    public String value() {
        return name();
    }

    public static VocReminderType fromValue(String v) {
        return valueOf(v);
    }

}

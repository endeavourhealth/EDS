
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ContactType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ContactType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="H"/>
 *     &lt;enumeration value="W"/>
 *     &lt;enumeration value="M"/>
 *     &lt;enumeration value="FX"/>
 *     &lt;enumeration value="EM"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ContactType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocContactType {


    /**
     * Home Phone Number
     * 
     */
    H,

    /**
     * Work Phone Number
     * 
     */
    W,

    /**
     * Mobile Phone Number
     * 
     */
    M,

    /**
     * Fax Number
     * 
     */
    FX,

    /**
     * Email Address
     * 
     */
    EM;

    public String value() {
        return name();
    }

    public static VocContactType fromValue(String v) {
        return valueOf(v);
    }

}

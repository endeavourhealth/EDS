
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.MessageAuthorSystemType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.MessageAuthorSystemType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="LV"/>
 *     &lt;enumeration value="GV"/>
 *     &lt;enumeration value="PCS"/>
 *     &lt;enumeration value="EMISWEB"/>
 *     &lt;enumeration value="EXT"/>
 *     &lt;enumeration value="GP2GP"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.MessageAuthorSystemType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocMessageAuthorSystemType {


    /**
     * EMIS LV clinical system
     * 
     */
    LV("LV"),

    /**
     * EMIS GV clinical system
     * 
     */
    GV("GV"),

    /**
     * EMIS PCS clinical system
     * 
     */
    PCS("PCS"),

    /**
     * EMISWEB clinical system
     * 
     */
    EMISWEB("EMISWEB"),

    /**
     * Clinical system external to EMIS
     * 
     */
    EXT("EXT"),
    @XmlEnumValue("GP2GP")
    GP_2_GP("GP2GP");
    private final String value;

    VocMessageAuthorSystemType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VocMessageAuthorSystemType fromValue(String v) {
        for (VocMessageAuthorSystemType c: VocMessageAuthorSystemType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

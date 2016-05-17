
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.PathMessageType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.PathMessageType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="ASTM"/>
 *     &lt;enumeration value="MEDRPT"/>
 *     &lt;enumeration value="NHS002"/>
 *     &lt;enumeration value="NHS003"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.PathMessageType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocPathMessageType {


    /**
     * ASTM
     * 
     */
    ASTM("ASTM"),

    /**
     * MEDRPT
     * 
     */
    MEDRPT("MEDRPT"),

    /**
     * NHS002
     * 
     */
    @XmlEnumValue("NHS002")
    NHS_002("NHS002"),

    /**
     * NHS003
     * 
     */
    @XmlEnumValue("NHS003")
    NHS_003("NHS003");
    private final String value;

    VocPathMessageType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VocPathMessageType fromValue(String v) {
        for (VocPathMessageType c: VocPathMessageType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

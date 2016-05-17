
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.InstanceType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.InstanceType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DEFAULT"/>
 *     &lt;enumeration value="HOW_AM_I_DRIVING"/>
 *     &lt;enumeration value="LOOK_AHEAD"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.InstanceType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocInstanceType {

    DEFAULT,
    HOW_AM_I_DRIVING,
    LOOK_AHEAD;

    public String value() {
        return name();
    }

    public static VocInstanceType fromValue(String v) {
        return valueOf(v);
    }

}

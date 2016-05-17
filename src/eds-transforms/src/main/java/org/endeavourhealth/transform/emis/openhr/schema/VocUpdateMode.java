
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.UpdateMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.UpdateMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="add"/>
 *     &lt;enumeration value="edit"/>
 *     &lt;enumeration value="delete"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.UpdateMode", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocUpdateMode {

    @XmlEnumValue("add")
    ADD("add"),
    @XmlEnumValue("edit")
    EDIT("edit"),
    @XmlEnumValue("delete")
    DELETE("delete"),
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    VocUpdateMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VocUpdateMode fromValue(String v) {
        for (VocUpdateMode c: VocUpdateMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

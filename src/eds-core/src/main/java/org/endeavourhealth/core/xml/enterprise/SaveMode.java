
package org.endeavourhealth.core.xml.enterprise;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for save_mode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="save_mode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="insert"/>
 *     &lt;enumeration value="update"/>
 *     &lt;enumeration value="delete"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "save_mode")
@XmlEnum
public enum SaveMode {

    @XmlEnumValue("insert")
    INSERT("insert"),
    @XmlEnumValue("update")
    UPDATE("update"),
    @XmlEnumValue("delete")
    DELETE("delete");
    private final String value;

    SaveMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SaveMode fromValue(String v) {
        for (SaveMode c: SaveMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

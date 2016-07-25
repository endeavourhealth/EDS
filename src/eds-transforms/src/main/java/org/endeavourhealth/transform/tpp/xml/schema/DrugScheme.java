
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DrugScheme.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DrugScheme">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Multilex"/>
 *     &lt;enumeration value="Dmd"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DrugScheme")
@XmlEnum
public enum DrugScheme {

    @XmlEnumValue("Multilex")
    MULTILEX("Multilex"),
    @XmlEnumValue("Dmd")
    DMD("Dmd");
    private final String value;

    DrugScheme(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DrugScheme fromValue(String v) {
        for (DrugScheme c: DrugScheme.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

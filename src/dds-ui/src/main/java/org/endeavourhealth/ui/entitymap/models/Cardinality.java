
package org.endeavourhealth.ui.entitymap.models;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cardinality.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="cardinality">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="single"/>
 *     &lt;enumeration value="multiple"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "cardinality")
@XmlEnum
public enum Cardinality {

    @XmlEnumValue("single")
    SINGLE("single"),
    @XmlEnumValue("multiple")
    MULTIPLE("multiple");
    private final String value;

    Cardinality(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Cardinality fromValue(String v) {
        for (Cardinality c: Cardinality.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

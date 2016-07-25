
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DrugSensitivityType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DrugSensitivityType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Drug"/>
 *     &lt;enumeration value="Ingredient"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DrugSensitivityType")
@XmlEnum
public enum DrugSensitivityType {

    @XmlEnumValue("Drug")
    DRUG("Drug"),
    @XmlEnumValue("Ingredient")
    INGREDIENT("Ingredient");
    private final String value;

    DrugSensitivityType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DrugSensitivityType fromValue(String v) {
        for (DrugSensitivityType c: DrugSensitivityType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}


package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MedicationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MedicationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Other medication"/>
 *     &lt;enumeration value="NHS medication"/>
 *     &lt;enumeration value="Private issue"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MedicationType")
@XmlEnum
public enum MedicationType {

    @XmlEnumValue("Other medication")
    OTHER_MEDICATION("Other medication"),
    @XmlEnumValue("NHS medication")
    NHS_MEDICATION("NHS medication"),
    @XmlEnumValue("Private issue")
    PRIVATE_ISSUE("Private issue");
    private final String value;

    MedicationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MedicationType fromValue(String v) {
        for (MedicationType c: MedicationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

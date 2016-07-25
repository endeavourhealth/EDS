
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CodeScheme.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CodeScheme">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CTV3"/>
 *     &lt;enumeration value="Snomed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CodeScheme")
@XmlEnum
public enum CodeScheme {

    @XmlEnumValue("CTV3")
    CTV_3("CTV3"),
    @XmlEnumValue("Snomed")
    SNOMED("Snomed");
    private final String value;

    CodeScheme(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CodeScheme fromValue(String v) {
        for (CodeScheme c: CodeScheme.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

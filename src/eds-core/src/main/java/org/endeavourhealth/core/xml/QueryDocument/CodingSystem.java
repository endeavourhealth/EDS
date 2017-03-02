
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for codingSystem.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="codingSystem">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="EMISReadV2"/>
 *     &lt;enumeration value="DMD"/>
 *     &lt;enumeration value="SnomedCt"/>
 *     &lt;enumeration value="CTV3"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "codingSystem")
@XmlEnum
public enum CodingSystem {

    @XmlEnumValue("EMISReadV2")
    EMIS_READ_V_2("EMISReadV2"),
    DMD("DMD"),
    @XmlEnumValue("SnomedCt")
    SNOMED_CT("SnomedCt"),
    @XmlEnumValue("CTV3")
    CTV_3("CTV3");
    private final String value;

    CodingSystem(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CodingSystem fromValue(String v) {
        for (CodingSystem c: CodingSystem.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

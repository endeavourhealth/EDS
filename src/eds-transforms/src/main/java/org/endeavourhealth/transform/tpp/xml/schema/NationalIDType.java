
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NationalIDType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="NationalIDType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="GMC"/>
 *     &lt;enumeration value="NMCC"/>
 *     &lt;enumeration value="UKCC"/>
 *     &lt;enumeration value="Other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "NationalIDType")
@XmlEnum
public enum NationalIDType {

    GMC("GMC"),
    NMCC("NMCC"),
    UKCC("UKCC"),
    @XmlEnumValue("Other")
    OTHER("Other");
    private final String value;

    NationalIDType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NationalIDType fromValue(String v) {
        for (NationalIDType c: NationalIDType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

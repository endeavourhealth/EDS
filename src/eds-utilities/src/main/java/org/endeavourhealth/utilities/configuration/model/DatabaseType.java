
package org.endeavourhealth.utilities.configuration.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DatabaseType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DatabaseType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="sftpreader"/>
 *     &lt;enumeration value="hl7receiver"/>
 *     &lt;enumeration value="logback"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DatabaseType")
@XmlEnum
public enum DatabaseType {

    @XmlEnumValue("sftpreader")
    SFTPREADER("sftpreader"),
    @XmlEnumValue("hl7receiver")
    HL_7_RECEIVER("hl7receiver"),
    @XmlEnumValue("logback")
    LOGBACK("logback");
    private final String value;

    DatabaseType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DatabaseType fromValue(String v) {
        for (DatabaseType c: DatabaseType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

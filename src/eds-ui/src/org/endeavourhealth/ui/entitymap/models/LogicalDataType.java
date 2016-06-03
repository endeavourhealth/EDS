
package org.endeavourhealth.ui.entitymap.models;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for logicalDataType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="logicalDataType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="string"/>
 *     &lt;enumeration value="integer"/>
 *     &lt;enumeration value="float"/>
 *     &lt;enumeration value="datetime"/>
 *     &lt;enumeration value="date"/>
 *     &lt;enumeration value="uuid"/>
 *     &lt;enumeration value="code"/>
 *     &lt;enumeration value="dataValues"/>
 *     &lt;enumeration value="organisationODS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "logicalDataType")
@XmlEnum
public enum LogicalDataType {

    @XmlEnumValue("string")
    STRING("string"),
    @XmlEnumValue("integer")
    INTEGER("integer"),
    @XmlEnumValue("float")
    FLOAT("float"),
    @XmlEnumValue("datetime")
    DATETIME("datetime"),
    @XmlEnumValue("date")
    DATE("date"),
    @XmlEnumValue("uuid")
    UUID("uuid"),
    @XmlEnumValue("code")
    CODE("code"),
    @XmlEnumValue("dataValues")
    DATA_VALUES("dataValues"),
    @XmlEnumValue("organisationODS")
    ORGANISATION_ODS("organisationODS");
    private final String value;

    LogicalDataType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LogicalDataType fromValue(String v) {
        for (LogicalDataType c: LogicalDataType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

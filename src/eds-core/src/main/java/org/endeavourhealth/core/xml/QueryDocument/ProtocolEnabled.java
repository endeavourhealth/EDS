
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for protocolEnabled.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="protocolEnabled">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="TRUE"/>
 *     &lt;enumeration value="FALSE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "protocolEnabled")
@XmlEnum
public enum ProtocolEnabled {

    TRUE,
    FALSE;

    public String value() {
        return name();
    }

    public static ProtocolEnabled fromValue(String v) {
        return valueOf(v);
    }

}

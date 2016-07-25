
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LetterDirection.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LetterDirection">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Incoming"/>
 *     &lt;enumeration value="Outgoing"/>
 *     &lt;enumeration value="Other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LetterDirection")
@XmlEnum
public enum LetterDirection {

    @XmlEnumValue("Incoming")
    INCOMING("Incoming"),
    @XmlEnumValue("Outgoing")
    OUTGOING("Outgoing"),
    @XmlEnumValue("Other")
    OTHER("Other");
    private final String value;

    LetterDirection(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LetterDirection fromValue(String v) {
        for (LetterDirection c: LetterDirection.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

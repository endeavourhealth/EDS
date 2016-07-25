
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EventMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="EventMethod">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Text"/>
 *     &lt;enumeration value="Email"/>
 *     &lt;enumeration value="Face to face"/>
 *     &lt;enumeration value="Telephone"/>
 *     &lt;enumeration value="Letter"/>
 *     &lt;enumeration value="Fax"/>
 *     &lt;enumeration value="E-mail"/>
 *     &lt;enumeration value="DNA"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "EventMethod")
@XmlEnum
public enum EventMethod {

    @XmlEnumValue("Text")
    TEXT("Text"),
    @XmlEnumValue("Email")
    EMAIL("Email"),
    @XmlEnumValue("Face to face")
    FACE_TO_FACE("Face to face"),
    @XmlEnumValue("Telephone")
    TELEPHONE("Telephone"),
    @XmlEnumValue("Letter")
    LETTER("Letter"),
    @XmlEnumValue("Fax")
    FAX("Fax"),
    @XmlEnumValue("E-mail")
    E_MAIL("E-mail"),
    DNA("DNA");
    private final String value;

    EventMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EventMethod fromValue(String v) {
        for (EventMethod c: EventMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

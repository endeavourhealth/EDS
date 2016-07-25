
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Episodicity.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Episodicity">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="New"/>
 *     &lt;enumeration value="Ongoing"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Episodicity")
@XmlEnum
public enum Episodicity {

    @XmlEnumValue("New")
    NEW("New"),
    @XmlEnumValue("Ongoing")
    ONGOING("Ongoing");
    private final String value;

    Episodicity(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Episodicity fromValue(String v) {
        for (Episodicity c: Episodicity.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}


package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Sex.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Sex">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="M"/>
 *     &lt;enumeration value="F"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Sex")
@XmlEnum
public enum Sex {

    M,
    F;

    public String value() {
        return name();
    }

    public static Sex fromValue(String v) {
        return valueOf(v);
    }

}

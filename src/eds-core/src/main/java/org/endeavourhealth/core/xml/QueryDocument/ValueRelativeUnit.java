
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for valueRelativeUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="valueRelativeUnit">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="month"/>
 *     &lt;enumeration value="year"/>
 *     &lt;enumeration value="week"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "valueRelativeUnit")
@XmlEnum
public enum ValueRelativeUnit {

    @XmlEnumValue("month")
    MONTH("month"),
    @XmlEnumValue("year")
    YEAR("year"),
    @XmlEnumValue("week")
    WEEK("week");
    private final String value;

    ValueRelativeUnit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ValueRelativeUnit fromValue(String v) {
        for (ValueRelativeUnit c: ValueRelativeUnit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

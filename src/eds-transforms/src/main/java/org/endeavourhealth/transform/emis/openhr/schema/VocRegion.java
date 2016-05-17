
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.Region.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.Region">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ENGLAND"/>
 *     &lt;enumeration value="SCOTLAND"/>
 *     &lt;enumeration value="NORTHERN IRELAND"/>
 *     &lt;enumeration value="GUERNSEY"/>
 *     &lt;enumeration value="REPUBLIC OF IRELAND"/>
 *     &lt;enumeration value="JERSEY"/>
 *     &lt;enumeration value="ISLE OF MAN"/>
 *     &lt;enumeration value="EMIRATES"/>
 *     &lt;enumeration value="WALES"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.Region", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocRegion {

    ENGLAND("ENGLAND"),
    SCOTLAND("SCOTLAND"),
    @XmlEnumValue("NORTHERN IRELAND")
    NORTHERN_IRELAND("NORTHERN IRELAND"),
    GUERNSEY("GUERNSEY"),
    @XmlEnumValue("REPUBLIC OF IRELAND")
    REPUBLIC_OF_IRELAND("REPUBLIC OF IRELAND"),
    JERSEY("JERSEY"),
    @XmlEnumValue("ISLE OF MAN")
    ISLE_OF_MAN("ISLE OF MAN"),
    EMIRATES("EMIRATES"),
    WALES("WALES");
    private final String value;

    VocRegion(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VocRegion fromValue(String v) {
        for (VocRegion c: VocRegion.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

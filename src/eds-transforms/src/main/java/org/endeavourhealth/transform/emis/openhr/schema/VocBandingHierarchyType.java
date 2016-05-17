
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.BandingHierarchyType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.BandingHierarchyType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="drugChapter"/>
 *     &lt;enumeration value="readCode"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.BandingHierarchyType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocBandingHierarchyType {

    @XmlEnumValue("drugChapter")
    DRUG_CHAPTER("drugChapter"),
    @XmlEnumValue("readCode")
    READ_CODE("readCode");
    private final String value;

    VocBandingHierarchyType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VocBandingHierarchyType fromValue(String v) {
        for (VocBandingHierarchyType c: VocBandingHierarchyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

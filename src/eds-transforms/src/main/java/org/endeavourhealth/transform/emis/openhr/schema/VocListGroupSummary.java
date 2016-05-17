
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ListGroupSummary.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ListGroupSummary">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="count"/>
 *     &lt;enumeration value="exists"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ListGroupSummary", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocListGroupSummary {

    @XmlEnumValue("count")
    COUNT("count"),
    @XmlEnumValue("exists")
    EXISTS("exists");
    private final String value;

    VocListGroupSummary(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VocListGroupSummary fromValue(String v) {
        for (VocListGroupSummary c: VocListGroupSummary.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

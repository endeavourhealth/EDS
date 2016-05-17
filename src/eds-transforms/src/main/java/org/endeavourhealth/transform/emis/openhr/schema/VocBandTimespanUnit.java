
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.BandTimespanUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.BandTimespanUnit">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="DAYS"/>
 *     &lt;enumeration value="WEEKS"/>
 *     &lt;enumeration value="MONTHS"/>
 *     &lt;enumeration value="YEARS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.BandTimespanUnit", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocBandTimespanUnit {

    DAYS,
    WEEKS,
    MONTHS,
    YEARS;

    public String value() {
        return name();
    }

    public static VocBandTimespanUnit fromValue(String v) {
        return valueOf(v);
    }

}

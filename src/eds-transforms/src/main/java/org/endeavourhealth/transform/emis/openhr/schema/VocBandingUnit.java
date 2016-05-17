
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.BandingUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.BandingUnit">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="YEAR"/>
 *     &lt;enumeration value="HOUR"/>
 *     &lt;enumeration value="WEEKOFYEAR"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.BandingUnit", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocBandingUnit {

    YEAR,
    HOUR,
    WEEKOFYEAR;

    public String value() {
        return name();
    }

    public static VocBandingUnit fromValue(String v) {
        return valueOf(v);
    }

}

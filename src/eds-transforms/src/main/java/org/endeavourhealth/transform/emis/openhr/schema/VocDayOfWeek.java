
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.DayOfWeek.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.DayOfWeek">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="MON"/>
 *     &lt;enumeration value="TUE"/>
 *     &lt;enumeration value="WED"/>
 *     &lt;enumeration value="THU"/>
 *     &lt;enumeration value="FRI"/>
 *     &lt;enumeration value="SAT"/>
 *     &lt;enumeration value="SUN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.DayOfWeek", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocDayOfWeek {

    MON,
    TUE,
    WED,
    THU,
    FRI,
    SAT,
    SUN;

    public String value() {
        return name();
    }

    public static VocDayOfWeek fromValue(String v) {
        return valueOf(v);
    }

}

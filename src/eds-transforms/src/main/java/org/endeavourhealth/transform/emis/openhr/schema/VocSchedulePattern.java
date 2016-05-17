
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.SchedulePattern.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.SchedulePattern">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="ONCE"/>
 *     &lt;enumeration value="DAY"/>
 *     &lt;enumeration value="WEEK"/>
 *     &lt;enumeration value="MONTH"/>
 *     &lt;enumeration value="YEAR"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.SchedulePattern", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocSchedulePattern {


    /**
     * Run once
     * 
     */
    ONCE,

    /**
     * Daily
     * 
     */
    DAY,

    /**
     * Weekly
     * 
     */
    WEEK,

    /**
     * Monthly
     * 
     */
    MONTH,

    /**
     * Yearly
     * 
     */
    YEAR;

    public String value() {
        return name();
    }

    public static VocSchedulePattern fromValue(String v) {
        return valueOf(v);
    }

}

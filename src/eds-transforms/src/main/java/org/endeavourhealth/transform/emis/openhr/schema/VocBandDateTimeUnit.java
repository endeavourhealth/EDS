
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.BandDateTimeUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.BandDateTimeUnit">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="DATETIME"/>
 *     &lt;enumeration value="DATE"/>
 *     &lt;enumeration value="YEAR"/>
 *     &lt;enumeration value="MONTH"/>
 *     &lt;enumeration value="WEEKOFYEAR"/>
 *     &lt;enumeration value="DAYOFWEEK"/>
 *     &lt;enumeration value="HOUR"/>
 *     &lt;enumeration value="MINUTE"/>
 *     &lt;enumeration value="HOURMINUTE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.BandDateTimeUnit", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocBandDateTimeUnit {


    /**
     * Specific date + time (e.g. "1971-11-19 15:35")
     * 
     */
    DATETIME,

    /**
     * Specific date (e.g. 1971-11-19)
     * 
     */
    DATE,

    /**
     * Year
     * 
     */
    YEAR,

    /**
     * Numeric month.  1=Jan, 12=Dec
     * 
     */
    MONTH,

    /**
     * Numeric week. When is week 1???
     * 
     */
    WEEKOFYEAR,

    /**
     * MON, TUE, WED, THU, FRI, SAT, SUN
     * 
     */
    DAYOFWEEK,

    /**
     *  1-24
     * 
     */
    HOUR,

    /**
     *  1-60
     * 
     */
    MINUTE,

    /**
     * Eg 15:30
     * 
     */
    HOURMINUTE;

    public String value() {
        return name();
    }

    public static VocBandDateTimeUnit fromValue(String v) {
        return valueOf(v);
    }

}

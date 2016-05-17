
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ValueUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ValueUnit">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="DATE"/>
 *     &lt;enumeration value="YEAR"/>
 *     &lt;enumeration value="MONTH"/>
 *     &lt;enumeration value="WEEK"/>
 *     &lt;enumeration value="DAY"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ValueUnit", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocValueUnit {


    /**
     * Actual date (e.g. 19/11/1971)
     * 
     */
    DATE,

    /**
     * Year
     * 
     */
    YEAR,

    /**
     * Month
     * 
     */
    MONTH,

    /**
     * Week
     * 
     */
    WEEK,

    /**
     * Day
     * 
     */
    DAY;

    public String value() {
        return name();
    }

    public static VocValueUnit fromValue(String v) {
        return valueOf(v);
    }

}

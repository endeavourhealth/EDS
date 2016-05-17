
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.TimeUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.TimeUnit">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="Y"/>
 *     &lt;enumeration value="M"/>
 *     &lt;enumeration value="W"/>
 *     &lt;enumeration value="D"/>
 *     &lt;enumeration value="H"/>
 *     &lt;enumeration value="N"/>
 *     &lt;enumeration value="S"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.TimeUnit", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocTimeUnit {


    /**
     * Year
     * 
     */
    Y,

    /**
     * Month
     * 
     */
    M,

    /**
     * Week
     * 
     */
    W,

    /**
     * Day
     * 
     */
    D,

    /**
     * Hour
     * 
     */
    H,

    /**
     * Minute
     * 
     */
    N,

    /**
     * Second
     * 
     */
    S;

    public String value() {
        return name();
    }

    public static VocTimeUnit fromValue(String v) {
        return valueOf(v);
    }

}

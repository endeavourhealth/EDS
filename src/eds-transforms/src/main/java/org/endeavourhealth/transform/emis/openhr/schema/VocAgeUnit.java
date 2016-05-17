
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.AgeUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.AgeUnit">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="Y"/>
 *     &lt;enumeration value="M"/>
 *     &lt;enumeration value="W"/>
 *     &lt;enumeration value="D"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.AgeUnit", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocAgeUnit {


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
    D;

    public String value() {
        return name();
    }

    public static VocAgeUnit fromValue(String v) {
        return valueOf(v);
    }

}

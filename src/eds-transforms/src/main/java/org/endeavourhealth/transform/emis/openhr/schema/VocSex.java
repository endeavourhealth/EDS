
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.Sex.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.Sex">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="U"/>
 *     &lt;enumeration value="M"/>
 *     &lt;enumeration value="F"/>
 *     &lt;enumeration value="I"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.Sex", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocSex {


    /**
     * Unknown
     * 
     */
    U,

    /**
     * Male
     * 
     */
    M,

    /**
     * Female
     * 
     */
    F,

    /**
     * Indeterminate
     * 
     */
    I;

    public String value() {
        return name();
    }

    public static VocSex fromValue(String v) {
        return valueOf(v);
    }

}

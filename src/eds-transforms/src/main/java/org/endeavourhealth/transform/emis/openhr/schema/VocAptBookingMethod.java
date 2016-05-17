
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.AptBookingMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.AptBookingMethod">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="PRAC"/>
 *     &lt;enumeration value="EACC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.AptBookingMethod", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocAptBookingMethod {


    /**
     * Practice
     * 
     */
    PRAC,

    /**
     * EMIS Access
     * 
     */
    EACC;

    public String value() {
        return name();
    }

    public static VocAptBookingMethod fromValue(String v) {
        return valueOf(v);
    }

}

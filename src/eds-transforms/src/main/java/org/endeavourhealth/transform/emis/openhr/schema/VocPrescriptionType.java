
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.PrescriptionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.PrescriptionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="R"/>
 *     &lt;enumeration value="D"/>
 *     &lt;enumeration value="U"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.PrescriptionType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocPrescriptionType {


    /**
     * Acute
     * 
     */
    A,

    /**
     * Repeat
     * 
     */
    R,

    /**
     * Repeat Dispensed
     * 
     */
    D,

    /**
     * Automatic
     * 
     */
    U;

    public String value() {
        return name();
    }

    public static VocPrescriptionType fromValue(String v) {
        return valueOf(v);
    }

}

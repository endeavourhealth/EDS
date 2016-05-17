
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.SingleValueConstant.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.SingleValueConstant">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="SEARCHDATE"/>
 *     &lt;enumeration value="TODAY"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.SingleValueConstant", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocSingleValueConstant {


    /**
     * Actual search date
     * 
     */
    SEARCHDATE,

    /**
     * Todays date
     * 
     */
    TODAY;

    public String value() {
        return name();
    }

    public static VocSingleValueConstant fromValue(String v) {
        return valueOf(v);
    }

}

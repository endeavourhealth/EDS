
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.OrderPriority.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.OrderPriority">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="H"/>
 *     &lt;enumeration value="M"/>
 *     &lt;enumeration value="L"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.OrderPriority", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocOrderPriority {


    /**
     * High
     * 
     */
    H,

    /**
     * Medium
     * 
     */
    M,

    /**
     * Low
     * 
     */
    L;

    public String value() {
        return name();
    }

    public static VocOrderPriority fromValue(String v) {
        return valueOf(v);
    }

}

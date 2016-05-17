
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.RangeToOperator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.RangeToOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="LT"/>
 *     &lt;enumeration value="LTEQ"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.RangeToOperator", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocRangeToOperator {


    /**
     * Less than
     * 
     */
    LT,

    /**
     * Less than or equal
     * 
     */
    LTEQ;

    public String value() {
        return name();
    }

    public static VocRangeToOperator fromValue(String v) {
        return valueOf(v);
    }

}


package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.RangeFromOperator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.RangeFromOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="GT"/>
 *     &lt;enumeration value="GTEQ"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.RangeFromOperator", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocRangeFromOperator {


    /**
     * Greater than
     * 
     */
    GT,

    /**
     * Greater than or equal
     * 
     */
    GTEQ;

    public String value() {
        return name();
    }

    public static VocRangeFromOperator fromValue(String v) {
        return valueOf(v);
    }

}

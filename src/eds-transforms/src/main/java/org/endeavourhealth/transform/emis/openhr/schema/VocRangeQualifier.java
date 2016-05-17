
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.RangeQualifier.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.RangeQualifier">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="U"/>
 *     &lt;enumeration value="T"/>
 *     &lt;enumeration value="X"/>
 *     &lt;enumeration value="R"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.RangeQualifier", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocRangeQualifier {


    /**
     * Unqualified
     * 
     */
    U,

    /**
     * Therapeutic Interval
     * 
     */
    T,

    /**
     * Toxic Interval
     * 
     */
    X,

    /**
     * Recommended Interval
     * 
     */
    R;

    public String value() {
        return name();
    }

    public static VocRangeQualifier fromValue(String v) {
        return valueOf(v);
    }

}

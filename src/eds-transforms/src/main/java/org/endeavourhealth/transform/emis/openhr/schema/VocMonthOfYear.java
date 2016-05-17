
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.MonthOfYear.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.MonthOfYear">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="JAN"/>
 *     &lt;enumeration value="FEB"/>
 *     &lt;enumeration value="MAR"/>
 *     &lt;enumeration value="APR"/>
 *     &lt;enumeration value="MAY"/>
 *     &lt;enumeration value="JUN"/>
 *     &lt;enumeration value="JUL"/>
 *     &lt;enumeration value="AUG"/>
 *     &lt;enumeration value="SEP"/>
 *     &lt;enumeration value="OCT"/>
 *     &lt;enumeration value="NOV"/>
 *     &lt;enumeration value="DEC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.MonthOfYear", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocMonthOfYear {

    JAN,
    FEB,
    MAR,
    APR,
    MAY,
    JUN,
    JUL,
    AUG,
    SEP,
    OCT,
    NOV,
    DEC;

    public String value() {
        return name();
    }

    public static VocMonthOfYear fromValue(String v) {
        return valueOf(v);
    }

}


package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.DatePart.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.DatePart">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="U"/>
 *     &lt;enumeration value="Y"/>
 *     &lt;enumeration value="YM"/>
 *     &lt;enumeration value="YMD"/>
 *     &lt;enumeration value="YMDT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.DatePart", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocDatePart {


    /**
     * Unknown Date
     * 
     */
    U,

    /**
     * Year e.g. to indicate 1999 write: 1999-01-01T00:00:00
     * 
     */
    Y,

    /**
     * Year and month e.g. to indicate May, 1999 write: 1999-05-01T00:00:00
     * 
     */
    YM,

    /**
     * Year, month and day e.g. to indicate May the 31st, 1999 write: 1999-05-31T00:00:00
     * 
     */
    YMD,

    /**
     * Year, month, day and time e.g. to indicate 1:20 pm on May the 31st, 1999 write: 1999-05-31T13:20:00
     * 
     */
    YMDT;

    public String value() {
        return name();
    }

    public static VocDatePart fromValue(String v) {
        return valueOf(v);
    }

}

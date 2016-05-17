
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.EDIResultStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.EDIResultStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="FIN"/>
 *     &lt;enumeration value="INT"/>
 *     &lt;enumeration value="NA"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.EDIResultStatus", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocEDIResultStatus {


    /**
     * Final/Complete
     * 
     */
    FIN,

    /**
     * Interim
     * 
     */
    INT,

    /**
     * Not Available
     * 
     */
    NA;

    public String value() {
        return name();
    }

    public static VocEDIResultStatus fromValue(String v) {
        return valueOf(v);
    }

}

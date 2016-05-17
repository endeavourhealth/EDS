
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.RequestMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.RequestMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="ONLINE"/>
 *     &lt;enumeration value="OFFLINE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.RequestMode", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocRequestMode {


    /**
     * Online
     * 
     */
    ONLINE,

    /**
     * Offline
     * 
     */
    OFFLINE;

    public String value() {
        return name();
    }

    public static VocRequestMode fromValue(String v) {
        return valueOf(v);
    }

}

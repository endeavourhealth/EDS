
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.TaskPriority.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.TaskPriority">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="HIGH"/>
 *     &lt;enumeration value="LOW"/>
 *     &lt;enumeration value="NORMAL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.TaskPriority", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocTaskPriority {

    HIGH,
    LOW,
    NORMAL;

    public String value() {
        return name();
    }

    public static VocTaskPriority fromValue(String v) {
        return valueOf(v);
    }

}

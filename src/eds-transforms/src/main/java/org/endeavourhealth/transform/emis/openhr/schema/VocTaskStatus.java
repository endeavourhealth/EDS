
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.TaskStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.TaskStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ACTIVE"/>
 *     &lt;enumeration value="COMPLETE"/>
 *     &lt;enumeration value="DELETED"/>
 *     &lt;enumeration value="ARCHIVED"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.TaskStatus", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocTaskStatus {

    ACTIVE,
    COMPLETE,
    DELETED,
    ARCHIVED;

    public String value() {
        return name();
    }

    public static VocTaskStatus fromValue(String v) {
        return valueOf(v);
    }

}

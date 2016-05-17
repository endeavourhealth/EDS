
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.OrderDirection.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.OrderDirection">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="ASC"/>
 *     &lt;enumeration value="DESC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.OrderDirection", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocOrderDirection {


    /**
     * Ascending
     * 
     */
    ASC,

    /**
     * Descending
     * 
     */
    DESC;

    public String value() {
        return name();
    }

    public static VocOrderDirection fromValue(String v) {
        return valueOf(v);
    }

}

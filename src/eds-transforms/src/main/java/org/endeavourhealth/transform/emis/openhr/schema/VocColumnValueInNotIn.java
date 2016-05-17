
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ColumnValueInNotIn.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ColumnValueInNotIn">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="NOTIN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ColumnValueInNotIn", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocColumnValueInNotIn {


    /**
     * In column value
     * 
     */
    IN,

    /**
     * Not in column value
     * 
     */
    NOTIN;

    public String value() {
        return name();
    }

    public static VocColumnValueInNotIn fromValue(String v) {
        return valueOf(v);
    }

}

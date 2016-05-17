
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.DrugStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.DrugStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="C"/>
 *     &lt;enumeration value="N"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.DrugStatus", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocDrugStatus {


    /**
     * Active
     * 
     */
    A,

    /**
     * Cancelled
     * 
     */
    C,

    /**
     * Never Active
     * 
     */
    N;

    public String value() {
        return name();
    }

    public static VocDrugStatus fromValue(String v) {
        return valueOf(v);
    }

}

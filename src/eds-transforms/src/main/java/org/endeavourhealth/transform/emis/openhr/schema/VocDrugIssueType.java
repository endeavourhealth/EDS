
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.DrugIssueType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.DrugIssueType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="P"/>
 *     &lt;enumeration value="Q"/>
 *     &lt;enumeration value="H"/>
 *     &lt;enumeration value="O"/>
 *     &lt;enumeration value="D"/>
 *     &lt;enumeration value="B"/>
 *     &lt;enumeration value="S"/>
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="E"/>
 *     &lt;enumeration value="OTC"/>
 *     &lt;enumeration value="OOOH"/>
 *     &lt;enumeration value="OH"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.DrugIssueType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocDrugIssueType {


    /**
     * Print
     * 
     */
    P,

    /**
     * Issue Without Script
     * 
     */
    Q,

    /**
     * Handwitten
     * 
     */
    H,

    /**
     * Outside
     * 
     */
    O,

    /**
     * Dispensing
     * 
     */
    D,

    /**
     * Repeat Dispensing
     * 
     */
    B,

    /**
     * Store
     * 
     */
    S,

    /**
     * Automatic
     * 
     */
    A,

    /**
     * Electronic
     * 
     */
    E,

    /**
     * Over the counter
     * 
     */
    OTC,

    /**
     * Outside Out of Hours
     * 
     */
    OOOH,

    /**
     * Outside Hospital
     * 
     */
    OH;

    public String value() {
        return name();
    }

    public static VocDrugIssueType fromValue(String v) {
        return valueOf(v);
    }

}

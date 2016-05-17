
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.PathReportDataType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.PathReportDataType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="PATH"/>
 *     &lt;enumeration value="RAD"/>
 *     &lt;enumeration value="DIS"/>
 *     &lt;enumeration value="OUT"/>
 *     &lt;enumeration value="OOH"/>
 *     &lt;enumeration value="TRI"/>
 *     &lt;enumeration value="SCR"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.PathReportDataType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocPathReportDataType {


    /**
     * Pathology
     * 
     */
    PATH,

    /**
     * Radiology
     * 
     */
    RAD,

    /**
     * Discharge
     * 
     */
    DIS,

    /**
     * Outpatient
     * 
     */
    OUT,

    /**
     * Out of hours
     * 
     */
    OOH,

    /**
     * Triage
     * 
     */
    TRI,

    /**
     * Screening
     * 
     */
    SCR;

    public String value() {
        return name();
    }

    public static VocPathReportDataType fromValue(String v) {
        return valueOf(v);
    }

}

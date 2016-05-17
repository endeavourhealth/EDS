
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.EnterpriseReportingLevel.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.EnterpriseReportingLevel">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AGGREGATE"/>
 *     &lt;enumeration value="PSEUDO_IDENTIFYING"/>
 *     &lt;enumeration value="PATIENT_LEVEL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.EnterpriseReportingLevel", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocEnterpriseReportingLevel {

    AGGREGATE,
    PSEUDO_IDENTIFYING,
    PATIENT_LEVEL;

    public String value() {
        return name();
    }

    public static VocEnterpriseReportingLevel fromValue(String v) {
        return valueOf(v);
    }

}

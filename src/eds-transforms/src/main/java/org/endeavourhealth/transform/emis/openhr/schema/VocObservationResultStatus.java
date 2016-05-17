
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ObservationResultStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ObservationResultStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="PR"/>
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="NA"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ObservationResultStatus", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocObservationResultStatus {


    /**
     * Provisional
     * 
     */
    PR,

    /**
     * Intrim
     * 
     */
    IN,

    /**
     * Not Available
     * 
     */
    NA;

    public String value() {
        return name();
    }

    public static VocObservationResultStatus fromValue(String v) {
        return valueOf(v);
    }

}

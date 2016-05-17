
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.OrderBatchStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.OrderBatchStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="AWAITING_SAMPLE"/>
 *     &lt;enumeration value="AWAITING_RESULT"/>
 *     &lt;enumeration value="REQUEST_COMPLETE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.OrderBatchStatus", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocOrderBatchStatus {


    /**
     * Awaiting Sample
     * 
     */
    AWAITING_SAMPLE,

    /**
     * Awaiting Result
     * 
     */
    AWAITING_RESULT,

    /**
     * Request Complete
     * 
     */
    REQUEST_COMPLETE;

    public String value() {
        return name();
    }

    public static VocOrderBatchStatus fromValue(String v) {
        return valueOf(v);
    }

}

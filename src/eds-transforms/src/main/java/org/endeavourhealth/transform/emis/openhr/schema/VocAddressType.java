
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.AddressType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.AddressType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="H"/>
 *     &lt;enumeration value="W"/>
 *     &lt;enumeration value="TMP"/>
 *     &lt;enumeration value="CAR"/>
 *     &lt;enumeration value="SCH"/>
 *     &lt;enumeration value="L"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.AddressType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocAddressType {


    /**
     * Home Address
     * 
     */
    H,

    /**
     * Work Address
     * 
     */
    W,

    /**
     * Temporary Address
     * 
     */
    TMP,

    /**
     * Carer Address
     * 
     */
    CAR,

    /**
     * School Address
     * 
     */
    SCH,

    /**
     * Lived-At Address
     * 
     */
    L;

    public String value() {
        return name();
    }

    public static VocAddressType fromValue(String v) {
        return valueOf(v);
    }

}

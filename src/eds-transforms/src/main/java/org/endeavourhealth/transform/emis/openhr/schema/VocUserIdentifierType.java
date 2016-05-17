
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.UserIdentifierType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.UserIdentifierType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="GP"/>
 *     &lt;enumeration value="NAT"/>
 *     &lt;enumeration value="PRES"/>
 *     &lt;enumeration value="UKCC"/>
 *     &lt;enumeration value="RSBP"/>
 *     &lt;enumeration value="GDC"/>
 *     &lt;enumeration value="REG"/>
 *     &lt;enumeration value="WP"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.UserIdentifierType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocUserIdentifierType {


    /**
     * GMC Number
     * 
     */
    GP,

    /**
     * Doctor Index Number
     * 
     */
    NAT,

    /**
     * GMP PPD Code
     * 
     */
    PRES,

    /**
     * NMC Code (formerly UKCC)
     * 
     */
    UKCC,

    /**
     * RPSGP Code
     * 
     */
    RSBP,

    /**
     * GDC Number
     * 
     */
    GDC,

    /**
     * Registration Number
     * 
     */
    REG,

    /**
     * Welsh Prescriber Number
     * 
     */
    WP;

    public String value() {
        return name();
    }

    public static VocUserIdentifierType fromValue(String v) {
        return valueOf(v);
    }

}

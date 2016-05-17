
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.AssociatedTextType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.AssociatedTextType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="RC"/>
 *     &lt;enumeration value="FC"/>
 *     &lt;enumeration value="PRE"/>
 *     &lt;enumeration value="POST"/>
 *     &lt;enumeration value="HEAD"/>
 *     &lt;enumeration value="UC"/>
 *     &lt;enumeration value="DI"/>
 *     &lt;enumeration value="CI"/>
 *     &lt;enumeration value="PT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.AssociatedTextType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocAssociatedTextType {


    /**
     * Report comment
     * 
     */
    RC,

    /**
     * Filing comment
     * 
     */
    FC,

    /**
     * Pre clinical code text
     * 
     */
    PRE,

    /**
     * Post clinical code text
     * 
     */
    POST,

    /**
     * Heading
     * 
     */
    HEAD,

    /**
     * User Comment
     * 
     */
    UC,

    /**
     * Drug Information
     * 
     */
    DI,

    /**
     * Clinical Information
     * 
     */
    CI,

    /**
     * Problem Text
     * 
     */
    PT;

    public String value() {
        return name();
    }

    public static VocAssociatedTextType fromValue(String v) {
        return valueOf(v);
    }

}

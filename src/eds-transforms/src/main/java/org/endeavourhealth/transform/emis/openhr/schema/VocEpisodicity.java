
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.Episodicity.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.Episodicity">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="NONE"/>
 *     &lt;enumeration value="FIRST"/>
 *     &lt;enumeration value="NEW"/>
 *     &lt;enumeration value="REV"/>
 *     &lt;enumeration value="FLA"/>
 *     &lt;enumeration value="END"/>
 *     &lt;enumeration value="CHG"/>
 *     &lt;enumeration value="EVO"/>
 *     &lt;enumeration value="OUT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.Episodicity", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocEpisodicity {


    /**
     * None
     * 
     */
    NONE,

    /**
     * First Occurrence
     * 
     */
    FIRST,

    /**
     * New Occurrence
     * 
     */
    NEW,

    /**
     * Problem Review
     * 
     */
    REV,

    /**
     * Problem Flare Up
     * 
     */
    FLA,

    /**
     * Problem Ended
     * 
     */
    END,

    /**
     * Changed
     * 
     */
    CHG,

    /**
     * Evolved
     * 
     */
    EVO,

    /**
     * Problem Outcome
     * 
     */
    OUT;

    public String value() {
        return name();
    }

    public static VocEpisodicity fromValue(String v) {
        return valueOf(v);
    }

}

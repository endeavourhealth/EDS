
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.PopulationParentType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.PopulationParentType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="ALL"/>
 *     &lt;enumeration value="ACTIVE"/>
 *     &lt;enumeration value="POP"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.PopulationParentType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocPopulationParentType {


    /**
     * All items inclued as parent (e.g. All patients and users)
     * 
     */
    ALL,

    /**
     * Active items included as parent (e.g. Currently registered patients and acive users)
     * 
     */
    ACTIVE,

    /**
     * Results of a specified population included as parent
     * 
     */
    POP;

    public String value() {
        return name();
    }

    public static VocPopulationParentType fromValue(String v) {
        return valueOf(v);
    }

}

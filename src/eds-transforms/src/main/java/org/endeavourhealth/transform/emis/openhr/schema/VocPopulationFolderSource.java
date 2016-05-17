
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.PopulationFolderSource.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.PopulationFolderSource">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="ORG"/>
 *     &lt;enumeration value="QOF"/>
 *     &lt;enumeration value="EMIS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.PopulationFolderSource", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocPopulationFolderSource {


    /**
     * Organisation based source
     * 
     */
    ORG,

    /**
     * QOF searches (ie nGMS Searches)
     * 
     */
    QOF,

    /**
     * Emis provided searches
     * 
     */
    EMIS;

    public String value() {
        return name();
    }

    public static VocPopulationFolderSource fromValue(String v) {
        return valueOf(v);
    }

}

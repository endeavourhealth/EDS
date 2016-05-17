
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.DocumentFolderType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.DocumentFolderType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="EPAT"/>
 *     &lt;enumeration value="EORG"/>
 *     &lt;enumeration value="EU"/>
 *     &lt;enumeration value="PAT"/>
 *     &lt;enumeration value="ORG"/>
 *     &lt;enumeration value="U"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.DocumentFolderType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocDocumentFolderType {


    /**
     * EMIS Patient
     * 
     */
    EPAT,

    /**
     * EMIS Organisation
     * 
     */
    EORG,

    /**
     * EMIS Unfiled
     * 
     */
    EU,

    /**
     * Patient
     * 
     */
    PAT,

    /**
     * Organisation
     * 
     */
    ORG,

    /**
     * Unfiled
     * 
     */
    U;

    public String value() {
        return name();
    }

    public static VocDocumentFolderType fromValue(String v) {
        return valueOf(v);
    }

}

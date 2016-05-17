
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.RegistartionTemplateType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.RegistartionTemplateType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="VIEW"/>
 *     &lt;enumeration value="EDIT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.RegistartionTemplateType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocRegistartionTemplateType {


    /**
     * View style type
     * 
     */
    VIEW,

    /**
     * Edit style type
     * 
     */
    EDIT;

    public String value() {
        return name();
    }

    public static VocRegistartionTemplateType fromValue(String v) {
        return valueOf(v);
    }

}

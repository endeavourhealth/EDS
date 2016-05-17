
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.ContractualRelationship.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.ContractualRelationship">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="E"/>
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="P"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.ContractualRelationship", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocContractualRelationship {


    /**
     * Employed
     * 
     */
    E,

    /**
     * Associated
     * 
     */
    A,

    /**
     * Employed By PCT
     * 
     */
    P;

    public String value() {
        return name();
    }

    public static VocContractualRelationship fromValue(String v) {
        return valueOf(v);
    }

}

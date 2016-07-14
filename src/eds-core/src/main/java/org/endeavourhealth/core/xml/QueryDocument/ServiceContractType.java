
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for serviceContractType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="serviceContractType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="PUBLISHER"/>
 *     &lt;enumeration value="SUBSCRIBER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "serviceContractType")
@XmlEnum
public enum ServiceContractType {

    PUBLISHER,
    SUBSCRIBER;

    public String value() {
        return name();
    }

    public static ServiceContractType fromValue(String v) {
        return valueOf(v);
    }

}

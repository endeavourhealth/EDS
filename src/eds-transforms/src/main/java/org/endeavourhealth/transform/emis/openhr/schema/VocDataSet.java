
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.DataSet.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.DataSet">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="RSC"/>
 *     &lt;enumeration value="CIC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.DataSet", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocDataSet {


    /**
     * Retinal Screening Code
     * 
     */
    RSC,

    /**
     * Childhood Immunisation Code
     * 
     */
    CIC;

    public String value() {
        return name();
    }

    public static VocDataSet fromValue(String v) {
        return valueOf(v);
    }

}

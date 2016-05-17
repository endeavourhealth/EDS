
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.AggregateValue.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.AggregateValue">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="populationCount"/>
 *     &lt;enumeration value="recordCount"/>
 *     &lt;enumeration value="percentOfBasePop"/>
 *     &lt;enumeration value="percentOfSubPop"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.AggregateValue", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocAggregateValue {

    @XmlEnumValue("populationCount")
    POPULATION_COUNT("populationCount"),
    @XmlEnumValue("recordCount")
    RECORD_COUNT("recordCount"),
    @XmlEnumValue("percentOfBasePop")
    PERCENT_OF_BASE_POP("percentOfBasePop"),
    @XmlEnumValue("percentOfSubPop")
    PERCENT_OF_SUB_POP("percentOfSubPop");
    private final String value;

    VocAggregateValue(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VocAggregateValue fromValue(String v) {
        for (VocAggregateValue c: VocAggregateValue.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

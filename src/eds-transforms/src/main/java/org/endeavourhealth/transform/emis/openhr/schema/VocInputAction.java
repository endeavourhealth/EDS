
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.InputAction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.InputAction">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="number"/>
 *     &lt;enumeration value="date"/>
 *     &lt;enumeration value="readcode"/>
 *     &lt;enumeration value="drugcode"/>
 *     &lt;enumeration value="age"/>
 *     &lt;enumeration value="time"/>
 *     &lt;enumeration value="user"/>
 *     &lt;enumeration value="sessionholder"/>
 *     &lt;enumeration value="appointment"/>
 *     &lt;enumeration value="location"/>
 *     &lt;enumeration value="values"/>
 *     &lt;enumeration value="freetext"/>
 *     &lt;enumeration value="usertype"/>
 *     &lt;enumeration value="locationtype"/>
 *     &lt;enumeration value="allergycode"/>
 *     &lt;enumeration value="speciality"/>
 *     &lt;enumeration value="organisation"/>
 *     &lt;enumeration value="ethnicorigin"/>
 *     &lt;enumeration value="sc_commissioner"/>
 *     &lt;enumeration value="sc_provider"/>
 *     &lt;enumeration value="sc_hrg"/>
 *     &lt;enumeration value="sc_consultant"/>
 *     &lt;enumeration value="sc_speciality"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.InputAction", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocInputAction {

    @XmlEnumValue("number")
    NUMBER("number"),
    @XmlEnumValue("date")
    DATE("date"),
    @XmlEnumValue("readcode")
    READCODE("readcode"),
    @XmlEnumValue("drugcode")
    DRUGCODE("drugcode"),
    @XmlEnumValue("age")
    AGE("age"),
    @XmlEnumValue("time")
    TIME("time"),
    @XmlEnumValue("user")
    USER("user"),
    @XmlEnumValue("sessionholder")
    SESSIONHOLDER("sessionholder"),
    @XmlEnumValue("appointment")
    APPOINTMENT("appointment"),
    @XmlEnumValue("location")
    LOCATION("location"),
    @XmlEnumValue("values")
    VALUES("values"),
    @XmlEnumValue("freetext")
    FREETEXT("freetext"),
    @XmlEnumValue("usertype")
    USERTYPE("usertype"),
    @XmlEnumValue("locationtype")
    LOCATIONTYPE("locationtype"),
    @XmlEnumValue("allergycode")
    ALLERGYCODE("allergycode"),
    @XmlEnumValue("speciality")
    SPECIALITY("speciality"),
    @XmlEnumValue("organisation")
    ORGANISATION("organisation"),
    @XmlEnumValue("ethnicorigin")
    ETHNICORIGIN("ethnicorigin"),
    @XmlEnumValue("sc_commissioner")
    SC_COMMISSIONER("sc_commissioner"),
    @XmlEnumValue("sc_provider")
    SC_PROVIDER("sc_provider"),
    @XmlEnumValue("sc_hrg")
    SC_HRG("sc_hrg"),
    @XmlEnumValue("sc_consultant")
    SC_CONSULTANT("sc_consultant"),
    @XmlEnumValue("sc_speciality")
    SC_SPECIALITY("sc_speciality");
    private final String value;

    VocInputAction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VocInputAction fromValue(String v) {
        for (VocInputAction c: VocInputAction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

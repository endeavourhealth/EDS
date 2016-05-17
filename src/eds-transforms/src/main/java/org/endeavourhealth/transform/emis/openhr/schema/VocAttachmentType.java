
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.AttachmentType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.AttachmentType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="WORD"/>
 *     &lt;enumeration value="JPEG"/>
 *     &lt;enumeration value="GIF"/>
 *     &lt;enumeration value="TIF"/>
 *     &lt;enumeration value="BMP"/>
 *     &lt;enumeration value="XML"/>
 *     &lt;enumeration value="RTF"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.AttachmentType", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocAttachmentType {


    /**
     * Word Document
     * 
     */
    WORD,

    /**
     * JPeg File
     * 
     */
    JPEG,

    /**
     * GIF File
     * 
     */
    GIF,

    /**
     * TIF File
     * 
     */
    TIF,

    /**
     * BMP File
     * 
     */
    BMP,

    /**
     * XML Document
     * 
     */
    XML,

    /**
     * RTF Document
     * 
     */
    RTF;

    public String value() {
        return name();
    }

    public static VocAttachmentType fromValue(String v) {
        return valueOf(v);
    }

}

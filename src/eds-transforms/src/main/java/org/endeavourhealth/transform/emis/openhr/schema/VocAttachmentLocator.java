
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voc.AttachmentLocator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voc.AttachmentLocator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="SF"/>
 *     &lt;enumeration value="URL"/>
 *     &lt;enumeration value="LOCAL"/>
 *     &lt;enumeration value="NOTAFILE"/>
 *     &lt;enumeration value="REFERENCE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voc.AttachmentLocator", namespace = "http://www.e-mis.com/emisopen")
@XmlEnum
public enum VocAttachmentLocator {


    /**
     * Server Farm
     * 
     */
    SF,

    /**
     * URL
     * 
     */
    URL,

    /**
     * Local
     * 
     */
    LOCAL,

    /**
     * Not a physical file
     * 
     */
    NOTAFILE,

    /**
     * A placeholder for a document which is not available
     * 
     */
    REFERENCE;

    public String value() {
        return name();
    }

    public static VocAttachmentLocator fromValue(String v) {
        return valueOf(v);
    }

}

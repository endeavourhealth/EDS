
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EDICommentListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EDICommentListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="EDIComment" type="{http://www.e-mis.com/emisopen/MedicalRecord}EDIComment" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EDICommentListType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "ediComment"
})
public class EDICommentListType {

    @XmlElement(name = "EDIComment", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<EDIComment> ediComment;

    /**
     * Gets the value of the ediComment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ediComment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEDIComment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EDIComment }
     * 
     * 
     */
    public List<EDIComment> getEDIComment() {
        if (ediComment == null) {
            ediComment = new ArrayList<EDIComment>();
        }
        return this.ediComment;
    }

}

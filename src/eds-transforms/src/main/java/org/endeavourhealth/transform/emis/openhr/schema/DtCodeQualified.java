
package org.endeavourhealth.transform.emis.openhr.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dt.CodeQualified complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.CodeQualified">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.e-mis.com/emisopen}dt.Code">
 *       &lt;sequence>
 *         &lt;element name="qualifier" type="{http://www.e-mis.com/emisopen}dt.Qualifier" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="translation" type="{http://www.e-mis.com/emisopen}dt.CodeQualified" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dt.CodeQualified", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "qualifier",
    "translation"
})
public class DtCodeQualified
    extends DtCode
{

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<DtQualifier> qualifier;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected List<DtCodeQualified> translation;

    /**
     * Gets the value of the qualifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the qualifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQualifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DtQualifier }
     * 
     * 
     */
    public List<DtQualifier> getQualifier() {
        if (qualifier == null) {
            qualifier = new ArrayList<DtQualifier>();
        }
        return this.qualifier;
    }

    /**
     * Gets the value of the translation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the translation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTranslation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DtCodeQualified }
     * 
     * 
     */
    public List<DtCodeQualified> getTranslation() {
        if (translation == null) {
            translation = new ArrayList<DtCodeQualified>();
        }
        return this.translation;
    }

}

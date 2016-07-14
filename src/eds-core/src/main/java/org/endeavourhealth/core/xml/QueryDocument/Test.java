
package org.endeavourhealth.core.xml.QueryDocument;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for test complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="test">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="resource" type="{}resource"/>
 *           &lt;element name="resourceUuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="isAny">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="fieldTest" type="{}fieldTest" maxOccurs="unbounded"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "test", propOrder = {
    "resource",
    "resourceUuid",
    "isAny",
    "fieldTest"
})
public class Test {

    protected Resource resource;
    protected String resourceUuid;
    protected Test.IsAny isAny;
    protected List<FieldTest> fieldTest;

    /**
     * Gets the value of the resource property.
     * 
     * @return
     *     possible object is
     *     {@link Resource }
     *     
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Sets the value of the resource property.
     * 
     * @param value
     *     allowed object is
     *     {@link Resource }
     *     
     */
    public void setResource(Resource value) {
        this.resource = value;
    }

    /**
     * Gets the value of the resourceUuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResourceUuid() {
        return resourceUuid;
    }

    /**
     * Sets the value of the resourceUuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResourceUuid(String value) {
        this.resourceUuid = value;
    }

    /**
     * Gets the value of the isAny property.
     * 
     * @return
     *     possible object is
     *     {@link Test.IsAny }
     *     
     */
    public Test.IsAny getIsAny() {
        return isAny;
    }

    /**
     * Sets the value of the isAny property.
     * 
     * @param value
     *     allowed object is
     *     {@link Test.IsAny }
     *     
     */
    public void setIsAny(Test.IsAny value) {
        this.isAny = value;
    }

    /**
     * Gets the value of the fieldTest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fieldTest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFieldTest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FieldTest }
     * 
     * 
     */
    public List<FieldTest> getFieldTest() {
        if (fieldTest == null) {
            fieldTest = new ArrayList<FieldTest>();
        }
        return this.fieldTest;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class IsAny {


    }

}

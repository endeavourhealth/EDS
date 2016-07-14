
package org.endeavourhealth.core.xml.QueryDocument;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fieldTest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fieldTest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="field" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;element name="valueFrom" type="{}valueFrom"/>
 *           &lt;element name="valueTo" type="{}valueTo"/>
 *           &lt;element name="valueRange" type="{}valueRange"/>
 *           &lt;element name="valueEqualTo" type="{}value"/>
 *           &lt;element name="codeSet" type="{}codeSet"/>
 *           &lt;element name="codeSetLibraryItemUuid" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *           &lt;element name="valueSet" type="{}valueSet"/>
 *         &lt;/choice>
 *         &lt;element name="negate" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fieldTest", propOrder = {
    "field",
    "valueFrom",
    "valueTo",
    "valueRange",
    "valueEqualTo",
    "codeSet",
    "codeSetLibraryItemUuid",
    "valueSet",
    "negate"
})
public class FieldTest {

    @XmlElement(required = true)
    protected String field;
    protected ValueFrom valueFrom;
    protected ValueTo valueTo;
    protected ValueRange valueRange;
    protected Value valueEqualTo;
    protected CodeSet codeSet;
    protected List<String> codeSetLibraryItemUuid;
    protected ValueSet valueSet;
    protected boolean negate;

    /**
     * Gets the value of the field property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getField() {
        return field;
    }

    /**
     * Sets the value of the field property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setField(String value) {
        this.field = value;
    }

    /**
     * Gets the value of the valueFrom property.
     * 
     * @return
     *     possible object is
     *     {@link ValueFrom }
     *     
     */
    public ValueFrom getValueFrom() {
        return valueFrom;
    }

    /**
     * Sets the value of the valueFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueFrom }
     *     
     */
    public void setValueFrom(ValueFrom value) {
        this.valueFrom = value;
    }

    /**
     * Gets the value of the valueTo property.
     * 
     * @return
     *     possible object is
     *     {@link ValueTo }
     *     
     */
    public ValueTo getValueTo() {
        return valueTo;
    }

    /**
     * Sets the value of the valueTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueTo }
     *     
     */
    public void setValueTo(ValueTo value) {
        this.valueTo = value;
    }

    /**
     * Gets the value of the valueRange property.
     * 
     * @return
     *     possible object is
     *     {@link ValueRange }
     *     
     */
    public ValueRange getValueRange() {
        return valueRange;
    }

    /**
     * Sets the value of the valueRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueRange }
     *     
     */
    public void setValueRange(ValueRange value) {
        this.valueRange = value;
    }

    /**
     * Gets the value of the valueEqualTo property.
     * 
     * @return
     *     possible object is
     *     {@link Value }
     *     
     */
    public Value getValueEqualTo() {
        return valueEqualTo;
    }

    /**
     * Sets the value of the valueEqualTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Value }
     *     
     */
    public void setValueEqualTo(Value value) {
        this.valueEqualTo = value;
    }

    /**
     * Gets the value of the codeSet property.
     * 
     * @return
     *     possible object is
     *     {@link CodeSet }
     *     
     */
    public CodeSet getCodeSet() {
        return codeSet;
    }

    /**
     * Sets the value of the codeSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeSet }
     *     
     */
    public void setCodeSet(CodeSet value) {
        this.codeSet = value;
    }

    /**
     * Gets the value of the codeSetLibraryItemUuid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codeSetLibraryItemUuid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodeSetLibraryItemUuid().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCodeSetLibraryItemUuid() {
        if (codeSetLibraryItemUuid == null) {
            codeSetLibraryItemUuid = new ArrayList<String>();
        }
        return this.codeSetLibraryItemUuid;
    }

    /**
     * Gets the value of the valueSet property.
     * 
     * @return
     *     possible object is
     *     {@link ValueSet }
     *     
     */
    public ValueSet getValueSet() {
        return valueSet;
    }

    /**
     * Sets the value of the valueSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueSet }
     *     
     */
    public void setValueSet(ValueSet value) {
        this.valueSet = value;
    }

    /**
     * Gets the value of the negate property.
     * 
     */
    public boolean isNegate() {
        return negate;
    }

    /**
     * Sets the value of the negate property.
     * 
     */
    public void setNegate(boolean value) {
        this.negate = value;
    }

}

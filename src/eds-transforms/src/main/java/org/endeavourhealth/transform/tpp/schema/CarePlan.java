
package org.endeavourhealth.transform.tpp.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CarePlan complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CarePlan">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CarePlanName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Category" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SubCategory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Aim" type="{}CarePlanAim"/>
 *         &lt;element name="ReviewDate" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="CarePlanInstruction" type="{}CarePlanInstruction" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="LinkedProblemUID" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CarePlan", propOrder = {
    "carePlanName",
    "description",
    "category",
    "subCategory",
    "aim",
    "reviewDate",
    "carePlanInstruction",
    "linkedProblemUID"
})
public class CarePlan {

    @XmlElement(name = "CarePlanName", required = true)
    protected String carePlanName;
    @XmlElement(name = "Description", required = true)
    protected String description;
    @XmlElement(name = "Category", required = true)
    protected String category;
    @XmlElement(name = "SubCategory")
    protected String subCategory;
    @XmlElement(name = "Aim", required = true)
    protected String aim;
    @XmlElement(name = "ReviewDate", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar reviewDate;
    @XmlElement(name = "CarePlanInstruction")
    protected List<CarePlanInstruction> carePlanInstruction;
    @XmlElement(name = "LinkedProblemUID")
    protected List<String> linkedProblemUID;

    /**
     * Gets the value of the carePlanName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCarePlanName() {
        return carePlanName;
    }

    /**
     * Sets the value of the carePlanName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCarePlanName(String value) {
        this.carePlanName = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the category property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCategory(String value) {
        this.category = value;
    }

    /**
     * Gets the value of the subCategory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubCategory() {
        return subCategory;
    }

    /**
     * Sets the value of the subCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubCategory(String value) {
        this.subCategory = value;
    }

    /**
     * Gets the value of the aim property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAim() {
        return aim;
    }

    /**
     * Sets the value of the aim property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAim(String value) {
        this.aim = value;
    }

    /**
     * Gets the value of the reviewDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getReviewDate() {
        return reviewDate;
    }

    /**
     * Sets the value of the reviewDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setReviewDate(XMLGregorianCalendar value) {
        this.reviewDate = value;
    }

    /**
     * Gets the value of the carePlanInstruction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the carePlanInstruction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCarePlanInstruction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CarePlanInstruction }
     * 
     * 
     */
    public List<CarePlanInstruction> getCarePlanInstruction() {
        if (carePlanInstruction == null) {
            carePlanInstruction = new ArrayList<CarePlanInstruction>();
        }
        return this.carePlanInstruction;
    }

    /**
     * Gets the value of the linkedProblemUID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkedProblemUID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkedProblemUID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLinkedProblemUID() {
        if (linkedProblemUID == null) {
            linkedProblemUID = new ArrayList<String>();
        }
        return this.linkedProblemUID;
    }

}

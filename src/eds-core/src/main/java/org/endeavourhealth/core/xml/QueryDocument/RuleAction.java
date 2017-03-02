
package org.endeavourhealth.core.xml.QueryDocument;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ruleAction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ruleAction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="action" type="{}ruleActionOperator"/>
 *         &lt;element name="ruleId" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ruleAction", propOrder = {
    "action",
    "ruleId"
})
public class RuleAction {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected RuleActionOperator action;
    @XmlElement(type = Integer.class)
    protected List<Integer> ruleId;

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link RuleActionOperator }
     *     
     */
    public RuleActionOperator getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link RuleActionOperator }
     *     
     */
    public void setAction(RuleActionOperator value) {
        this.action = value;
    }

    /**
     * Gets the value of the ruleId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ruleId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRuleId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getRuleId() {
        if (ruleId == null) {
            ruleId = new ArrayList<Integer>();
        }
        return this.ruleId;
    }

}

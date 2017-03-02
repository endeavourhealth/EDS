
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rule">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;choice>
 *           &lt;element name="test" type="{}test"/>
 *           &lt;element name="testLibraryItemUUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="queryLibraryItemUUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="expression" type="{}expressionType"/>
 *         &lt;/choice>
 *         &lt;element name="onPass" type="{}ruleAction"/>
 *         &lt;element name="onFail" type="{}ruleAction"/>
 *         &lt;element name="layout" type="{}layoutType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rule", propOrder = {
    "description",
    "id",
    "test",
    "testLibraryItemUUID",
    "queryLibraryItemUUID",
    "expression",
    "onPass",
    "onFail",
    "layout"
})
public class Rule {

    @XmlElement(required = true)
    protected String description;
    protected int id;
    protected Test test;
    protected String testLibraryItemUUID;
    protected String queryLibraryItemUUID;
    protected ExpressionType expression;
    @XmlElement(required = true)
    protected RuleAction onPass;
    @XmlElement(required = true)
    protected RuleAction onFail;
    @XmlElement(required = true)
    protected LayoutType layout;

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
     * Gets the value of the id property.
     * 
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Gets the value of the test property.
     * 
     * @return
     *     possible object is
     *     {@link Test }
     *     
     */
    public Test getTest() {
        return test;
    }

    /**
     * Sets the value of the test property.
     * 
     * @param value
     *     allowed object is
     *     {@link Test }
     *     
     */
    public void setTest(Test value) {
        this.test = value;
    }

    /**
     * Gets the value of the testLibraryItemUUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTestLibraryItemUUID() {
        return testLibraryItemUUID;
    }

    /**
     * Sets the value of the testLibraryItemUUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTestLibraryItemUUID(String value) {
        this.testLibraryItemUUID = value;
    }

    /**
     * Gets the value of the queryLibraryItemUUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryLibraryItemUUID() {
        return queryLibraryItemUUID;
    }

    /**
     * Sets the value of the queryLibraryItemUUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryLibraryItemUUID(String value) {
        this.queryLibraryItemUUID = value;
    }

    /**
     * Gets the value of the expression property.
     * 
     * @return
     *     possible object is
     *     {@link ExpressionType }
     *     
     */
    public ExpressionType getExpression() {
        return expression;
    }

    /**
     * Sets the value of the expression property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExpressionType }
     *     
     */
    public void setExpression(ExpressionType value) {
        this.expression = value;
    }

    /**
     * Gets the value of the onPass property.
     * 
     * @return
     *     possible object is
     *     {@link RuleAction }
     *     
     */
    public RuleAction getOnPass() {
        return onPass;
    }

    /**
     * Sets the value of the onPass property.
     * 
     * @param value
     *     allowed object is
     *     {@link RuleAction }
     *     
     */
    public void setOnPass(RuleAction value) {
        this.onPass = value;
    }

    /**
     * Gets the value of the onFail property.
     * 
     * @return
     *     possible object is
     *     {@link RuleAction }
     *     
     */
    public RuleAction getOnFail() {
        return onFail;
    }

    /**
     * Sets the value of the onFail property.
     * 
     * @param value
     *     allowed object is
     *     {@link RuleAction }
     *     
     */
    public void setOnFail(RuleAction value) {
        this.onFail = value;
    }

    /**
     * Gets the value of the layout property.
     * 
     * @return
     *     possible object is
     *     {@link LayoutType }
     *     
     */
    public LayoutType getLayout() {
        return layout;
    }

    /**
     * Sets the value of the layout property.
     * 
     * @param value
     *     allowed object is
     *     {@link LayoutType }
     *     
     */
    public void setLayout(LayoutType value) {
        this.layout = value;
    }

}

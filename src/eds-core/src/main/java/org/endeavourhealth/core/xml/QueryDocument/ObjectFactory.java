
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.core.xml.QueryDocument package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _QueryDocument_QNAME = new QName("", "queryDocument");
    private final static QName _LibraryItem_QNAME = new QName("", "libraryItem");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.core.xml.QueryDocument
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Query }
     * 
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link Test }
     * 
     */
    public Test createTest() {
        return new Test();
    }

    /**
     * Create an instance of {@link QueryDocument }
     * 
     */
    public QueryDocument createQueryDocument() {
        return new QueryDocument();
    }

    /**
     * Create an instance of {@link LibraryItem }
     * 
     */
    public LibraryItem createLibraryItem() {
        return new LibraryItem();
    }

    /**
     * Create an instance of {@link CalculationType }
     * 
     */
    public CalculationType createCalculationType() {
        return new CalculationType();
    }

    /**
     * Create an instance of {@link ValueTo }
     * 
     */
    public ValueTo createValueTo() {
        return new ValueTo();
    }

    /**
     * Create an instance of {@link CodeSetValue }
     * 
     */
    public CodeSetValue createCodeSetValue() {
        return new CodeSetValue();
    }

    /**
     * Create an instance of {@link ValueSet }
     * 
     */
    public ValueSet createValueSet() {
        return new ValueSet();
    }

    /**
     * Create an instance of {@link FieldTest }
     * 
     */
    public FieldTest createFieldTest() {
        return new FieldTest();
    }

    /**
     * Create an instance of {@link ValueRange }
     * 
     */
    public ValueRange createValueRange() {
        return new ValueRange();
    }

    /**
     * Create an instance of {@link Rule }
     * 
     */
    public Rule createRule() {
        return new Rule();
    }

    /**
     * Create an instance of {@link Section }
     * 
     */
    public Section createSection() {
        return new Section();
    }

    /**
     * Create an instance of {@link VariableType }
     * 
     */
    public VariableType createVariableType() {
        return new VariableType();
    }

    /**
     * Create an instance of {@link Protocol }
     * 
     */
    public Protocol createProtocol() {
        return new Protocol();
    }

    /**
     * Create an instance of {@link Composition }
     * 
     */
    public Composition createComposition() {
        return new Composition();
    }

    /**
     * Create an instance of {@link CodeSet }
     * 
     */
    public CodeSet createCodeSet() {
        return new CodeSet();
    }

    /**
     * Create an instance of {@link LayoutType }
     * 
     */
    public LayoutType createLayoutType() {
        return new LayoutType();
    }

    /**
     * Create an instance of {@link ExpressionType }
     * 
     */
    public ExpressionType createExpressionType() {
        return new ExpressionType();
    }

    /**
     * Create an instance of {@link Value }
     * 
     */
    public Value createValue() {
        return new Value();
    }

    /**
     * Create an instance of {@link DataSet }
     * 
     */
    public DataSet createDataSet() {
        return new DataSet();
    }

    /**
     * Create an instance of {@link ParameterType }
     * 
     */
    public ParameterType createParameterType() {
        return new ParameterType();
    }

    /**
     * Create an instance of {@link Comparison }
     * 
     */
    public Comparison createComparison() {
        return new Comparison();
    }

    /**
     * Create an instance of {@link Resource }
     * 
     */
    public Resource createResource() {
        return new Resource();
    }

    /**
     * Create an instance of {@link ServiceContract }
     * 
     */
    public ServiceContract createServiceContract() {
        return new ServiceContract();
    }

    /**
     * Create an instance of {@link CalculationParameter }
     * 
     */
    public CalculationParameter createCalculationParameter() {
        return new CalculationParameter();
    }

    /**
     * Create an instance of {@link ValueFrom }
     * 
     */
    public ValueFrom createValueFrom() {
        return new ValueFrom();
    }

    /**
     * Create an instance of {@link TechnicalInterface }
     * 
     */
    public TechnicalInterface createTechnicalInterface() {
        return new TechnicalInterface();
    }

    /**
     * Create an instance of {@link RuleAction }
     * 
     */
    public RuleAction createRuleAction() {
        return new RuleAction();
    }

    /**
     * Create an instance of {@link Folder }
     * 
     */
    public Folder createFolder() {
        return new Folder();
    }

    /**
     * Create an instance of {@link System }
     * 
     */
    public System createSystem() {
        return new System();
    }

    /**
     * Create an instance of {@link Service }
     * 
     */
    public Service createService() {
        return new Service();
    }

    /**
     * Create an instance of {@link CountReport }
     * 
     */
    public CountReport createCountReport() {
        return new CountReport();
    }

    /**
     * Create an instance of {@link Restriction }
     * 
     */
    public Restriction createRestriction() {
        return new Restriction();
    }

    /**
     * Create an instance of {@link Query.StartingRules }
     * 
     */
    public Query.StartingRules createQueryStartingRules() {
        return new Query.StartingRules();
    }

    /**
     * Create an instance of {@link Test.IsAny }
     * 
     */
    public Test.IsAny createTestIsAny() {
        return new Test.IsAny();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryDocument }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "queryDocument")
    public JAXBElement<QueryDocument> createQueryDocument(QueryDocument value) {
        return new JAXBElement<QueryDocument>(_QueryDocument_QNAME, QueryDocument.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LibraryItem }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "libraryItem")
    public JAXBElement<LibraryItem> createLibraryItem(LibraryItem value) {
        return new JAXBElement<LibraryItem>(_LibraryItem_QNAME, LibraryItem.class, null, value);
    }

}

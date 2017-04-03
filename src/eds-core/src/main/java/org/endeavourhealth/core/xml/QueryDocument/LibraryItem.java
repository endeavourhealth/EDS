
package org.endeavourhealth.core.xml.QueryDocument;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for libraryItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="libraryItem">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="uuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="folderUuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;element name="query" type="{}query"/>
 *           &lt;element name="resource" type="{}resource"/>
 *           &lt;element name="test" type="{}test"/>
 *           &lt;element name="codeSet" type="{}codeSet"/>
 *           &lt;element name="dataSet" type="{}dataSet"/>
 *           &lt;element name="protocol" type="{}protocol"/>
 *           &lt;element name="system" type="{}system"/>
 *           &lt;element name="countReport" type="{}countReport"/>
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
@XmlType(name = "libraryItem", propOrder = {
    "uuid",
    "name",
    "description",
    "folderUuid",
    "query",
    "resource",
    "test",
    "codeSet",
    "dataSet",
    "protocol",
    "system",
    "countReport"
})
public class LibraryItem {

    @XmlElement(required = true)
    protected String uuid;
    @XmlElement(required = true)
    protected String name;
    protected String description;
    @XmlElement(required = true)
    protected String folderUuid;
    protected Query query;
    protected Resource resource;
    protected Test test;
    protected CodeSet codeSet;
    protected DataSet dataSet;
    protected Protocol protocol;
    protected System system;
    protected CountReport countReport;

    /**
     * Gets the value of the uuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
     * Gets the value of the folderUuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFolderUuid() {
        return folderUuid;
    }

    /**
     * Sets the value of the folderUuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFolderUuid(String value) {
        this.folderUuid = value;
    }

    /**
     * Gets the value of the query property.
     * 
     * @return
     *     possible object is
     *     {@link Query }
     *     
     */
    public Query getQuery() {
        return query;
    }

    /**
     * Sets the value of the query property.
     * 
     * @param value
     *     allowed object is
     *     {@link Query }
     *     
     */
    public void setQuery(Query value) {
        this.query = value;
    }

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
     * Gets the value of the dataSet property.
     * 
     * @return
     *     possible object is
     *     {@link DataSet }
     *     
     */
    public DataSet getDataSet() {
        return dataSet;
    }

    /**
     * Sets the value of the dataSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSet }
     *     
     */
    public void setDataSet(DataSet value) {
        this.dataSet = value;
    }

    /**
     * Gets the value of the protocol property.
     * 
     * @return
     *     possible object is
     *     {@link Protocol }
     *     
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * Sets the value of the protocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Protocol }
     *     
     */
    public void setProtocol(Protocol value) {
        this.protocol = value;
    }

    /**
     * Gets the value of the system property.
     * 
     * @return
     *     possible object is
     *     {@link System }
     *     
     */
    public System getSystem() {
        return system;
    }

    /**
     * Sets the value of the system property.
     * 
     * @param value
     *     allowed object is
     *     {@link System }
     *     
     */
    public void setSystem(System value) {
        this.system = value;
    }

    /**
     * Gets the value of the countReport property.
     * 
     * @return
     *     possible object is
     *     {@link CountReport }
     *     
     */
    public CountReport getCountReport() {
        return countReport;
    }

    /**
     * Sets the value of the countReport property.
     * 
     * @param value
     *     allowed object is
     *     {@link CountReport }
     *     
     */
    public void setCountReport(CountReport value) {
        this.countReport = value;
    }

}

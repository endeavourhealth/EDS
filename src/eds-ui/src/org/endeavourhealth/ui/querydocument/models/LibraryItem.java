
package org.endeavourhealth.ui.querydocument.models;

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
 *           &lt;element name="dataSource" type="{}dataSource"/>
 *           &lt;element name="test" type="{}test"/>
 *           &lt;element name="codeSet" type="{}codeSet"/>
 *           &lt;element name="listReport" type="{}listReport"/>
 *           &lt;element name="protocol" type="{}protocol"/>
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
    "dataSource",
    "test",
    "codeSet",
    "listReport",
    "protocol"
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
    protected DataSource dataSource;
    protected Test test;
    protected CodeSet codeSet;
    protected ListReport listReport;
    protected Protocol protocol;

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
     * Gets the value of the dataSource property.
     * 
     * @return
     *     possible object is
     *     {@link DataSource }
     *     
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets the value of the dataSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSource }
     *     
     */
    public void setDataSource(DataSource value) {
        this.dataSource = value;
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
     * Gets the value of the listReport property.
     * 
     * @return
     *     possible object is
     *     {@link ListReport }
     *     
     */
    public ListReport getListReport() {
        return listReport;
    }

    /**
     * Sets the value of the listReport property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListReport }
     *     
     */
    public void setListReport(ListReport value) {
        this.listReport = value;
    }

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

}

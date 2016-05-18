
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Base ID/Ref Type . DBID is the internal EMIS db, RefID references something else within the message that also has the same ID , GUID is used for GUID projects 
 * 
 * <p>Java class for IdentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IdentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DBID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="RefID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="GUID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FileStatus" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="OldGUID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "dbid",
    "refID",
    "guid",
    "fileStatus",
    "oldGUID"
})
@XmlSeeAlso({
    TypeOfLocationType.class,
    RoleType.class,
    PersonType.class,
    org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.ElementListType.ConsultationElement.class,
    EDIOrderType.FormDestination.class,
    EDIOrderType.class,
    MixtureItemType.class,
    PathologySpecimenType.class,
    PersonCategoryType.class,
    PathologyReportType.class,
    StructuredIdentType.class,
    ApplicationType.class,
    TeamType.class,
    AppointmentType.class,
    PathologyTestType.class,
    InvestigationTypeBase.class,
    RegistrationType.class,
    MedicationType.class,
    ItemBaseType.class,
    TestRequestHeaderType.class,
    ConsultationType.class,
    NoteType.class,
    StatusType.class,
    AddressType.class,
    LocationType.GPLinkCodeList.GPLinkCode.class,
    LocationType.class
})
public class IdentType {

    @XmlElement(name = "DBID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger dbid;
    @XmlElement(name = "RefID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger refID;
    @XmlElement(name = "GUID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String guid;
    @XmlElement(name = "FileStatus", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected BigInteger fileStatus;
    @XmlElement(name = "OldGUID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected String oldGUID;

    /**
     * Gets the value of the dbid property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDBID() {
        return dbid;
    }

    /**
     * Sets the value of the dbid property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDBID(BigInteger value) {
        this.dbid = value;
    }

    /**
     * Gets the value of the refID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRefID() {
        return refID;
    }

    /**
     * Sets the value of the refID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRefID(BigInteger value) {
        this.refID = value;
    }

    /**
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGUID() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGUID(String value) {
        this.guid = value;
    }

    /**
     * Gets the value of the fileStatus property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFileStatus() {
        return fileStatus;
    }

    /**
     * Sets the value of the fileStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFileStatus(BigInteger value) {
        this.fileStatus = value;
    }

    /**
     * Gets the value of the oldGUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldGUID() {
        return oldGUID;
    }

    /**
     * Sets the value of the oldGUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldGUID(String value) {
        this.oldGUID = value;
    }

}

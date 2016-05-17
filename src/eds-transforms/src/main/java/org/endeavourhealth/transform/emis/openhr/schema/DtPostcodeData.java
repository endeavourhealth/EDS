
package org.endeavourhealth.transform.emis.openhr.schema;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dt.PostcodeData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.PostcodeData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CountryCode">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="3"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="PCTCode">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="3"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="TownsendScore">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="IMD">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="HouseInPoorCondition">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="CombinedAirQualityIndex">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="NitrogenDioxide">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="PM10">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="SulphurDioxide">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Benzene">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="RuralityEnglandWales">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="RuralityScotland">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Version">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dt.PostcodeData", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "countryCode",
    "pctCode",
    "townsendScore",
    "imd",
    "houseInPoorCondition",
    "combinedAirQualityIndex",
    "nitrogenDioxide",
    "pm10",
    "sulphurDioxide",
    "benzene",
    "ruralityEnglandWales",
    "ruralityScotland",
    "version"
})
public class DtPostcodeData {

    @XmlElement(name = "CountryCode", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String countryCode;
    @XmlElement(name = "PCTCode", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String pctCode;
    @XmlElement(name = "TownsendScore", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected BigDecimal townsendScore;
    @XmlElement(name = "IMD", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected BigDecimal imd;
    @XmlElement(name = "HouseInPoorCondition", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected BigDecimal houseInPoorCondition;
    @XmlElement(name = "CombinedAirQualityIndex", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected BigDecimal combinedAirQualityIndex;
    @XmlElement(name = "NitrogenDioxide", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected BigDecimal nitrogenDioxide;
    @XmlElement(name = "PM10", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected BigDecimal pm10;
    @XmlElement(name = "SulphurDioxide", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected BigDecimal sulphurDioxide;
    @XmlElement(name = "Benzene", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected BigDecimal benzene;
    @XmlElement(name = "RuralityEnglandWales", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String ruralityEnglandWales;
    @XmlElement(name = "RuralityScotland", namespace = "http://www.e-mis.com/emisopen", required = true)
    protected String ruralityScotland;
    @XmlElement(name = "Version", namespace = "http://www.e-mis.com/emisopen")
    protected int version;

    /**
     * Gets the value of the countryCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the value of the countryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryCode(String value) {
        this.countryCode = value;
    }

    /**
     * Gets the value of the pctCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPCTCode() {
        return pctCode;
    }

    /**
     * Sets the value of the pctCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPCTCode(String value) {
        this.pctCode = value;
    }

    /**
     * Gets the value of the townsendScore property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTownsendScore() {
        return townsendScore;
    }

    /**
     * Sets the value of the townsendScore property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTownsendScore(BigDecimal value) {
        this.townsendScore = value;
    }

    /**
     * Gets the value of the imd property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getIMD() {
        return imd;
    }

    /**
     * Sets the value of the imd property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setIMD(BigDecimal value) {
        this.imd = value;
    }

    /**
     * Gets the value of the houseInPoorCondition property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getHouseInPoorCondition() {
        return houseInPoorCondition;
    }

    /**
     * Sets the value of the houseInPoorCondition property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setHouseInPoorCondition(BigDecimal value) {
        this.houseInPoorCondition = value;
    }

    /**
     * Gets the value of the combinedAirQualityIndex property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCombinedAirQualityIndex() {
        return combinedAirQualityIndex;
    }

    /**
     * Sets the value of the combinedAirQualityIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCombinedAirQualityIndex(BigDecimal value) {
        this.combinedAirQualityIndex = value;
    }

    /**
     * Gets the value of the nitrogenDioxide property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getNitrogenDioxide() {
        return nitrogenDioxide;
    }

    /**
     * Sets the value of the nitrogenDioxide property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setNitrogenDioxide(BigDecimal value) {
        this.nitrogenDioxide = value;
    }

    /**
     * Gets the value of the pm10 property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getPM10() {
        return pm10;
    }

    /**
     * Sets the value of the pm10 property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setPM10(BigDecimal value) {
        this.pm10 = value;
    }

    /**
     * Gets the value of the sulphurDioxide property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSulphurDioxide() {
        return sulphurDioxide;
    }

    /**
     * Sets the value of the sulphurDioxide property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSulphurDioxide(BigDecimal value) {
        this.sulphurDioxide = value;
    }

    /**
     * Gets the value of the benzene property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getBenzene() {
        return benzene;
    }

    /**
     * Sets the value of the benzene property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setBenzene(BigDecimal value) {
        this.benzene = value;
    }

    /**
     * Gets the value of the ruralityEnglandWales property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRuralityEnglandWales() {
        return ruralityEnglandWales;
    }

    /**
     * Sets the value of the ruralityEnglandWales property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRuralityEnglandWales(String value) {
        this.ruralityEnglandWales = value;
    }

    /**
     * Gets the value of the ruralityScotland property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRuralityScotland() {
        return ruralityScotland;
    }

    /**
     * Sets the value of the ruralityScotland property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRuralityScotland(String value) {
        this.ruralityScotland = value;
    }

    /**
     * Gets the value of the version property.
     * 
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     */
    public void setVersion(int value) {
        this.version = value;
    }

}

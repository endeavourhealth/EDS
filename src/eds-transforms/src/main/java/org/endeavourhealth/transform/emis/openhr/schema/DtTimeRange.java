
package org.endeavourhealth.transform.emis.openhr.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dt.TimeRange complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dt.TimeRange">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="baseLow">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="low" type="{http://www.e-mis.com/emisopen}dt.DatePart"/>
 *                   &lt;choice minOccurs="0">
 *                     &lt;element name="width" type="{http://www.e-mis.com/emisopen}dt.Duration" minOccurs="0"/>
 *                     &lt;element name="high" type="{http://www.e-mis.com/emisopen}dt.DatePart" minOccurs="0"/>
 *                   &lt;/choice>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="baseHigh">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="high" type="{http://www.e-mis.com/emisopen}dt.DatePart"/>
 *                   &lt;choice minOccurs="0">
 *                     &lt;element name="width" type="{http://www.e-mis.com/emisopen}dt.Duration" minOccurs="0"/>
 *                     &lt;element name="low" type="{http://www.e-mis.com/emisopen}dt.DatePart" minOccurs="0"/>
 *                   &lt;/choice>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dt.TimeRange", namespace = "http://www.e-mis.com/emisopen", propOrder = {
    "baseLow",
    "baseHigh"
})
public class DtTimeRange {

    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtTimeRange.BaseLow baseLow;
    @XmlElement(namespace = "http://www.e-mis.com/emisopen")
    protected DtTimeRange.BaseHigh baseHigh;

    /**
     * Gets the value of the baseLow property.
     * 
     * @return
     *     possible object is
     *     {@link DtTimeRange.BaseLow }
     *     
     */
    public DtTimeRange.BaseLow getBaseLow() {
        return baseLow;
    }

    /**
     * Sets the value of the baseLow property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtTimeRange.BaseLow }
     *     
     */
    public void setBaseLow(DtTimeRange.BaseLow value) {
        this.baseLow = value;
    }

    /**
     * Gets the value of the baseHigh property.
     * 
     * @return
     *     possible object is
     *     {@link DtTimeRange.BaseHigh }
     *     
     */
    public DtTimeRange.BaseHigh getBaseHigh() {
        return baseHigh;
    }

    /**
     * Sets the value of the baseHigh property.
     * 
     * @param value
     *     allowed object is
     *     {@link DtTimeRange.BaseHigh }
     *     
     */
    public void setBaseHigh(DtTimeRange.BaseHigh value) {
        this.baseHigh = value;
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
     *       &lt;sequence>
     *         &lt;element name="high" type="{http://www.e-mis.com/emisopen}dt.DatePart"/>
     *         &lt;choice minOccurs="0">
     *           &lt;element name="width" type="{http://www.e-mis.com/emisopen}dt.Duration" minOccurs="0"/>
     *           &lt;element name="low" type="{http://www.e-mis.com/emisopen}dt.DatePart" minOccurs="0"/>
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
    @XmlType(name = "", propOrder = {
        "high",
        "width",
        "low"
    })
    public static class BaseHigh {

        @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
        protected DtDatePart high;
        @XmlElement(namespace = "http://www.e-mis.com/emisopen")
        protected DtDuration width;
        @XmlElement(namespace = "http://www.e-mis.com/emisopen")
        protected DtDatePart low;

        /**
         * Gets the value of the high property.
         * 
         * @return
         *     possible object is
         *     {@link DtDatePart }
         *     
         */
        public DtDatePart getHigh() {
            return high;
        }

        /**
         * Sets the value of the high property.
         * 
         * @param value
         *     allowed object is
         *     {@link DtDatePart }
         *     
         */
        public void setHigh(DtDatePart value) {
            this.high = value;
        }

        /**
         * Gets the value of the width property.
         * 
         * @return
         *     possible object is
         *     {@link DtDuration }
         *     
         */
        public DtDuration getWidth() {
            return width;
        }

        /**
         * Sets the value of the width property.
         * 
         * @param value
         *     allowed object is
         *     {@link DtDuration }
         *     
         */
        public void setWidth(DtDuration value) {
            this.width = value;
        }

        /**
         * Gets the value of the low property.
         * 
         * @return
         *     possible object is
         *     {@link DtDatePart }
         *     
         */
        public DtDatePart getLow() {
            return low;
        }

        /**
         * Sets the value of the low property.
         * 
         * @param value
         *     allowed object is
         *     {@link DtDatePart }
         *     
         */
        public void setLow(DtDatePart value) {
            this.low = value;
        }

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
     *       &lt;sequence>
     *         &lt;element name="low" type="{http://www.e-mis.com/emisopen}dt.DatePart"/>
     *         &lt;choice minOccurs="0">
     *           &lt;element name="width" type="{http://www.e-mis.com/emisopen}dt.Duration" minOccurs="0"/>
     *           &lt;element name="high" type="{http://www.e-mis.com/emisopen}dt.DatePart" minOccurs="0"/>
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
    @XmlType(name = "", propOrder = {
        "low",
        "width",
        "high"
    })
    public static class BaseLow {

        @XmlElement(namespace = "http://www.e-mis.com/emisopen", required = true)
        protected DtDatePart low;
        @XmlElement(namespace = "http://www.e-mis.com/emisopen")
        protected DtDuration width;
        @XmlElement(namespace = "http://www.e-mis.com/emisopen")
        protected DtDatePart high;

        /**
         * Gets the value of the low property.
         * 
         * @return
         *     possible object is
         *     {@link DtDatePart }
         *     
         */
        public DtDatePart getLow() {
            return low;
        }

        /**
         * Sets the value of the low property.
         * 
         * @param value
         *     allowed object is
         *     {@link DtDatePart }
         *     
         */
        public void setLow(DtDatePart value) {
            this.low = value;
        }

        /**
         * Gets the value of the width property.
         * 
         * @return
         *     possible object is
         *     {@link DtDuration }
         *     
         */
        public DtDuration getWidth() {
            return width;
        }

        /**
         * Sets the value of the width property.
         * 
         * @param value
         *     allowed object is
         *     {@link DtDuration }
         *     
         */
        public void setWidth(DtDuration value) {
            this.width = value;
        }

        /**
         * Gets the value of the high property.
         * 
         * @return
         *     possible object is
         *     {@link DtDatePart }
         *     
         */
        public DtDatePart getHigh() {
            return high;
        }

        /**
         * Sets the value of the high property.
         * 
         * @param value
         *     allowed object is
         *     {@link DtDatePart }
         *     
         */
        public void setHigh(DtDatePart value) {
            this.high = value;
        }

    }

}

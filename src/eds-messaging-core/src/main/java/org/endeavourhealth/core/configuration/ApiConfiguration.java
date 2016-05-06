
package org.endeavourhealth.core.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="GetData">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Pipeline" type="{}Pipeline"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="GetDataAsync">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Pipeline" type="{}Pipeline"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PostMessage">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Pipeline" type="{}Pipeline"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PostMessageAsync">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Pipeline" type="{}Pipeline"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
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
@XmlType(name = "", propOrder = {
    "getData",
    "getDataAsync",
    "postMessage",
    "postMessageAsync"
})
@XmlRootElement(name = "ApiConfiguration")
public class ApiConfiguration {

    @XmlElement(name = "GetData", required = true)
    protected ApiConfiguration.GetData getData;
    @XmlElement(name = "GetDataAsync", required = true)
    protected ApiConfiguration.GetDataAsync getDataAsync;
    @XmlElement(name = "PostMessage", required = true)
    protected ApiConfiguration.PostMessage postMessage;
    @XmlElement(name = "PostMessageAsync", required = true)
    protected ApiConfiguration.PostMessageAsync postMessageAsync;

    /**
     * Gets the value of the getData property.
     * 
     * @return
     *     possible object is
     *     {@link ApiConfiguration.GetData }
     *     
     */
    public ApiConfiguration.GetData getGetData() {
        return getData;
    }

    /**
     * Sets the value of the getData property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApiConfiguration.GetData }
     *     
     */
    public void setGetData(ApiConfiguration.GetData value) {
        this.getData = value;
    }

    /**
     * Gets the value of the getDataAsync property.
     * 
     * @return
     *     possible object is
     *     {@link ApiConfiguration.GetDataAsync }
     *     
     */
    public ApiConfiguration.GetDataAsync getGetDataAsync() {
        return getDataAsync;
    }

    /**
     * Sets the value of the getDataAsync property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApiConfiguration.GetDataAsync }
     *     
     */
    public void setGetDataAsync(ApiConfiguration.GetDataAsync value) {
        this.getDataAsync = value;
    }

    /**
     * Gets the value of the postMessage property.
     * 
     * @return
     *     possible object is
     *     {@link ApiConfiguration.PostMessage }
     *     
     */
    public ApiConfiguration.PostMessage getPostMessage() {
        return postMessage;
    }

    /**
     * Sets the value of the postMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApiConfiguration.PostMessage }
     *     
     */
    public void setPostMessage(ApiConfiguration.PostMessage value) {
        this.postMessage = value;
    }

    /**
     * Gets the value of the postMessageAsync property.
     * 
     * @return
     *     possible object is
     *     {@link ApiConfiguration.PostMessageAsync }
     *     
     */
    public ApiConfiguration.PostMessageAsync getPostMessageAsync() {
        return postMessageAsync;
    }

    /**
     * Sets the value of the postMessageAsync property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApiConfiguration.PostMessageAsync }
     *     
     */
    public void setPostMessageAsync(ApiConfiguration.PostMessageAsync value) {
        this.postMessageAsync = value;
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
     *         &lt;element name="Pipeline" type="{}Pipeline"/>
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
        "pipeline"
    })
    public static class GetData {

        @XmlElement(name = "Pipeline", required = true)
        protected Pipeline pipeline;

        /**
         * Gets the value of the pipeline property.
         * 
         * @return
         *     possible object is
         *     {@link Pipeline }
         *     
         */
        public Pipeline getPipeline() {
            return pipeline;
        }

        /**
         * Sets the value of the pipeline property.
         * 
         * @param value
         *     allowed object is
         *     {@link Pipeline }
         *     
         */
        public void setPipeline(Pipeline value) {
            this.pipeline = value;
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
     *         &lt;element name="Pipeline" type="{}Pipeline"/>
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
        "pipeline"
    })
    public static class GetDataAsync {

        @XmlElement(name = "Pipeline", required = true)
        protected Pipeline pipeline;

        /**
         * Gets the value of the pipeline property.
         * 
         * @return
         *     possible object is
         *     {@link Pipeline }
         *     
         */
        public Pipeline getPipeline() {
            return pipeline;
        }

        /**
         * Sets the value of the pipeline property.
         * 
         * @param value
         *     allowed object is
         *     {@link Pipeline }
         *     
         */
        public void setPipeline(Pipeline value) {
            this.pipeline = value;
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
     *         &lt;element name="Pipeline" type="{}Pipeline"/>
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
        "pipeline"
    })
    public static class PostMessage {

        @XmlElement(name = "Pipeline", required = true)
        protected Pipeline pipeline;

        /**
         * Gets the value of the pipeline property.
         * 
         * @return
         *     possible object is
         *     {@link Pipeline }
         *     
         */
        public Pipeline getPipeline() {
            return pipeline;
        }

        /**
         * Sets the value of the pipeline property.
         * 
         * @param value
         *     allowed object is
         *     {@link Pipeline }
         *     
         */
        public void setPipeline(Pipeline value) {
            this.pipeline = value;
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
     *         &lt;element name="Pipeline" type="{}Pipeline"/>
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
        "pipeline"
    })
    public static class PostMessageAsync {

        @XmlElement(name = "Pipeline", required = true)
        protected Pipeline pipeline;

        /**
         * Gets the value of the pipeline property.
         * 
         * @return
         *     possible object is
         *     {@link Pipeline }
         *     
         */
        public Pipeline getPipeline() {
            return pipeline;
        }

        /**
         * Sets the value of the pipeline property.
         * 
         * @param value
         *     allowed object is
         *     {@link Pipeline }
         *     
         */
        public void setPipeline(Pipeline value) {
            this.pipeline = value;
        }

    }

}

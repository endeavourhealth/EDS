
package org.endeavourhealthX.subscriber.configuration.models;

import javax.xml.bind.annotation.*;


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
 *         &lt;element name="PostgreSQLConnection" type="{}PostgreSQLConnection"/>
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
    "postgreSQLConnection"
})
@XmlRootElement(name = "SubscriberConfiguration")
public class SubscriberConfiguration {

    @XmlElement(name = "PostgreSQLConnection", required = true)
    protected PostgreSQLConnection postgreSQLConnection;

    /**
     * Gets the value of the postgreSQLConnection property.
     * 
     * @return
     *     possible object is
     *     {@link PostgreSQLConnection }
     *     
     */
    public PostgreSQLConnection getPostgreSQLConnection() {
        return postgreSQLConnection;
    }

    /**
     * Sets the value of the postgreSQLConnection property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostgreSQLConnection }
     *     
     */
    public void setPostgreSQLConnection(PostgreSQLConnection value) {
        this.postgreSQLConnection = value;
    }

}

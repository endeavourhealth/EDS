
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TestRequestListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TestRequestListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TestRequestDetail" type="{http://www.e-mis.com/emisopen/MedicalRecord}TestRequestType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestRequestListType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "testRequestDetail"
})
public class TestRequestListType {

    @XmlElement(name = "TestRequestDetail", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<TestRequestType> testRequestDetail;

    /**
     * Gets the value of the testRequestDetail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the testRequestDetail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTestRequestDetail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TestRequestType }
     * 
     * 
     */
    public List<TestRequestType> getTestRequestDetail() {
        if (testRequestDetail == null) {
            testRequestDetail = new ArrayList<TestRequestType>();
        }
        return this.testRequestDetail;
    }

}

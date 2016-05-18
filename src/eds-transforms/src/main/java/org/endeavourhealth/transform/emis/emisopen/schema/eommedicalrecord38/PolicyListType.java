
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PolicyListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PolicyListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PolicyType" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="DBID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *                   &lt;element name="RefId" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *                   &lt;element name="GUID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="Term" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="FileStatus" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *                   &lt;element name="UserList" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="PolicyUser" type="{http://www.e-mis.com/emisopen/MedicalRecord}PersonType" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
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
@XmlType(name = "PolicyListType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord", propOrder = {
    "policyType"
})
public class PolicyListType {

    @XmlElement(name = "PolicyType", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
    protected List<PolicyListType.PolicyType> policyType;

    /**
     * Gets the value of the policyType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the policyType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPolicyType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PolicyListType.PolicyType }
     * 
     * 
     */
    public List<PolicyListType.PolicyType> getPolicyType() {
        if (policyType == null) {
            policyType = new ArrayList<PolicyListType.PolicyType>();
        }
        return this.policyType;
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
     *         &lt;element name="DBID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
     *         &lt;element name="RefId" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
     *         &lt;element name="GUID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="Term" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="FileStatus" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
     *         &lt;element name="UserList" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="PolicyUser" type="{http://www.e-mis.com/emisopen/MedicalRecord}PersonType" maxOccurs="unbounded" minOccurs="0"/>
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
        "dbid",
        "refId",
        "guid",
        "term",
        "fileStatus",
        "userList"
    })
    public static class PolicyType {

        @XmlElement(name = "DBID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected BigInteger dbid;
        @XmlElement(name = "RefId", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected BigInteger refId;
        @XmlElement(name = "GUID", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String guid;
        @XmlElement(name = "Term", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected String term;
        @XmlElement(name = "FileStatus", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected BigInteger fileStatus;
        @XmlElement(name = "UserList", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
        protected PolicyListType.PolicyType.UserList userList;

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
         * Gets the value of the refId property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getRefId() {
            return refId;
        }

        /**
         * Sets the value of the refId property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setRefId(BigInteger value) {
            this.refId = value;
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
         * Gets the value of the term property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTerm() {
            return term;
        }

        /**
         * Sets the value of the term property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTerm(String value) {
            this.term = value;
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
         * Gets the value of the userList property.
         * 
         * @return
         *     possible object is
         *     {@link PolicyListType.PolicyType.UserList }
         *     
         */
        public PolicyListType.PolicyType.UserList getUserList() {
            return userList;
        }

        /**
         * Sets the value of the userList property.
         * 
         * @param value
         *     allowed object is
         *     {@link PolicyListType.PolicyType.UserList }
         *     
         */
        public void setUserList(PolicyListType.PolicyType.UserList value) {
            this.userList = value;
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
         *         &lt;element name="PolicyUser" type="{http://www.e-mis.com/emisopen/MedicalRecord}PersonType" maxOccurs="unbounded" minOccurs="0"/>
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
            "policyUser"
        })
        public static class UserList {

            @XmlElement(name = "PolicyUser", namespace = "http://www.e-mis.com/emisopen/MedicalRecord")
            protected List<PersonType> policyUser;

            /**
             * Gets the value of the policyUser property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the policyUser property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getPolicyUser().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link PersonType }
             * 
             * 
             */
            public List<PersonType> getPolicyUser() {
                if (policyUser == null) {
                    policyUser = new ArrayList<PersonType>();
                }
                return this.policyUser;
            }

        }

    }

}


package org.endeavourhealth.transform.emis.emisopen.schema.eomslotsforsession;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.transform.emis.emisopen.schema.eomslotsforsession package. 
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

    private final static QName _SlotList_QNAME = new QName("", "SlotList");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.transform.emis.emisopen.schema.eomslotsforsession
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SlotListStruct }
     * 
     */
    public SlotListStruct createSlotListStruct() {
        return new SlotListStruct();
    }

    /**
     * Create an instance of {@link PatientListStruct }
     * 
     */
    public PatientListStruct createPatientListStruct() {
        return new PatientListStruct();
    }

    /**
     * Create an instance of {@link PatientStruct }
     * 
     */
    public PatientStruct createPatientStruct() {
        return new PatientStruct();
    }

    /**
     * Create an instance of {@link TypeStruct }
     * 
     */
    public TypeStruct createTypeStruct() {
        return new TypeStruct();
    }

    /**
     * Create an instance of {@link SlotStruct }
     * 
     */
    public SlotStruct createSlotStruct() {
        return new SlotStruct();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SlotListStruct }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "SlotList")
    public JAXBElement<SlotListStruct> createSlotList(SlotListStruct value) {
        return new JAXBElement<SlotListStruct>(_SlotList_QNAME, SlotListStruct.class, null, value);
    }

}

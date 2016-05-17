
package org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AppointmentSessionList }
     * 
     */
    public AppointmentSessionList createAppointmentSessionList() {
        return new AppointmentSessionList();
    }

    /**
     * Create an instance of {@link AppointmentSessionStruct }
     * 
     */
    public AppointmentSessionStruct createAppointmentSessionStruct() {
        return new AppointmentSessionStruct();
    }

    /**
     * Create an instance of {@link SlotTypeList }
     * 
     */
    public SlotTypeList createSlotTypeList() {
        return new SlotTypeList();
    }

    /**
     * Create an instance of {@link SiteStruct }
     * 
     */
    public SiteStruct createSiteStruct() {
        return new SiteStruct();
    }

    /**
     * Create an instance of {@link SlotsStruct }
     * 
     */
    public SlotsStruct createSlotsStruct() {
        return new SlotsStruct();
    }

    /**
     * Create an instance of {@link HolderList }
     * 
     */
    public HolderList createHolderList() {
        return new HolderList();
    }

    /**
     * Create an instance of {@link HolderStruct }
     * 
     */
    public HolderStruct createHolderStruct() {
        return new HolderStruct();
    }

}

package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;
import org.endeavourhealth.transform.hl7v2.parser.Datatype;

public class Pl extends Datatype {

    public Pl(GenericDatatype datatype) {
        super(datatype);
    }

    public String getPointOfCare() { return this.getComponent(1); }
    public String getRoom() { return this.getComponent(2); }
    public String getBed() { return this.getComponent(3); }
    public String getFacility() { return this.getComponent(4); }
    public String getLocationStatus() { return this.getComponent(5); }
    public String getPersonLocationType() { return this.getComponent(6); }
    public String getBuilding() { return this.getComponent(7); }
    public String getFloor() { return this.getComponent(8); }
    public String getLocationDescription() { return this.getComponent(9); }
}

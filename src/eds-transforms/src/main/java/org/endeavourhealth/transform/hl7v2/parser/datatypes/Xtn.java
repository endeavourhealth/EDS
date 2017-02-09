package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.Datatype;
import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;

public class Xtn extends Datatype {
    public Xtn(GenericDatatype datatype) {
        super(datatype);
    }

    public String getTelephoneNumber() { return this.getComponent(1); }
    public String getUseCode() { return this.getComponent(2); }
    public String getEquipmentType() { return this.getComponent(3); }
    public String getEmailAddress() { return this.getComponent(4); }
    public String getCountryCode() { return this.getComponent(5); }
    public String getAreaCode() { return this.getComponent(6); }
    public String getPhoneNumber() { return this.getComponent(7); }
    public String getExtension() { return this.getComponent(8); }
    public String getText() { return this.getComponent(9); }
}

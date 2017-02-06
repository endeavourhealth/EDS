package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.Datatype;
import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;

public class Xpn extends Datatype {
    public Xpn(GenericDatatype datatype) {
        super(datatype);
    }

    public String getFamilyName() { return this.getComponent(1); }
    public String getGivenName() { return this.getComponent(2); }
    public String getMiddleName() { return this.getComponent(3); }
    public String getSuffix() { return this.getComponent(4); }
    public String getPrefix() { return this.getComponent(5); }
    public String getDegree() { return this.getComponent(6); }
    public String getNameTypeCode() { return this.getComponent(7); }
    public String getNameRepresentationCode() { return this.getComponent(8); }
}

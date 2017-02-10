package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;
import org.endeavourhealth.transform.hl7v2.parser.Datatype;

public class Xon extends Datatype {

    public Xon(GenericDatatype datatype) {
        super(datatype);
    }

    public String getOrganizationName() { return this.getComponent(1); }
    public String getOrganizationNameTypeCode() { return this.getComponent(2); }
    public String getIdNumber() { return this.getComponent(3); }
    public String getCheckDigit() { return this.getComponent(4); }
    public String getCodeIdentifyingTheCheckDigitSchemeEmployed() { return this.getComponent(5); }
    public String getAssigningAuthority() { return this.getComponent(6); }
    public String getIdentifierTypeCode() { return this.getComponent(7); }
    public String getAssigningFacilityId() { return this.getComponent(8); }
    public String getNameRepresentationCode() { return this.getComponent(9); }
}

package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;
import org.endeavourhealth.transform.hl7v2.parser.Datatype;

public class Cx extends Datatype {

    public Cx(GenericDatatype datatype) {
        super(datatype);
    }

    public String getId() { return this.getComponent(1); }
    public String getCheckDigit() { return this.getComponent(2); }
    public String getCheckDigitCodeScheme() { return this.getComponent(3); }
    public String getAssigningAuthority() { return this.getComponent(4); }
    public String getIdentifierTypeCode() { return this.getComponent(5); }
    public String getAssigningFacility() { return this.getComponent(6); }
}

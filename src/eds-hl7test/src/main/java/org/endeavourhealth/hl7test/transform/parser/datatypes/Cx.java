package org.endeavourhealth.hl7test.transform.parser.datatypes;

import org.endeavourhealth.hl7test.transform.parser.GenericDatatype;
import org.endeavourhealth.hl7test.transform.parser.Datatype;

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

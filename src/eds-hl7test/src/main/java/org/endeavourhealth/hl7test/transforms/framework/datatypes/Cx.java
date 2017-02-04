package org.endeavourhealth.hl7test.transforms.framework.datatypes;

import org.endeavourhealth.hl7test.transforms.framework.GenericDatatype;
import org.endeavourhealth.hl7test.transforms.framework.Datatype;

public class Cx extends Datatype {

    public Cx(GenericDatatype datatype) {
        super(datatype);
    }

    public String getId() { return datatype.getComponent(1); }
    public String getCheckDigit() { return datatype.getComponent(2); }
    public String getCheckDigitCodeScheme() { return datatype.getComponent(3); }
    public String getAssigningAuthority() { return datatype.getComponent(4); }
    public String getIdentifierTypeCode() { return datatype.getComponent(5); }
    public String getAssigningFacility() { return datatype.getComponent(6); }
}

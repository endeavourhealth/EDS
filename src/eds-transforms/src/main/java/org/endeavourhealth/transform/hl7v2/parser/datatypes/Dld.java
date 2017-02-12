package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;
import org.endeavourhealth.transform.hl7v2.parser.Datatype;

import java.time.LocalDateTime;

public class Dld extends Datatype {

    public Dld(GenericDatatype datatype) {
        super(datatype);
    }

    public String getDischargeLocation() {
        return this.getComponentAsString(1);
    }
    public LocalDateTime getEffectiveDate() { return this.getComponentAsDateTime(2); }

}
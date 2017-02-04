package org.endeavourhealth.hl7test.transforms.framework.datatypes;

import org.endeavourhealth.hl7test.transforms.framework.Datatype;
import org.endeavourhealth.hl7test.transforms.framework.GenericDatatype;

public class Xad extends Datatype {
    public Xad(GenericDatatype datatype) {
        super(datatype);
    }

    public String getStreetAddress() { return this.getComponent(1); }
    public String getOtherDesignation() { return this.getComponent(2); }
    public String getCity() { return this.getComponent(3); }
    public String getProvince() { return this.getComponent(4); }
    public String getPostCode() { return this.getComponent(5); }
    public String getCountry() { return this.getComponent(6); }
    public String getAddressType() { return this.getComponent(7); }
    public String getOtherGeographicDesignation() { return this.getComponent(8); }
    public String getCountyCode() { return this.getComponent(9); }
    public String getCensusTract() { return this.getComponent(10); }
    public String getAddressRepresentationCode() { return this.getComponent(11); }
}

package org.endeavourhealth.transform.hl7v2.parser.datatypes;

import org.endeavourhealth.transform.hl7v2.parser.Datatype;
import org.endeavourhealth.transform.hl7v2.parser.GenericDatatype;

// extended composite id and name for persons
public class Xcn extends Datatype implements IXpn, ICx {
    public Xcn(GenericDatatype datatype) { super(datatype); }

    public String getId() { return this.getComponent(1); }
    public String getFamilyName() { return this.getComponent(2); }
    public String getGivenName() { return this.getComponent(3); }
    public String getMiddleName() { return this.getComponent(4); }
    public String getSuffix() { return this.getComponent(5); }
    public String getPrefix() { return this.getComponent(6);}
    public String getDegree() { return this.getComponent(7); }
    public String getSourceTable() { return this.getComponent(8); }
    public String getAssigningAuthority() { return this.getComponent(9); }
    public String getNameTypeCode() { return this.getComponent(10); }
    public String getCheckDigit() { return this.getComponent(11); }
    public String getCheckDigitCodeScheme() { return this.getComponent(12); }
    public String getIdentifierTypeCode() { return this.getComponent(13); }
    public String getAssigningFacility() { return this.getComponent(14); }
    public String getNameRepresentationCode() { return this.getComponent(15); }
}

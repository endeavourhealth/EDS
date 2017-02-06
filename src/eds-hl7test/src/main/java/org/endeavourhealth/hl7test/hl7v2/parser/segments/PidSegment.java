package org.endeavourhealth.hl7test.hl7v2.parser.segments;

import org.endeavourhealth.hl7test.hl7v2.parser.*;
import org.endeavourhealth.hl7test.hl7v2.parser.datatypes.*;

import java.time.LocalDateTime;
import java.util.List;

public class PidSegment extends Segment {
    public PidSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public int getSetId() throws ParseException { return this.getFieldAsInteger(1); }
    public Cx getExternalPatientId() { return this.getFieldAsDatatype(2, Cx.class); }
    public List<Cx> getInternalPatientId() { return this.getFieldAsDatatypes(3, Cx.class); }
    public Cx getAlternatePatientId() { return this.getFieldAsDatatype(4, Cx.class); }
    public List<Xpn> getPatientNames() { return this.getFieldAsDatatypes(5, Xpn.class); }
    public List<Xpn> getMothersMaidenNames() { return this.getFieldAsDatatypes(6, Xpn.class); }
    public LocalDateTime getDateOfBirth() throws ParseException { return this.getFieldAsDate(7); }
    public String getSex() { return this.getFieldAsString(8); }
    public List<Xpn> getPatientAlias() { return this.getFieldAsDatatypes(9, Xpn.class); }
    public Ce getRace() { return this.getFieldAsDatatype(10, Ce.class); }
    public List<Xad> getAddresses() { return this.getFieldAsDatatypes(11, Xad.class); }

    /*
    CountyCode
    PhoneNumberHome
    PhoneNumberBusiness
    PrimaryLanguage
    MaritalStatus
    Religion
    PatientAccountNumber
    SSNNumberPatient
    DriversLicenseNumber
    MothersIdentifier
    EthnicGroup
    BirthPlace
    MultipleBirthIndicator
    BirthOrder
    Citizenship
    VeteransMilitaryStatus
            Nationality
    PatientDeathDateandTime
    PatientDeathIndicator
*/

    public Ce getNationality() { return this.getFieldAsDatatype(28, Ce.class); }
}

package org.endeavourhealth.hl7test.transforms.framework.segments;

import org.endeavourhealth.hl7test.transforms.framework.*;
import org.endeavourhealth.hl7test.transforms.framework.datatypes.Cx;

import java.util.List;

public class PidSegment extends Segment {
    public PidSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public int getSetID() throws ParseException { return this.getFieldAsInteger(1); }
    public Cx getExternalPatientId() { return this.getFieldDatatype(2, Cx.class); }
    public List<Cx> getInternalPatientId() throws ParseException { return this.getFieldDatatypes(3, Cx.class); }

    public String AlternatePatientID;

    /*
    PatientName

    MothersMaidenName

    DateTimeofBirth
            Sex

    PatientAlias

    Race

    PatientAddress

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
}

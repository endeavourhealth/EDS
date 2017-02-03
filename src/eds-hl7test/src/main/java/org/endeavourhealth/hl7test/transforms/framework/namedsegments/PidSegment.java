package org.endeavourhealth.hl7test.transforms.framework.namedsegments;

import org.endeavourhealth.hl7test.transforms.framework.Field;
import org.endeavourhealth.hl7test.transforms.framework.ParseException;
import org.endeavourhealth.hl7test.transforms.framework.Segment;
import org.endeavourhealth.hl7test.transforms.framework.Seperators;

public class PidSegment extends Segment {
    public PidSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public int getSetID() throws ParseException {
        return this.getFieldAsInteger(1);
    }

    public Field ExternalPatientID;

    public String InternalPatientID;

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

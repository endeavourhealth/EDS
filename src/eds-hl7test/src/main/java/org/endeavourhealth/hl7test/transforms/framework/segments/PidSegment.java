package org.endeavourhealth.hl7test.transforms.framework.segments;

import org.endeavourhealth.hl7test.transforms.framework.*;
import org.endeavourhealth.hl7test.transforms.framework.datatypes.Cx;

import java.util.List;

public class PidSegment extends Segment {
    public PidSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    public int getSetID() throws ParseException {
        return this.getFieldAsInteger(1);
    }

    public Field getExternalPatientIdField() {
        return this.getField(2);
    }

    public List<Cx> getInternalPatientIdField() throws ParseException {
        Field field = this.getField(3);

        return field.getDatatypes(Cx.class);
    }

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

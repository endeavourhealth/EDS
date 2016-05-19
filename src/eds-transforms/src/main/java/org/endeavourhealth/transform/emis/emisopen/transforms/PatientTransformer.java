package org.endeavourhealth.transform.emis.emisopen.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.RegistrationType;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientTransformer
{
    public static Patient transform(MedicalRecordType medicalRecord) throws TransformException
    {
        RegistrationType source = medicalRecord.getRegistration();

        if (source == null)
            throw new TransformException("Registration element is null");

        Patient target = new Patient();

        if (StringUtils.isBlank(source.getGUID()))
            throw new TransformException("Patient GUID is empty");

        target.setId(source.getGUID());

        for (Identifier identifier : transformIdentifiers(source))
            target.addIdentifier(identifier);



        return target;
    }

    private static List<Identifier> transformIdentifiers(RegistrationType registrationType)
    {
        List<Identifier> identifiers = new ArrayList<>();

        if (StringUtils.isNotBlank(registrationType.getNhsNumber()))
        {
            Identifier identifier = new Identifier()
                    .setSystem(FhirUris.IDENTIFIER_SYSTEM_NHSNUMBER)
                    .setValue(registrationType.getNhsNumber());

            identifiers.add(identifier);
        }

        if (StringUtils.isNotBlank(registrationType.getCHINumber()))
        {
            Identifier identifier = new Identifier()
                    .setSystem(FhirUris.IDENTIFIER_SYSTEM_CHINUMBER)
                    .setValue(registrationType.getCHINumber());

            identifiers.add(identifier);
        }

        return identifiers;
    }
}

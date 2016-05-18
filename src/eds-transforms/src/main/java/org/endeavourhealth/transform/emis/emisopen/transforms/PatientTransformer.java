package org.endeavourhealth.transform.emis.emisopen.transforms;

import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.RegistrationType;
import org.hl7.fhir.instance.model.Patient;

public class PatientTransformer
{
    public static Patient transform(MedicalRecordType medicalRecord)
    {
        RegistrationType source = medicalRecord.getRegistration();

        Patient patient = new Patient();

        



        return patient;
    }
}

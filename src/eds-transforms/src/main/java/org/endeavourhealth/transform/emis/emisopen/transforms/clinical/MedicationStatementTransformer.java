package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicationType;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class MedicationStatementTransformer
{
    public static List<Resource> transform(MedicalRecordType medicalRecordType) throws TransformException
    {
        List<Resource> resource = new ArrayList<>();

        for (MedicationType medicationType : medicalRecordType.getMedicationList().getMedication())
            resource.add(transform(medicationType, medicalRecordType.getRegistration().getGUID()));

        return resource;
    }

    private static MedicationStatement transform(MedicationType medicationType, String patientUuid) throws TransformException
    {
        MedicationStatement medicationStatement = new MedicationStatement();
        medicationStatement.setId(medicationType.getGUID());
        medicationStatement.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_MEDICATION_AUTHORISATION));

        medicationStatement.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patientUuid));

        return medicationStatement;
    }
}

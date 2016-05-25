package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AllergyType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class AllergyTransformer
{
    public static List<Resource> transform(MedicalRecordType medicalRecordType) throws TransformException
    {
        List<Resource> resource = new ArrayList<>();

        for (AllergyType allergyType : medicalRecordType.getAllergyList().getAllergy())
            resource.add(transform(allergyType, medicalRecordType.getRegistration().getGUID()));

        return resource;
    }

    private static AllergyIntolerance transform(AllergyType allergyType, String patientUuid) throws TransformException
    {
        AllergyIntolerance allergy = new AllergyIntolerance();
        allergy.setId(allergyType.getGUID());
        allergy.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_ALLERGY_INTOLERANCE));

        allergy.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patientUuid));
        allergy.setRecorder(ReferenceHelper.createReference(ResourceType.Practitioner, allergyType.getOriginalAuthor().getUser().getGUID()));

        allergy.setOnsetElement(DateConverter.convertPartialDateToDateTimeType(allergyType.getAssignedDate(), allergyType.getAssignedTime(), allergyType.getDatePart()));




        return allergy;
    }
}

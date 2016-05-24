package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AllergyType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class AllergyTransformer
{
    public static List<Resource> transform(MedicalRecordType medicalRecordType)
    {
        List<Resource> resource = new ArrayList<>();

        for (AllergyType allergyType : medicalRecordType.getAllergyList().getAllergy())
            resource.add(transform(allergyType));

        return resource;
    }

    private static AllergyIntolerance transform(AllergyType allergyType)
    {
        AllergyIntolerance allergy = new AllergyIntolerance();
        allergy.setId(allergyType.getGUID());
        allergy.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_ALLERGY_INTOLERANCE));





        return allergy;
    }
}

package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.FamilyMemberHistory;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.ResourceType;

final class FamilyHistoryTransformer
{
    public static FamilyMemberHistory transform(EventType eventType, String patientUuid) throws TransformException
    {
        FamilyMemberHistory familyMemberHistory = new FamilyMemberHistory();
        familyMemberHistory.setId(eventType.getGUID());
        familyMemberHistory.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_FAMILY_MEMBER_HISTORY));
        familyMemberHistory.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patientUuid));

        return familyMemberHistory;
    }
}

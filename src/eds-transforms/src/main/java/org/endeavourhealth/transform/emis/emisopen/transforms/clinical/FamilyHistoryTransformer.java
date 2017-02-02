package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AuthorType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.QualifierType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

final class FamilyHistoryTransformer
{
    public static FamilyMemberHistory transform(EventType eventType, String patientUuid) throws TransformException
    {
        FamilyMemberHistory familyMemberHistory = new FamilyMemberHistory();
        familyMemberHistory.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_FAMILY_MEMBER_HISTORY));

        familyMemberHistory.setId(eventType.getGUID());
        familyMemberHistory.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patientUuid));

        familyMemberHistory.setRelationship(getRelationship(eventType));

        FamilyMemberHistory.FamilyMemberHistoryConditionComponent familyMemberHistoryConditionComponent = new FamilyMemberHistory.FamilyMemberHistoryConditionComponent();
        familyMemberHistoryConditionComponent.setCode(CodeConverter.convert(eventType.getCode(), eventType.getDescriptiveText()));
        familyMemberHistory.addCondition(familyMemberHistoryConditionComponent);

        familyMemberHistory.addExtension(getFamilyMemberHistoryRecorderExtension(eventType.getOriginalAuthor()));

        return familyMemberHistory;
    }

    private static Extension getFamilyMemberHistoryRecorderExtension(AuthorType authorType) throws TransformException
    {
        return new Extension()
                .setUrl(FhirExtensionUri.RECORDED_BY)
                .setValue(ReferenceHelper.createReference(ResourceType.Practitioner, authorType.getUser().getGUID()));
    }

    private static CodeableConcept getRelationship(EventType eventType)
    {
        final String QUALIFIER_GROUP_TERM_FAMILY_MEMBER = "Family member";

        QualifierType qualifierType = eventType
                .getQualifierList()
                .getQualifier()
                .stream()
                .filter(t -> t.getGroup().getTerm().equals(QUALIFIER_GROUP_TERM_FAMILY_MEMBER))
                .collect(StreamExtension.singleOrNullCollector());

        CodeableConcept codeableConcept = new CodeableConcept();

        Coding coding = new Coding();
        coding.setDisplay(qualifierType.getQualifierItemID().getTerm());

        codeableConcept.addCoding(coding);

        // needs mapping back to SNOMED

        return codeableConcept;
    }
}

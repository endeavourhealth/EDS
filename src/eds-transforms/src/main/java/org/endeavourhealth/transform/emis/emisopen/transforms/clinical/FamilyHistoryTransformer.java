package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AuthorType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.QualifierType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.fhir.schema.FamilyMember;
import org.hl7.fhir.instance.model.*;

final class FamilyHistoryTransformer {

    private static final String QUALIFIER_GROUP_TERM_FAMILY_MEMBER = "Family member";

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

    private static CodeableConcept getRelationship(EventType eventType) {

        //if the event doesn't have the qualifier to say who the family member is, fall back on the generic "family member"
        if (eventType.getQualifierList() == null) {
            return CodeableConceptHelper.createCodeableConcept(FamilyMember.FAMILY_MEMBER);
        }

        QualifierType qualifierType = eventType
                .getQualifierList()
                .getQualifier()
                .stream()
                .filter(t -> t.getGroup().getTerm().equals(QUALIFIER_GROUP_TERM_FAMILY_MEMBER))
                .collect(StreamExtension.singleOrNullCollector());

        //we have event types that don't have a qualifier of the right type, so fall back to using the generic relationship type
        if (qualifierType == null
                || qualifierType.getQualifierItemID() == null) {
            return CodeableConceptHelper.createCodeableConcept(FamilyMember.FAMILY_MEMBER);
        }

        CodeableConcept codeableConcept = new CodeableConcept();

        Coding coding = new Coding();
        coding.setDisplay(qualifierType.getQualifierItemID().getTerm());

        codeableConcept.addCoding(coding);

        // needs mapping back to SNOMED

        return codeableConcept;
    }
}

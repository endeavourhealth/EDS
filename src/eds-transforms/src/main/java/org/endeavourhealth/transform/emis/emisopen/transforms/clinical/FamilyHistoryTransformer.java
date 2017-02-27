package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.AnnotationHelper;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.schema.FamilyMember;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AuthorType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;

final class FamilyHistoryTransformer extends ClinicalTransformerBase {



    public static void transform(EventType eventType, List<Resource> results, String patientGuid) throws TransformException
    {
        FamilyMemberHistory fhirFamilyMemberHistory = new FamilyMemberHistory();
        fhirFamilyMemberHistory.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_FAMILY_MEMBER_HISTORY));

        EmisOpenHelper.setUniqueId(fhirFamilyMemberHistory, patientGuid, eventType.getGUID());

        fhirFamilyMemberHistory.setPatient(EmisOpenHelper.createPatientReference(patientGuid));

        fhirFamilyMemberHistory.setRelationship(getRelationship(eventType));

        FamilyMemberHistory.FamilyMemberHistoryConditionComponent familyMemberHistoryConditionComponent = new FamilyMemberHistory.FamilyMemberHistoryConditionComponent();
        familyMemberHistoryConditionComponent.setCode(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));
        fhirFamilyMemberHistory.addCondition(familyMemberHistoryConditionComponent);

        fhirFamilyMemberHistory.addExtension(getFamilyMemberHistoryRecorderExtension(eventType.getOriginalAuthor()));

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirFamilyMemberHistory.setNote(AnnotationHelper.createAnnotation(text));
        }

        Date dateRecorded = findRecordedDate(eventType.getOriginalAuthor());
        addRecordedDateExtension(fhirFamilyMemberHistory, dateRecorded);

        String recordedByGuid = findRecordedUserGuid(eventType.getOriginalAuthor());
        addRecordedByExtension(fhirFamilyMemberHistory, recordedByGuid);

        linkToProblem(eventType, patientGuid, fhirFamilyMemberHistory, results);

        results.add(fhirFamilyMemberHistory);
    }

    private static Extension getFamilyMemberHistoryRecorderExtension(AuthorType authorType) throws TransformException
    {
        return new Extension()
                .setUrl(FhirExtensionUri.RECORDED_BY)
                .setValue(EmisOpenHelper.createPractitionerReference(authorType.getUser().getGUID()));
    }

    private static CodeableConcept getRelationship(EventType eventType) {

        List<String> familyMembers = findQualifierTerms(eventType, EmisOpenHelper.QUALIFIER_GROUP_TERM_FAMILY_MEMBER);

        //if the event doesn't have the qualifier to say who the family member is, fall back on the generic "family member"
        if (familyMembers.isEmpty()) {
            return CodeableConceptHelper.createCodeableConcept(FamilyMember.FAMILY_MEMBER);
        }

        String members = String.join(", ", familyMembers);
        return CodeableConceptHelper.createCodeableConcept(members);
    }

}

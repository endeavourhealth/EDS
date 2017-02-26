package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.IntegerType;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Resource;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class ProblemTransformer extends ClinicalTransformerBase {

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String organisationGuid, String patientGuid) throws TransformException {

        //pretty much any item in the record can be a problem, so we need to go through everything
        if (medicalRecord.getEventList() != null) {
            for (EventType event: medicalRecord.getEventList().getEvent()) {
                createProblem(event, patientGuid, resources);
            }
        }

        if (medicalRecord.getConsultationList() != null) {
            for (ConsultationType consultation: medicalRecord.getConsultationList().getConsultation()) {
                if (consultation.getElementList() != null) {
                    for (ElementListType.ConsultationElement element: consultation.getElementList().getConsultationElement()) {
                        if (element.getEvent() != null) {
                            createProblem(element.getEvent(), patientGuid, resources);
                        }
                        if (element.getDiary() != null) {
                            createProblem(element.getDiary(), patientGuid, resources);
                        }
                        if (element.getReferral() != null) {
                            createProblem(element.getReferral(), patientGuid, resources);
                        }
                        if (element.getAllergy() != null) {
                            createProblem(element.getAllergy(), patientGuid, resources);
                        }
                    }
                }
            }
        }

        if (medicalRecord.getReferralList() != null) {
            for (ReferralType referral: medicalRecord.getReferralList().getReferral()) {
                createProblem(referral, patientGuid, resources);
            }
        }

        if (medicalRecord.getAllergyList() != null) {
            for (AllergyType allergy: medicalRecord.getAllergyList().getAllergy()) {
                createProblem(allergy, patientGuid, resources);
            }
        }

        if (medicalRecord.getDiaryList() != null) {
            for (DiaryType diary: medicalRecord.getDiaryList().getDiary()) {
                createProblem(diary, patientGuid, resources);
            }
        }
    }

    private static void createProblem(CodedItemBaseType codedItem, String patientUuid, List<Resource> resources) throws TransformException {
        if (codedItem.getProblem() == null) {
            return;
        }

        Condition fhirProblem = new Condition();
        fhirProblem.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROBLEM));

        String eventGuid = codedItem.getGUID();
        EmisOpenHelper.setUniqueId(fhirProblem, patientUuid, eventGuid);

        fhirProblem.setPatient(EmisOpenHelper.createPatientReference(patientUuid));

        IdentType author = codedItem.getAuthorID();
        if (author != null) {
            fhirProblem.setAsserter(EmisOpenHelper.createPractitionerReference(author.getGUID()));
        }

        Date dateRecorded = findRecordedDate(codedItem.getOriginalAuthor());
        if (dateRecorded != null) {
            fhirProblem.setDateRecorded(dateRecorded);
        }

        String recordedByGuid = findRecordedUserGuid(codedItem.getOriginalAuthor());
        addRecordedByExtension(fhirProblem, recordedByGuid);

        fhirProblem.setCode(CodeConverter.convert(codedItem.getCode(), codedItem.getDisplayTerm()));

        fhirProblem.setClinicalStatus("active"); //if we have a Problem record for this condition, this status may be changed
        fhirProblem.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        fhirProblem.setOnset(DateConverter.convertPartialDateToDateTimeType(codedItem.getAssignedDate(), codedItem.getAssignedTime(), codedItem.getDatePart()));

        String text = codedItem.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirProblem.setNotes(text);
        }

        ProblemType problem = codedItem.getProblem();
        String endDateStr = problem.getEndDate();
        if (!Strings.isNullOrEmpty(endDateStr)) {
            fhirProblem.setAbatement(DateConverter.convertPartialDateToDateTimeType(endDateStr, null, problem.getEndDatePart()));

        } else {

            //if there's no end date, the problem may still be ended, which is in the status description
            /*String problemStatus = parser.getProblemStatusDescription();
            if (problemStatus.equalsIgnoreCase("Past Problem")) {
                fhirProblem.setAbatement(new BooleanType(true));
            }*/
        }

        BigInteger duration = problem.getExpectedDuration();
        if (duration != null) {
            int durationInt = duration.intValue();
            if (durationInt > 0) {
                fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_EXPECTED_DURATION, new IntegerType(durationInt)));
            }
        }

        //TODO - finish this!

        /*
        Date lastReviewDate = parser.getLastReviewDate();
        String lastReviewPrecision = parser.getLastReviewDatePrecision();
        DateType lastReviewDateType = EmisDateTimeHelper.createDateType(lastReviewDate, lastReviewPrecision);
        String lastReviewedByGuid = parser.getLastReviewUserInRoleGuid();
        if (lastReviewDateType != null
                || !Strings.isNullOrEmpty(lastReviewedByGuid)) {

            //the review extension is a compound extension, containing who and when
            Extension fhirExtension = ExtensionConverter.createCompoundExtension(FhirExtensionUri.PROBLEM_LAST_REVIEWED);

            if (lastReviewDateType != null) {
                fhirExtension.addExtension(ExtensionConverter.createExtension(FhirExtensionUri._PROBLEM_LAST_REVIEWED__DATE, lastReviewDateType));
            }
            if (!Strings.isNullOrEmpty(lastReviewedByGuid)) {
                fhirExtension.addExtension(ExtensionConverter.createExtension(FhirExtensionUri._PROBLEM_LAST_REVIEWED__PERFORMER, csvHelper.createPractitionerReference(lastReviewedByGuid)));
            }
            fhirProblem.addExtension(fhirExtension);
        }

        ProblemSignificance fhirSignificance = convertSignificance(parser.getSignificanceDescription());
        CodeableConcept fhirConcept = CodeableConceptHelper.createCodeableConcept(fhirSignificance);
        fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_SIGNIFICANCE, fhirConcept));

        String parentProblemGuid = parser.getParentProblemObservationGuid();
        String parentRelationship = parser.getParentProblemRelationship();
        if (!Strings.isNullOrEmpty(parentProblemGuid)) {
            ProblemRelationshipType fhirRelationshipType = convertRelationshipType(parentRelationship);

            //this extension is composed of two separate extensions
            Extension typeExtension = ExtensionConverter.createExtension("type", new StringType(fhirRelationshipType.getCode()));
            Extension referenceExtension = ExtensionConverter.createExtension("target", csvHelper.createProblemReference(parentProblemGuid, patientGuid));
            fhirProblem.addExtension(ExtensionConverter.createCompoundExtension(FhirExtensionUri.PROBLEM_RELATED, typeExtension, referenceExtension));
        }

        //carry over linked items from any previous instance of this problem
        List<Reference> previousReferences = findPreviousLinkedReferences(csvHelper, fhirResourceFiler, fhirProblem.getId());
        if (previousReferences != null && !previousReferences.isEmpty()) {
            csvHelper.addLinkedItemsToProblem(fhirProblem, previousReferences);
        }

        //apply any linked items from this extract
        List<String> linkedResources = csvHelper.getAndRemoveProblemRelationships(observationGuid, patientGuid);
        if (linkedResources != null) {
            List<Reference> references = ReferenceHelper.createReferences(linkedResources);
            csvHelper.addLinkedItemsToProblem(fhirProblem, references);
        }*/

        /**
         protected Byte problemStatus;
         protected Byte groupingStatus;
         protected Byte problemType;
         protected Byte significance;
         protected IdentType parentProblem;
         protected Byte owner;
         protected CareAimListType careAimList;
         protected CarePlanListType carePlanList;
         */

        resources.add(fhirProblem);
    }
}

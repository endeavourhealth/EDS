package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ConsultationTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultationTransformer.class);

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        ConsultationListType consultationList = medicalRecord.getConsultationList();
        if (consultationList == null) {
            return;
        }

        for (ConsultationType consultation : consultationList.getConsultation()) {
            transform(consultation, resources, patientGuid);
        }
    }

    private static void transform(ConsultationType consultation, List<Resource> resources, String patientGuid) throws TransformException {

        Encounter fhirEncounter = new Encounter();
        fhirEncounter.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ENCOUNTER));

        String consultationGuid = consultation.getGUID();
        EmisOpenHelper.setUniqueId(fhirEncounter, patientGuid, consultationGuid);

        fhirEncounter.setPatient(EmisOpenHelper.createPatientReference(patientGuid));
        fhirEncounter.setStatus(Encounter.EncounterState.FINISHED);

        DateTimeType dateTimeType = DateConverter.convertPartialDateToDateTimeType(consultation.getAssignedDate(), null, consultation.getDatePart());
        Period period = new Period();
        period.setStartElement(dateTimeType);

        if (consultation.getDuration() != null) {
            int duration = consultation.getDuration().intValue();
            fhirEncounter.setLength(QuantityHelper.createDuration(new Integer(duration), "minutes"));
        }

        fhirEncounter.setPeriod(period);

        IdentType userId = consultation.getUserID();
        if (userId != null) {
            Reference reference = EmisOpenHelper.createPractitionerReference(userId.getGUID());
            fhirEncounter.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_BY, reference));
        }

        AuthorType author = consultation.getOriginalAuthor();
        if (author != null) {
            IdentType authorId = author.getUser();
            Reference reference = EmisOpenHelper.createPractitionerReference(authorId.getGUID());

            Encounter.EncounterParticipantComponent fhirParticipant = fhirEncounter.addParticipant();
            fhirParticipant.addType(CodeableConceptHelper.createCodeableConcept(EncounterParticipantType.PRIMARY_PERFORMER));
            fhirParticipant.setIndividual(reference);
        }

        IdentType accompanying = consultation.getAccompanyingHCPID();
        if (accompanying != null) {
            Reference reference = EmisOpenHelper.createPractitionerReference(accompanying.getGUID());

            Encounter.EncounterParticipantComponent fhirParticipant = fhirEncounter.addParticipant();
            fhirParticipant.addType(CodeableConceptHelper.createCodeableConcept(EncounterParticipantType.SECONDARY_PERFORMER));
            fhirParticipant.setIndividual(reference);
        }

        //TODO - finish

/**
  protected String externalConsultant;
 protected IdentType locationID;
 protected IdentType locationTypeID;
  protected Byte consultationType;
  protected BigInteger travelTime;
 protected BigInteger appointmentSlotID;
 protected BigInteger dataSource;
 */

        resources.add(fhirEncounter);

        Reference encounterReference = EmisOpenHelper.createEncounterReference(consultationGuid, patientGuid);
        List<Resource> childResources = new ArrayList<>();

        ElementListType elements = consultation.getElementList();
        if (elements != null) {
            for (ElementListType.ConsultationElement element: elements.getConsultationElement()) {

                if (element.getEvent() != null) {
                    EventTransformer.transform(element.getEvent(), childResources, patientGuid);
                }

                if (element.getMedication() != null) {
                    MedicationTransformer.transform(element.getMedication(), childResources, patientGuid);
                }

                if (element.getDiary() != null) {
                    DiaryTransformer.transform(element.getDiary(), childResources, patientGuid);
                }

                if (element.getReferral() != null) {
                    ReferralTransformer.transform(element.getReferral(), childResources, patientGuid);
                }

                if (element.getAllergy() != null) {
                    AllergyTransformer.transform(element.getAllergy(), childResources, patientGuid);
                }

                if (element.getInvestigation() != null) {
                    InvestigationTransformer.transform(element.getInvestigation(), childResources, patientGuid);
                }

                if (element.getTestRequest() != null) {
                    TestRequestHeaderTransformer.transform(element.getTestRequest(), childResources, patientGuid);
                }
            }
        }

        for (Resource childResource: childResources) {

            if (childResource instanceof Observation) {
                ((Observation)childResource).setEncounter(encounterReference);

            } else if (childResource instanceof Condition) {
                ((Condition)childResource).setEncounter(encounterReference);

            } else if (childResource instanceof Procedure) {
                ((Procedure)childResource).setEncounter(encounterReference);

            } else if (childResource instanceof FamilyMemberHistory) {
                ((FamilyMemberHistory)childResource).addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, encounterReference));

            } else if (childResource instanceof Immunization) {
                ((Immunization)childResource).setEncounter(encounterReference);

            } else if (childResource instanceof ProcedureRequest) {
                ((ProcedureRequest)childResource).setEncounter(encounterReference);

            } else if (childResource instanceof ReferralRequest) {
                ((ReferralRequest)childResource).setEncounter(encounterReference);

            } else if (childResource instanceof AllergyIntolerance) {
                ((AllergyIntolerance)childResource).addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, encounterReference));

            } else if (childResource instanceof DiagnosticReport) {
                ((DiagnosticReport)childResource).setEncounter(encounterReference);

            } else if (childResource instanceof DiagnosticOrder) {
                ((DiagnosticOrder)childResource).setEncounter(encounterReference);

            } else if (childResource instanceof MedicationStatement) {
                //TODO - extend MedicationStatement profile to include an Encounter reference
                ((MedicationStatement)childResource).addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, encounterReference));

            } else {
                LOG.warn("Not linking Encounter to " + childResource.getResourceType());
            }

            resources.add(childResource);
        }

    }
}

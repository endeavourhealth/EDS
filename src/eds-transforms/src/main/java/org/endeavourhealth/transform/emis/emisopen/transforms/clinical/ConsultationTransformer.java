package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.*;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ConsultationTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultationTransformer.class);

    private static final String CONTAINED_LIST_ID = "Items";

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        ConsultationListType consultationList = medicalRecord.getConsultationList();
        if (consultationList == null) {
            return;
        }

        for (ConsultationType consultation : consultationList.getConsultation()) {
            transform(consultation, resources, patientGuid, medicalRecord);
        }
    }

    private static void transform(ConsultationType consultation, List<Resource> resources, String patientGuid, MedicalRecordType medicalRecord) throws TransformException {

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
            fhirEncounter.setLength(QuantityHelper.createDuration(Integer.valueOf(duration), "minutes"));
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

        String externalConsultant = consultation.getExternalConsultant();
        if (!Strings.isNullOrEmpty(externalConsultant)) {
            //participants must always be Practitioners, so create a contained Practitioner resource to hold the name
            String externalConsultantId = "Consultant";

            HumanName fhirName = new HumanName();
            fhirName.setText(externalConsultant);

            Practitioner fhirPractitioner = new Practitioner();
            fhirPractitioner.setId(externalConsultantId);
            fhirPractitioner.setName(fhirName);
            fhirEncounter.getContained().add(fhirPractitioner);

            Encounter.EncounterParticipantComponent fhirParticipant = fhirEncounter.addParticipant();
            fhirParticipant.addType(CodeableConceptHelper.createCodeableConcept(EncounterParticipantType.PRIMARY_PERFORMER));
            fhirParticipant.setIndividual(ReferenceHelper.createInternalReference(externalConsultantId));
        }

        IdentType locationId = consultation.getLocationID();
        if (locationId != null) {
            Encounter.EncounterLocationComponent location = fhirEncounter.addLocation();
            location.setLocation(EmisOpenHelper.createLocationReference(locationId.getGUID()));
        }

        IdentType locationType = consultation.getLocationTypeID();
        if (locationType != null) {
            String locationTypeDesc = findLocationType(locationType.getGUID(), medicalRecord);
            if (!Strings.isNullOrEmpty(locationTypeDesc)) {
                //location must always be Location, so create a contained Location resource to hold the type
                String locationResourceId = "LocationType";

                Location fhirLocation = new Location();
                fhirLocation.setId(locationResourceId);
                fhirLocation.setType(CodeableConceptHelper.createCodeableConcept(locationTypeDesc));
                fhirEncounter.getContained().add(fhirLocation);

                Encounter.EncounterLocationComponent location = fhirEncounter.addLocation();
                location.setLocation(ReferenceHelper.createInternalReference(locationResourceId));
            }
        }

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

        if (!childResources.isEmpty()) {

            //add the extension to say we have a contained list of resourecs
            Reference listReference = ReferenceHelper.createInternalReference(CONTAINED_LIST_ID);
            fhirEncounter.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ENCOUNTER_COMPONENTS, listReference));

            List_ list = new List_();
            list.setId(CONTAINED_LIST_ID);
            fhirEncounter.getContained().add(list);

            for (Resource childResource : childResources) {

                //add the child reference to our contained list
                Reference childReference = ReferenceHelper.createReferenceExternal(childResource);
                list.addEntry().setItem(childReference);

                //and set the backwards reference to our encounter in each of the child resources
                if (childResource instanceof Observation) {
                    ((Observation) childResource).setEncounter(encounterReference);

                } else if (childResource instanceof Condition) {
                    ((Condition) childResource).setEncounter(encounterReference);

                } else if (childResource instanceof Procedure) {
                    ((Procedure) childResource).setEncounter(encounterReference);

                } else if (childResource instanceof FamilyMemberHistory) {
                    ((FamilyMemberHistory) childResource).addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, encounterReference));

                } else if (childResource instanceof Immunization) {
                    ((Immunization) childResource).setEncounter(encounterReference);

                } else if (childResource instanceof ProcedureRequest) {
                    ((ProcedureRequest) childResource).setEncounter(encounterReference);

                } else if (childResource instanceof ReferralRequest) {
                    ((ReferralRequest) childResource).setEncounter(encounterReference);

                } else if (childResource instanceof AllergyIntolerance) {
                    ((AllergyIntolerance) childResource).addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, encounterReference));

                } else if (childResource instanceof DiagnosticReport) {
                    ((DiagnosticReport) childResource).setEncounter(encounterReference);

                } else if (childResource instanceof DiagnosticOrder) {
                    ((DiagnosticOrder) childResource).setEncounter(encounterReference);

                } else if (childResource instanceof MedicationStatement) {
                    ((MedicationStatement) childResource).addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, encounterReference));

                } else {
                    LOG.warn("Not linking Encounter to " + childResource.getResourceType());
                }

                resources.add(childResource);
            }
        }

    }

    private static String findLocationType(String guid, MedicalRecordType medicalRecord) {
        if (medicalRecord.getLocationTypeList() == null) {
            return null;
        }

        for (TypeOfLocationType locationType: medicalRecord.getLocationTypeList().getLocationType()) {
            if (locationType.getGUID().equals(guid)) {
                return locationType.getDescription();
            }
        }

        return null;
    }
}

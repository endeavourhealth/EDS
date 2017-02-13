package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.EncounterParticipantType;
import org.hl7.fhir.instance.model.*;

import java.util.List;

public class ConsultationTransformer {

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

        EmisOpenHelper.setUniqueId(fhirEncounter, patientGuid, consultation.getGUID());

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

        //TODO - finish

/**
  protected String externalConsultant;
 protected IdentType locationID;
 protected IdentType locationTypeID;
 protected IdentType accompanyingHCPID;
 protected Byte consultationType;
  protected BigInteger travelTime;
 protected BigInteger appointmentSlotID;
 protected BigInteger dataSource;
 */

        resources.add(fhirEncounter);

        ElementListType elements = consultation.getElementList();
        if (elements != null) {
            for (ElementListType.ConsultationElement element: elements.getConsultationElement()) {

                if (element.getEvent() != null) {
                    Resource resource = EventTransformer.transform(element.getEvent(), patientGuid);
                    if (resource != null) {
                        //link to encounter

                        resources.add(resource);
                    }
                }

                if (element.getMedication() != null) {
                    Resource resource = MedicationTransformer.transform(element.getMedication(), patientGuid);
                    if (resource != null) {
                        //link to encounter

                        resources.add(resource);
                    }
                }

                if (element.getDiary() != null) {
                    Resource resource = DiaryTransformer.transform(element.getDiary(), patientGuid);
                    if (resource != null) {
                        //link to encounter

                        resources.add(resource);
                    }
                    //TODO - diary
                }

                if (element.getReferral() != null) {
                    Resource resource = ReferralTransformer.transform(element.getReferral(), patientGuid);
                    if (resource != null) {
                        //link to encounter

                        resources.add(resource);
                    }
                }

                if (element.getAllergy() != null) {
                    Resource resource = AllergyTransformer.transform(element.getAllergy(), patientGuid);
                    if (resource != null) {
                        //link to encounter

                        resources.add(resource);
                    }
                }

                if (element.getInvestigation() != null) {
                    Resource resource = InvestigationTransformer.transform(element.getInvestigation(), patientGuid);
                    if (resource != null) {
                        //link to encounter

                        resources.add(resource);
                    }
                }

                if (element.getTestRequest() != null) {
                    Resource resource = TestRequestHeaderTransformer.transform(element.getTestRequest(), patientGuid);
                    if (resource != null) {
                        //link to encounter

                        resources.add(resource);
                    }
                }
            }
        }

    }
}

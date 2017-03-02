package org.endeavourhealth.core.fhirStorage.metadata;

import org.endeavourhealth.core.fhirStorage.exceptions.UnprocessableEntityException;
import org.hl7.fhir.instance.model.*;

public class MetadataFactory {
    public static ResourceMetadata createMetadata(Resource resource) throws UnprocessableEntityException {
        switch (resource.getResourceType()) {
            case Patient:
                return new PatientMetadata((Patient)resource);
            case AllergyIntolerance:
                return new AllergyIntoleranceMetadata((AllergyIntolerance)resource);
            case Condition:
                return new ConditionMetadata((Condition)resource);
            case DiagnosticOrder:
                return new DiagnosticOrderMetadata((DiagnosticOrder)resource);
            case DiagnosticReport:
                return new DiagnosticReportMetadata((DiagnosticReport)resource);
            case FamilyMemberHistory:
                return new FamilyMemberHistoryMetadata((FamilyMemberHistory)resource);
            case Immunization:
                return new ImmunizationMetadata((Immunization)resource);
            case Medication:
                return new MedicationMetadata((Medication)resource);
            case MedicationStatement:
                return new MedicationStatementMetadata((MedicationStatement)resource);
            case MedicationOrder:
                return new MedicationOrderMetadata((MedicationOrder)resource);
            case Observation:
                return new ObservationMetadata((Observation)resource);
            case Procedure:
                return new ProcedureMetadata((Procedure)resource);
            case ProcedureRequest:
                return new ProcedureRequestMetadata((ProcedureRequest)resource);
            case ReferralRequest:
                return new ReferralRequestMetadata((ReferralRequest)resource);
            case Specimen:
                return new SpecimenMetadata((Specimen)resource);
            case Location:
                return new LocationMetadata((Location)resource);
            case Organization:
                return new OrganizationMetadata((Organization)resource);
            case Practitioner:
                return new PractitionerMetadata((Practitioner)resource);
            case RelatedPerson:
                return new RelatedPersonMetadata((RelatedPerson)resource);
            case Substance:
                return new SubstanceMetadata((Substance)resource);
            case Composition:
                return new CompositionMetadata((Composition)resource);
            case Appointment:
                return new AppointmentMetadata((Appointment)resource);
            case Encounter:
                return new EncounterMetadata((Encounter)resource);
            case EpisodeOfCare:
                return new EpisodeOfCareMetadata((EpisodeOfCare)resource);
            case Schedule:
                return new ScheduleMetadata((Schedule)resource);
            case Slot:
                return new SlotMetadata((Slot)resource);
            case Order:
                return new OrderMetadata((Order)resource);

            default:
                throw new UnprocessableEntityException("Resource Type not supported:" + resource.getResourceType().toString());
        }
    }
}

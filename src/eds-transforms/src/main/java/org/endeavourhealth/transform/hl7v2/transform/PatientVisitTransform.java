package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.PeriodHelper;
import org.endeavourhealth.transform.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Pl;
import org.endeavourhealth.transform.hl7v2.parser.segments.Pv1Segment;
import org.endeavourhealth.transform.hl7v2.transform.converters.*;
import org.endeavourhealth.transform.hl7v2.transform.converters.ExtensionHelper;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.UUID;

public class PatientVisitTransform {

    public static Encounter fromHl7v2(Pv1Segment source, String sendingFacility) throws ParseException, TransformException {
        Encounter target = new Encounter();

        target.setClass_(convertPatientClass(source.getPatientClass()));

        if (target.getClass_() == Encounter.EncounterClass.OTHER) {
            target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.ENCOUNTER_PATIENT_CLASS, source.getPatientClass()));
        }

        //Current Location
        if (source.getAssignedPatientLocation() != null) {
            target.addLocation()
                    .setStatus(Encounter.EncounterLocationStatus.ACTIVE)
                    .setLocation(getReference(source.getAssignedPatientLocation()));
        }

        if (source.getAdmissionType() != null){
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(source.getAdmissionType()));
        }

        //Prior Location
        if (source.getPriorPatientLocation() != null) {
            target.addLocation()
                    .setStatus(Encounter.EncounterLocationStatus.COMPLETED)
                    .setLocation(getReference(source.getPriorPatientLocation()));
        }

        if (source.getAttendingDoctor() != null) {
            for (Encounter.EncounterParticipantComponent epl : PractitionerTransform.createParticipantComponents(source.getAttendingDoctor()
                    , EncounterParticipantType.PRIMARY_PERFORMER.getDescription()))
                target.addParticipant(epl);
        }

        if (source.getReferringDoctor() != null) {
            for (Encounter.EncounterParticipantComponent epl : PractitionerTransform.createParticipantComponents(source.getReferringDoctor()
                    , EncounterParticipantType.REFERRER.getDescription()))
                target.addParticipant(epl);
        }

        if (source.getConsultingDoctor() != null) {
            for (Encounter.EncounterParticipantComponent epl : PractitionerTransform.createParticipantComponents(source.getConsultingDoctor()
                    , EncounterParticipantType.CONSULTANT.getDescription()))
                target.addParticipant(epl);
        }

        if (StringUtils.isNotBlank(source.getHospitalService()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(source.getHospitalService()));

        //Temporary Location
        if (source.getTemporaryLocation() != null) {
            target.addLocation()
                    .setStatus(Encounter.EncounterLocationStatus.ACTIVE)
                    .setLocation(getReference(source.getTemporaryLocation()));
        }

        if (StringUtils.isNotBlank(source.getAdmitSource())
                || StringUtils.isNotBlank(source.getDischargeDisposition())
                || StringUtils.isNotBlank(source.getDischargedToLocation())) {

            Encounter.EncounterHospitalizationComponent hospitalComponent = new Encounter.EncounterHospitalizationComponent();

            if (StringUtils.isNotBlank(source.getAdmitSource()))
                hospitalComponent.setAdmitSource(CodeableConceptHelper.getCodeableConceptFromString(source.getAdmitSource()));

            if (StringUtils.isNotBlank(source.getDischargeDisposition()))
                hospitalComponent.setDischargeDisposition(CodeableConceptHelper.getCodeableConceptFromString(source.getDischargeDisposition()));

            if (StringUtils.isNotBlank(source.getDischargedToLocation())) {
                String ln = source.getDischargedToLocation();
                Reference reference = new Reference();
                reference.setDisplay(ln).setReference(generateId(ln,ln));

                hospitalComponent.setDestination(reference);
            }

            target.setHospitalization(hospitalComponent);
        }

        if (StringUtils.isNotBlank(source.getAccountStatus())){
            target.setStatus(getState(source.getAccountStatus()));
        }

        if (source.getAdmitDateTime() != null || source.getDischargeDateTime() != null){
            target.setPeriod(PeriodHelper.createPeriod(DateHelper.fromLocalDateTime(source.getAdmitDateTime()),
                    DateHelper.fromLocalDateTime(source.getDischargeDateTime())));
        }

        if (StringUtils.isNotBlank(source.getPatientType()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(source.getPatientType()));


        if (source.getVisitNumber() != null) {
            Identifier identifier = IdentifierConverter.convert(source.getVisitNumber(), sendingFacility);

            if (identifier != null)
                target.addIdentifier(identifier);
        }

        if (source.getOtherHealthcareProvider() != null) {
            for (Encounter.EncounterParticipantComponent epl : PractitionerTransform.createParticipantComponents(source.getOtherHealthcareProvider()
                    , EncounterParticipantType.SECONDARY_PERFORMER.getDescription()))
                target.addParticipant(epl);
        }

        return target;
    }

    private static Encounter.EncounterState getState(String state) throws TransformException  {
        state = state.trim().toUpperCase();

        switch (state) {
            case "CANCELLED": return Encounter.EncounterState.CANCELLED;
            case "DISCHARGED": return Encounter.EncounterState.FINISHED;
            case "PENDING ARRIVAL": return Encounter.EncounterState.PLANNED;
            case "ACTIVE": return Encounter.EncounterState.INPROGRESS;
            case "PREADMIT": return Encounter.EncounterState.ARRIVED;

            default: throw new TransformException(state + " state not recognised");
        }
    }

    private static Reference getReference(Pl location) throws TransformException {
        List<Location> locations = LocationTransform.convert(location);
        Location finalLocation = locations.get(locations.size() - 1);

        return LocationTransform.createReferenceFromLocation(finalLocation);
    }

    private static Encounter.EncounterClass convertPatientClass(String patientClass) throws TransformException {
        patientClass = patientClass.trim().toUpperCase();

        switch (patientClass) {
            case "OUTPATIENT": return Encounter.EncounterClass.OUTPATIENT;
            case "EMERGENCY": return Encounter.EncounterClass.EMERGENCY;
            case "INPATIENT": return Encounter.EncounterClass.INPATIENT;

            //Homerton Specific
            case "RECURRING": return Encounter.EncounterClass.OTHER;
            case "WAIT LIST": return Encounter.EncounterClass.OTHER;
            default: throw new TransformException(patientClass + " patient class not recognised");
        }
    }

    private static String generateId(String uniqueString, String identifierString) {
        return UUID.nameUUIDFromBytes((identifierString + uniqueString).getBytes()).toString();
    }


}

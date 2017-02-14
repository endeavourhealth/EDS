package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Pl;
import org.endeavourhealth.transform.hl7v2.parser.segments.Pv1Segment;
import org.endeavourhealth.transform.hl7v2.transform.converters.*;
import org.endeavourhealth.transform.hl7v2.transform.converters.ExtensionHelper;
import org.hl7.fhir.instance.model.*;

import java.util.List;

public class PatientVisitTransform {

    public static Encounter fromHl7v2(Pv1Segment source) throws ParseException, TransformException {
        Encounter target = new Encounter();

        target.setClass_(convertPatientClass(source.getPatientClass()));

        if (target.getClass_() == Encounter.EncounterClass.OTHER) {
            target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.ENCOUNTER_PATIENT_CLASS, source.getPatientClass()));
        }

        List<Encounter.EncounterLocationComponent> encounterLocationComponentList;

        //Current Location
        //encounterLocationComponentList = LocationConverter.convert(source.getAssignedPatientLocation(), "Current Location");

        //for (Encounter.EncounterLocationComponent elc : encounterLocationComponentList)
        //    target.addLocation(elc);

        if (source.getAdmissionType() != null){
            target.addType(getCodeableConceptFromString(source.getAdmissionType()));
        }

        //Prior Location
        //encounterLocationComponentList = LocationConverter.convert(source.getPriorPatientLocation(), "Prior Location");

        //for (Encounter.EncounterLocationComponent elc : encounterLocationComponentList)
        //    target.addLocation(elc);

        Encounter.EncounterParticipantComponent epl = new Encounter.EncounterParticipantComponent();



        return target;
    }

    private static CodeableConcept getCodeableConceptFromString(String code) throws TransformException {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding();
        codeableConcept.setText(code);

        return codeableConcept;
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




}

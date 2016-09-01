package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.PatientDemographics;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PatientTransformer extends AbstractTransformer {

    public static void transform(Patient fhirPatient,
                                 List<AbstractModel> models,
                                 Map<String, Resource> hsAllResources) throws Exception {

        //we also need the episode of care
        Reference episodeReference = ReferenceHelper.createReference(ResourceType.EpisodeOfCare, fhirPatient.getId());
        EpisodeOfCare fhirEpisode = (EpisodeOfCare)findResource(episodeReference, hsAllResources);

        createPatient(fhirPatient, fhirEpisode, models, hsAllResources);
        createPatientDemographics(fhirPatient, fhirEpisode, models, hsAllResources);
    }


    private static void createPatient(Patient fhirPatient,
                                    EpisodeOfCare fhirEpisode,
                                    List<AbstractModel> models,
                                    Map<String, Resource> hsAllResources) throws Exception {

        org.endeavourhealth.transform.ceg.models.Patient model = new org.endeavourhealth.transform.ceg.models.Patient();


        model.setPatientId(transformPatientId(fhirPatient.getId()));

        Date dob = fhirPatient.getBirthDate();
        model.setDateOfBirth(dob);

        if (fhirPatient.hasDeceasedDateTimeType()) {
            Date dod = transformDate(fhirPatient.getDeceasedDateTimeType());
            Calendar cal = Calendar.getInstance();
            cal.setTime(dod);
            int year = cal.get(Calendar.YEAR);
            model.setYearOfDeath(new Integer(year));
        }

        String gender = fhirPatient.getGender().getDisplay();
        model.setGender(gender);

        if (fhirEpisode != null) {
            Date regDate = transformDate(fhirEpisode.getPeriod());
            model.setDateRegistered(regDate);

            if (fhirEpisode.getPeriod().hasEnd()) {
                Date dedDate = fhirEpisode.getPeriod().getEnd();
                model.setDateRegisteredEnd(dedDate);
            }
        }

        //TODO - implement
        /**
         private Long patientIdPseudo;
         */

        models.add(model);
    }

    private static void createPatientDemographics(Patient fhirPatient,
                                     EpisodeOfCare fhirEpisode,
                                     List<AbstractModel> models,
                                     Map<String, Resource> hsAllResources) throws Exception {

        PatientDemographics model = new PatientDemographics();

        model.setPatientId(transformPatientId(fhirPatient.getId()));

        if (fhirPatient.hasDeceasedDateTimeType()) {
            Date dod = transformDate(fhirPatient.getDeceasedDateTimeType());
            Calendar cal = Calendar.getInstance();
            cal.setTime(dod);
            int year = cal.get(Calendar.YEAR);
            model.setYearOfDeath(new Integer(year));
        }

        String gender = fhirPatient.getGender().getDisplay();
        model.setGender(gender);

        Coding regTypeCoding = (Coding)findExtension(fhirPatient, FhirExtensionUri.PATIENT_REGISTRATION_TYPE);
        if (regTypeCoding != null) {
            String regTypeDesc = regTypeCoding.getDisplay();
            model.setPatientStatus(regTypeDesc);
        }

        if (fhirPatient.hasCareProvider()) {
            Reference practitionerReference = fhirPatient.getCareProvider().get(0);
            Practitioner practitioner = (Practitioner)findResource(practitionerReference, hsAllResources);
            if (practitioner != null) {
                String name = practitioner.getName().getText();
                model.setUsualGpName(name);
            }
        }

        if (fhirEpisode != null) {
            Date regDate = transformDate(fhirEpisode.getPeriod());
            model.setDateRegistered(regDate);

            if (fhirEpisode.getPeriod().hasEnd()) {
                Date dedDate = fhirEpisode.getPeriod().getEnd();
                model.setDateRegisteredEnd(dedDate);
            }
        }

        models.add(model);
    }

}

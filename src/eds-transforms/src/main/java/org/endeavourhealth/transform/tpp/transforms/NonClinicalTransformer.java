package org.endeavourhealth.transform.tpp.transforms;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.tpp.schema.Appointment;
import org.endeavourhealth.transform.tpp.schema.NonClinical;
import org.endeavourhealth.transform.tpp.schema.Task;
import org.endeavourhealth.transform.tpp.schema.Visit;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class NonClinicalTransformer {

    public static void transform(NonClinical tppNonClinical, List<Resource> fhirResources) throws TransformException {

        for (Appointment tppAppointment: tppNonClinical.getAppointment()) {
            AppointmentTransformer.transform(tppAppointment, fhirResources);
        }

        for (Visit tppVisit: tppNonClinical.getVisit()) {
            VisitTransformer.transform(tppVisit, fhirResources);
        }

        for (Task tppTask: tppNonClinical.getTask()) {
            TaskTransformer.transform(tppTask, fhirResources);
        }
    }
}

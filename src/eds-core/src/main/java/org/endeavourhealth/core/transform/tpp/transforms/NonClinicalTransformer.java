package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.tpp.schema.Appointment;
import org.endeavourhealth.core.transform.tpp.schema.NonClinical;
import org.endeavourhealth.core.transform.tpp.schema.Task;
import org.endeavourhealth.core.transform.tpp.schema.Visit;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class NonClinicalTransformer {

    public static void transform(NonClinical tppNonClinical, List<Resource> fhirResources) {

        for (Appointment tppAppointment: tppNonClinical.getAppointment()) {
            AppointmentTransformer.transform(tppAppointment, fhirResources);
        }

        for (Visit tppVisit: tppNonClinical.getVisit()) {

        }

        for (Task tppTask: tppNonClinical.getTask()) {

        }
    }
}

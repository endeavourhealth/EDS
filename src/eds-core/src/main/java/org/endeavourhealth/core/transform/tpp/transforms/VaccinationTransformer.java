package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.tpp.schema.Event;
import org.endeavourhealth.core.transform.tpp.schema.Vaccination;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.MedicationAdministration;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class VaccinationTransformer {

    public static void transform(Event tppEvent, Vaccination tppVaccination, List<Resource> fhirResources) {

     /*   Immunization fhirImmunisation = new Immunization();
     ..set profile
        fhirResources.add(fhirImmunisation);

        fhirImmunisation.set
        fhirImmunisation.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED);*/


    }
}

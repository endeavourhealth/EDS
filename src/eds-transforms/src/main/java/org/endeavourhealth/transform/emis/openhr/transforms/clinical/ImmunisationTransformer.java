package org.endeavourhealth.transform.emis.openhr.transforms.clinical;

import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001HealthDomain;
import org.endeavourhealth.transform.emis.openhr.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.common.DateConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.common.EventEncounterMap;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

public class ImmunisationTransformer implements ClinicalResourceTransformer
{
    public Immunization transform(OpenHR001HealthDomain.Event source, OpenHR001HealthDomain healthDomain, EventEncounterMap eventEncounterMap) throws TransformException
    {
        Immunization target = new Immunization();
        target.setId(source.getId());
        target.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_IMMUNIZATION));

        target.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED.toCode());
        target.setDateElement(DateConverter.convertPartialDateTimeToDateTimeType(source.getEffectiveTime()));
        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, source.getPatient()));
        target.setPerformer(ReferenceHelper.createReference(ResourceType.Practitioner, source.getAuthorisingUserInRole()));
        target.setEncounter(eventEncounterMap.getEncounterReference(source.getId()));
        target.setVaccineCode(CodeConverter.convertCode(source.getCode(), source.getDisplayTerm()));
        return target;
    }
}
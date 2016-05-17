package org.endeavourhealth.transform.emis.openhr.transforms;

import org.endeavourhealth.transform.common.ReferenceHelper;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001HealthDomain;
import org.endeavourhealth.transform.emis.openhr.transforms.converters.CodeConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.converters.DateConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.helpers.EventEncounterMap;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.hl7.fhir.instance.model.*;

public class AllergyTransformer implements ClinicalResourceTransformer
{
    public AllergyIntolerance transform(OpenHR001HealthDomain.Event source, OpenHR001HealthDomain healthDomain, EventEncounterMap eventEncounterMap) throws TransformException
    {
        AllergyIntolerance target = new AllergyIntolerance();
        target.setId(source.getId());
        target.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_ALLERGY_INTOLERANCE));

        target.setStatus(AllergyIntolerance.AllergyIntoleranceStatus.ACTIVE);
        target.setOnsetElement(DateConverter.convertPartialDateTimeToDateTimeType(source.getEffectiveTime()));
        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, source.getPatient()));
        target.setRecorder(ReferenceHelper.createReference(ResourceType.Practitioner, source.getAuthorisingUserInRole()));
        target.setSubstance(CodeConverter.convertCode(source.getCode(), source.getDisplayTerm()));
        return target;
    }
}

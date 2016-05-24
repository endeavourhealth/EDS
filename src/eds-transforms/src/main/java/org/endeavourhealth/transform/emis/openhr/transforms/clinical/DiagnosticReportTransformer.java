package org.endeavourhealth.transform.emis.openhr.transforms.clinical;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001HealthDomain;
import org.endeavourhealth.transform.emis.openhr.transforms.common.EventEncounterMap;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Meta;

public class DiagnosticReportTransformer implements ClinicalResourceTransformer
{
    public DiagnosticReport transform(OpenHR001HealthDomain.Event source, OpenHR001HealthDomain healthDomain, EventEncounterMap eventEncounterMap) throws TransformException
    {
        DiagnosticReport target = new DiagnosticReport();
        target.setId(source.getId());
        target.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_DIAGNOSTIC_REPORT));

        return target;
    }
}
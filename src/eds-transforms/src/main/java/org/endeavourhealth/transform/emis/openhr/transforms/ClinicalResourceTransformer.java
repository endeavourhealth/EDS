package org.endeavourhealth.transform.emis.openhr.transforms;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001HealthDomain;
import org.endeavourhealth.transform.emis.openhr.transforms.helpers.EventEncounterMap;
import org.hl7.fhir.instance.model.Resource;

public interface ClinicalResourceTransformer
{
    Resource transform(OpenHR001HealthDomain.Event source, OpenHR001HealthDomain healthDomain, EventEncounterMap eventEncounterMap) throws TransformException;
}
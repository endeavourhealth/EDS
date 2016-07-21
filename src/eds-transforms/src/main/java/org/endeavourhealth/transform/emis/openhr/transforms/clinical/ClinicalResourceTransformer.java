package org.endeavourhealth.transform.emis.openhr.transforms.clinical;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001HealthDomain;
import org.endeavourhealth.transform.emis.openhr.transforms.common.EventEncounterMap;
import org.hl7.fhir.instance.model.Resource;

public interface ClinicalResourceTransformer
{
    Resource transform(OpenHR001HealthDomain.Event source, OpenHR001HealthDomain healthDomain, EventEncounterMap eventEncounterMap) throws TransformException;
}
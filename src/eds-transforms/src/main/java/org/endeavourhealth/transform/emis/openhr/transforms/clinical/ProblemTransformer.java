package org.endeavourhealth.transform.emis.openhr.transforms.clinical;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001HealthDomain;
import org.endeavourhealth.transform.emis.openhr.transforms.common.EventEncounterMap;
import org.hl7.fhir.instance.model.Condition;

public class ProblemTransformer implements ClinicalResourceTransformer
{
    public Condition transform(OpenHR001HealthDomain.Event source, OpenHR001HealthDomain healthDomain, EventEncounterMap eventEncounterMap) throws TransformException
    {
        Condition target = new Condition();
        target.setId(source.getId());

        return target;
    }
}
package org.endeavourhealth.transform.emis.openhr.transforms.common;

import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Component;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Encounter;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001HealthDomain;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.HashMap;

public class EventEncounterMap extends HashMap<String, OpenHR001Encounter>
{
    private EventEncounterMap()
    {
    }

    public static EventEncounterMap Create(OpenHR001HealthDomain healthDomain) throws TransformException
    {
        EventEncounterMap eventEncounterMap = new EventEncounterMap();

        for (OpenHR001Encounter encounter : healthDomain.getEncounter())
        {
            for (OpenHR001Component component : encounter.getComponent())
            {
                OpenHRHelper.ensureDboNotDelete(component);
                eventEncounterMap.putIfAbsent(component.getEvent(), encounter);
            }
        }

        return eventEncounterMap;
    }

    public void add(String eventId, OpenHR001Encounter encounter)
    {
        putIfAbsent(eventId, encounter);
    }

    public OpenHR001Encounter getEncounterFromEventId(String eventId)
    {
        return get(eventId);
    }

    public Reference getEncounterReference(String eventId) throws TransformException
    {
        OpenHR001Encounter encounter = getEncounterFromEventId(eventId);

        if (encounter == null)
            return null;

        return ReferenceHelper.createReference(ResourceType.Encounter, encounter.getId());
    }
}

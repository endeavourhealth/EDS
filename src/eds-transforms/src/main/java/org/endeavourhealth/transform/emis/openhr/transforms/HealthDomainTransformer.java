package org.endeavourhealth.transform.emis.openhr.transforms;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.DtCodeQualified;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001HealthDomain;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Problem;
import org.endeavourhealth.transform.emis.openhr.schema.VocEventType;
import org.endeavourhealth.transform.emis.openhr.transforms.helpers.EventEncounterMap;
import org.endeavourhealth.transform.emis.openhr.transforms.helpers.OpenHRHelper;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class HealthDomainTransformer
{
    public static List<Resource> transform(OpenHR001HealthDomain openHRHealthDomain) throws TransformException
    {
        List<Resource> resources = transformEvents(openHRHealthDomain);

        ConditionTransformer.buildConditionLinks(resources, openHRHealthDomain.getProblem());

        resources.addAll(EncounterTransformer.transform(openHRHealthDomain, resources));

        return resources;
    }

    private static List<Resource> transformEvents(OpenHR001HealthDomain healthDomain) throws TransformException
    {
        EventEncounterMap eventEncounterMap = EventEncounterMap.Create(healthDomain);

        List<Resource> result = new ArrayList<>();

        for (OpenHR001HealthDomain.Event event : healthDomain.getEvent())
        {
            OpenHRHelper.ensureDboNotDelete(event);

            ClinicalResourceTransformer transformer = getTransformerForEvent(healthDomain, event);
            result.add(transformer.transform(event, healthDomain, eventEncounterMap));
        }

        return result;
    }

    private static ClinicalResourceTransformer getTransformerForEvent(OpenHR001HealthDomain healthDomain, OpenHR001HealthDomain.Event event) throws TransformException
    {
        if (isProblem(healthDomain.getProblem(), event))
            return new ProblemTransformer();

        switch (event.getEventType())
        {
            case OBS:                                            // Observation
            {
                if (isProcedure(event))
                    return new ProcedureTransformer();
                else // if condition
                    return new ConditionTransformer();
                //else
                //    return new ObservationTransformer();
            }
            case VAL:                                            // Value
            case INV:                                            // Investigation
            case ATT: return new ObservationTransformer();       // Attachment
            case DRY: return new ProcedureRequestTransformer();  // Diary
            case ISS: return new MedicationOrderTransformer();   // Medication Issue
            case MED: return new MedicationStatementTransformer();        // Medication
            case TR:  return new DiagnosticOrderTransformer();   // Test Request
            case REF: return new ReferralTransformer();          // Referral
            case ALT: return new AlertTransformer();             // Alert
            case ALL: return new AllergyTransformer();           // Allergy
            case FH:  return new FamilyHistoryTransformer();     // Family history
            case IMM: return new ImmunisationTransformer();      // Immunisation
            case REP: return new DiagnosticReportTransformer();  // Report

            default: throw new TransformException("Event Type not supported: " + event.getEventType().toString());
        }
    }

    public static boolean isProblem(List<OpenHR001Problem> problemList, OpenHR001HealthDomain.Event event) throws TransformException
    {
        // The condition resource specifically excludes AllergyIntolerance as those are handled with their own resource
        return event.getEventType() != VocEventType.ALL
                && problemList != null
                && problemList.stream().anyMatch(p -> p.getId().equals(event.getId()));
    }

    public static boolean isProcedure(OpenHR001HealthDomain.Event event) throws TransformException
    {
        //TODO: This method needs to be replaced with SNOMED hierarchy lookup using CREs

        if (event.getCode() != null)
        {
            if (isProcedureCode(event.getCode()))
                return true;

            if (event.getCode().getTranslation() != null)
            {
                return (event
                        .getCode()
                        .getTranslation()
                        .stream()
                        .anyMatch(t -> isProcedureCode(t)));
            }
        }

        return false;
    }

    public static boolean isProcedureCode(DtCodeQualified code)
    {
        // check for codes in the READ2 code system that fall in the Procedure (7) hierarchy
        return code != null && code.getCodeSystem().equals("2.16.840.1.113883.2.1.6.2") && code.getCode().startsWith("7");
    }
}

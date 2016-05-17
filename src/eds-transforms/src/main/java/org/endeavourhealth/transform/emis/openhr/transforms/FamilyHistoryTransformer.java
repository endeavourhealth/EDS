package org.endeavourhealth.transform.emis.openhr.transforms;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001HealthDomain;
import org.endeavourhealth.transform.emis.openhr.transforms.helpers.EventEncounterMap;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.hl7.fhir.instance.model.FamilyMemberHistory;
import org.hl7.fhir.instance.model.Meta;

public class FamilyHistoryTransformer implements ClinicalResourceTransformer
{
    public FamilyMemberHistory transform(OpenHR001HealthDomain.Event source, OpenHR001HealthDomain healthDomain, EventEncounterMap eventEncounterMap) throws TransformException
    {
        FamilyMemberHistory target = new FamilyMemberHistory();
        target.setId(source.getId());
        target.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_FAMILY_MEMBER_HISTORY));

        return target;
    }
}
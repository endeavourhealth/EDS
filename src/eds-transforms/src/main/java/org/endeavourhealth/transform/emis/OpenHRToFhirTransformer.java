package org.endeavourhealth.transform.emis;

import org.endeavourhealth.common.utility.XmlHelper;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001OpenHealthRecord;
import org.endeavourhealth.transform.emis.openhr.transforms.admin.LocationTransformer;
import org.endeavourhealth.transform.emis.openhr.transforms.admin.OrganisationTransformer;
import org.endeavourhealth.transform.emis.openhr.transforms.admin.PatientTransformer;
import org.endeavourhealth.transform.emis.openhr.transforms.admin.PractitionerTransformer;
import org.endeavourhealth.transform.emis.openhr.transforms.clinical.HealthDomainTransformer;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class OpenHRToFhirTransformer
{
    public List<Resource> toFhirFullRecord(String openHRXml) throws TransformException
    {
        OpenHR001OpenHealthRecord openHR = XmlHelper.deserialize(openHRXml, OpenHR001OpenHealthRecord.class);

        List<Resource> result = new ArrayList<>();

        if (openHR.getAdminDomain() != null)
        {
            result.addAll(OrganisationTransformer.transform(openHR.getAdminDomain().getOrganisation()));
            result.addAll(LocationTransformer.transform(openHR.getAdminDomain()));
            result.addAll(PractitionerTransformer.transform(openHR.getAdminDomain()));
            result.add(PatientTransformer.transform(openHR.getAdminDomain()));
        }

        if (openHR.getHealthDomain() != null)
        {
            result.addAll(HealthDomainTransformer.transform(openHR.getHealthDomain()));
        }

        return result;
    }
}

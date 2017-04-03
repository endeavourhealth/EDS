package org.endeavourhealth.transform.emis.openhr.transforms.admin;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Organisation;
import org.endeavourhealth.transform.emis.openhr.transforms.common.DateConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.common.OpenHRHelper;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class OrganisationTransformer
{
	public static List<Organization> transform(List<OpenHR001Organisation> sources) throws TransformException
    {
        ArrayList<Organization> organizations = new ArrayList<>();

        for (OpenHR001Organisation source: sources)
            organizations.add(transform(source));

        return organizations;
    }

	public static Organization transform(OpenHR001Organisation source) throws TransformException
    {
		OpenHRHelper.ensureDboNotDelete(source);

		Organization target = new Organization();

        target.setId(source.getId());
        target.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ORGANIZATION));

        if (StringUtils.isNotBlank(source.getNationalPracticeCode()))
            target.addIdentifier(new Identifier().setSystem(FhirUri.IDENTIFIER_SYSTEM_ODS_CODE).setValue(source.getNationalPracticeCode()));

        target.setActive(source.getCloseDate() == null);

        if ((source.getOpenDate() != null) || (source.getCloseDate() != null))
        {
            Period period = new Period();

            if (source.getOpenDate() != null)
                period.setStart(DateConverter.toDate(source.getOpenDate()));

            if (source.getCloseDate() != null)
                period.setEnd(DateConverter.toDate(source.getCloseDate()));

            target.getActiveElement().addExtension(new Extension().setUrl(FhirExtensionUri.ACTIVE_PERIOD).setValue(period));
        }

        target.setName(source.getName());
        target.setType(new CodeableConcept().setText(source.getOrganisationType().getDisplayName()));

        if (!StringUtils.isBlank(source.getParentOrganisation()))
            target.setPartOf(ReferenceHelper.createReference(ResourceType.Organization, source.getParentOrganisation()));

        if (!StringUtils.isBlank(source.getMainLocation()))
            target.addExtension(new Extension().setUrl(FhirExtensionUri.ORGANISATION_MAIN_LOCATION).setValue(ReferenceHelper.createReference(ResourceType.Location, source.getMainLocation())));

		return target;
	}
}
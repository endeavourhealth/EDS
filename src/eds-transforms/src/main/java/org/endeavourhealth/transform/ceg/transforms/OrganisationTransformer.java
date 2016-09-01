package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Organisation;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.IdentifierHelper;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.List;

public class OrganisationTransformer extends AbstractTransformer {

    public static void transform(Organization fhir, List<AbstractModel> models) throws Exception {

        Organisation model = new Organisation();

        model.setServiceProviderName(fhir.getName());

        if (fhir.hasIdentifier()) {
            String odsCode = IdentifierHelper.findIdentifierValue(fhir.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);
            model.setServiceProviderCode(odsCode);
        }

        if (fhir.hasPartOf()) {
            String id = ReferenceHelper.getReferenceId(fhir.getPartOf());
            Organization partOfOrg = (Organization)new ResourceRepository().getCurrentVersionAsResource(ResourceType.Organization, id);

            model.setCommissioner(partOfOrg.getName());

            String ccgCode = IdentifierHelper.findIdentifierValue(fhir.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);
            model.setCommissionerCode(ccgCode);
        }

        models.add(model);
    }
}

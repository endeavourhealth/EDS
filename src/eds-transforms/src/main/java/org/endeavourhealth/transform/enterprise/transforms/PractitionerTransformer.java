package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.FhirValueSetUri;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PractitionerTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long enterprisePatientId,
                          Long enterprisePersonId,
                          String configName) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.Practitioner model = data.getPractitioners();

        Long enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.longValue());

        } else {

            Practitioner fhir = (Practitioner)deserialiseResouce(resource);

            long id;
            long organizaationId;
            String name = null;
            String roleCode = null;
            String roleDesc = null;

            id = enterpriseId.longValue();

            if (fhir.hasName()) {
                HumanName fhirName = fhir.getName();
                name = fhirName.getText();
            }

            Long practitionerEnterpriseOrgId = null;
            //LOG.trace("Transforming practitioner " + fhir.getId() + " with " + fhir.getPractitionerRole().size() + " roles and enterpriseOrganisationUuid " + enterpriseOrganisationUuid);
            for (Practitioner.PractitionerPractitionerRoleComponent role : fhir.getPractitionerRole()) {

                CodeableConcept cc = role.getRole();
                for (Coding coding : cc.getCoding()) {
                    if (coding.getSystem().equals(FhirValueSetUri.VALUE_SET_JOB_ROLE_CODES)) {
                        roleCode = coding.getCode();
                        roleDesc = coding.getDisplay();
                    }
                }

                Reference organisationReference = role.getManagingOrganization();
                practitionerEnterpriseOrgId = findEnterpriseId(data.getOrganisations(), organisationReference);
                //LOG.trace("Got role with org ID " + practitionerEnterpriseOrgId + " from " + organisationReference);
            }

            if (practitionerEnterpriseOrgId == null) {
                //LOG.trace("No role, so setting to the enterpriseOrganisationUuid " + enterpriseOrganisationUuid);
                practitionerEnterpriseOrgId = enterpriseOrganisationId;
            }

            organizaationId = practitionerEnterpriseOrgId.longValue();

            model.writeUpsert(id,
                organizaationId,
                name,
                roleCode,
                roleDesc);
        }
    }

}

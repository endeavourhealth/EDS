package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.FhirValueSetUri;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class PractitionerTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerTransformer.class);

    public boolean shouldAlwaysTransform() {
        return false;
    }

    public void transform(Long enterpriseId,
                          Resource resource,
                          OutputContainer data,
                          AbstractEnterpriseCsvWriter csvWriter,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long enterprisePatientId,
                          Long enterprisePersonId,
                          String configName,
                          UUID protocolId) throws Exception {

        Practitioner fhir = (Practitioner)resource;

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
            if (practitionerEnterpriseOrgId == null) {
                practitionerEnterpriseOrgId = transformOnDemand(organisationReference, data, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName, protocolId);
            }
            //LOG.trace("Got role with org ID " + practitionerEnterpriseOrgId + " from " + organisationReference);
        }

        if (practitionerEnterpriseOrgId == null) {
            //LOG.trace("No role, so setting to the enterpriseOrganisationUuid " + enterpriseOrganisationUuid);
            practitionerEnterpriseOrgId = enterpriseOrganisationId;
        }

        organizaationId = practitionerEnterpriseOrgId.longValue();

        /*if (organizaationId != enterpriseOrganisationId.longValue()) {
            return;
        }*/

        org.endeavourhealth.transform.enterprise.outputModels.Practitioner model = (org.endeavourhealth.transform.enterprise.outputModels.Practitioner)csvWriter;
        model.writeUpsert(id,
            organizaationId,
            name,
            roleCode,
            roleDesc);
    }
}

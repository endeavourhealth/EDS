package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.transform.fhir.FhirValueSetUri;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PractitionerTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.Practitioner model = data.getPractitioners();

        Integer enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.intValue());

        } else {

            Practitioner fhir = (Practitioner)deserialiseResouce(resource);

            int id;
            int organizaationId;
            String name = null;
            String roleCode = null;
            String roleDesc = null;

            id = enterpriseId.intValue();

            if (fhir.hasName()) {
                HumanName fhirName = fhir.getName();
                name = fhirName.getText();
            }

            Integer practitionerEnterpriseOrgId = null;

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
            }

            if (practitionerEnterpriseOrgId == null) {
                practitionerEnterpriseOrgId = enterpriseOrganisationUuid;
            }

            organizaationId = practitionerEnterpriseOrgId.intValue();

            model.writeUpsert(id,
                organizaationId,
                name,
                roleCode,
                roleDesc);
        }
    }

    /*public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Practitioner model = new org.endeavourhealth.core.xml.enterprise.Practitioner();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            Practitioner fhir = (Practitioner)deserialiseResouce(resource);

            if (fhir.hasName()) {
                HumanName name = fhir.getName();
                model.setName(name.getText());
            }

            for (Practitioner.PractitionerPractitionerRoleComponent role : fhir.getPractitionerRole()) {

                CodeableConcept cc = role.getRole();
                for (Coding coding: cc.getCoding()) {
                    if (coding.getSystem().equals(FhirValueSetUri.VALUE_SET_JOB_ROLE_CODES)) {
                        model.setRoleCode(coding.getCode());
                        model.setRoleDesc(coding.getDisplay());
                    }
                }

                Reference organisationReference = role.getManagingOrganization();
                Integer enterpriseOrgId = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Organization(), organisationReference);

                if (enterpriseOrgId == null) {
                    LOG.warn("" + fhir.getResourceType() + " " + fhir.getId() + " refers to " + organisationReference.getReference() + " that doesn't exist");
                } else {
                    model.setOrganizationId(enterpriseOrgId);
                }
            }
        }

        //the EMIS test data has practitioners that point to non-exist organisations,
        //so, in order to file them in enterprise, we sub in the main org ID
        if (model.getOrganizationId() == 0) {
            model.setOrganizationId(enterpriseOrganisationUuid);
        }

        data.getPractitioner().add(model);
    }*/
}

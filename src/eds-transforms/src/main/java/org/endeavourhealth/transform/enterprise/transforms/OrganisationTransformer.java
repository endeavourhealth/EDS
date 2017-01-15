package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.ResourceNotFoundException;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.transform.EnterpriseIdMapRepository;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.Organization;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.FhirValueSetUri;
import org.endeavourhealth.transform.fhir.IdentifierHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class OrganisationTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationTransformer.class);

    private String orgOdsCode = null;

    public OrganisationTransformer(String orgOdsCode) {
        this.orgOdsCode = orgOdsCode;
    }

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        Organization model = new Organization();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            org.hl7.fhir.instance.model.Organization fhir = (org.hl7.fhir.instance.model.Organization)deserialiseResouce(resource);

            if (fhir.hasIdentifier()) {
                String odsCode = IdentifierHelper.findIdentifierValue(fhir.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);
                model.setOdsCode(odsCode);
            }

            //LOG.info("Org ID " + model.getOdsCode());

            //if the organisation ODS code matches the one we're filing data for, replace the ID with the ID
            //we've pre-generated to use as our org ID
            if (model.getOdsCode() != null
                && model.getOdsCode().equalsIgnoreCase(orgOdsCode)) {

                new EnterpriseIdMapRepository().saveEnterpriseOrganisationIdMapping(orgOdsCode, new Integer(model.getId()));
            }

            model.setName(fhir.getName());

            if (fhir.hasPartOf()) {

                //the EMIS test pack has organisations that refer to parent orgs that don't exist, so
                //this is wrapped in a try/catch to handle that failure to find the parent
                try {
                    Reference partOfReference = fhir.getPartOf();
                    org.hl7.fhir.instance.model.Organization partOfOrganisation = (org.hl7.fhir.instance.model.Organization)findResource(partOfReference, otherResources);

                    if (partOfOrganisation != null) {
                        Integer partOfEnterpriseId = findEnterpriseId(partOfOrganisation);

                        //because we can't guarantee what order we'll process organisations in, we may process
                        //child orgs before we process parent ones, in which case we won't have allocated
                        //an Enterprise ID for them yet. If that happens, simply assume the Enterprise ID now.
                        if (partOfEnterpriseId == null) {
                            String partOfType = partOfOrganisation.getResourceType().toString();
                            UUID partOfId = UUID.fromString(partOfReference.getId());
                            partOfEnterpriseId = createEnterpriseId(partOfType, partOfId);
                        }

                        model.setParentOrganizationId(partOfEnterpriseId);
                    }

                } catch (Exception ex) {
                    //ignore any failure
                }
            }

            if (fhir.hasType()) {
                CodeableConcept cc = fhir.getType();
                for (Coding coding: cc.getCoding()) {
                    if (coding.getSystem().equals(FhirValueSetUri.VALUE_SET_ORGANISATION_TYPE)) {

                        model.setTypeCode(coding.getCode());
                        model.setTypeDesc(coding.getDisplay());

                    }
                }
            }

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {

                    if (extension.getUrl().equals(FhirExtensionUri.ORGANISATION_MAIN_LOCATION)) {

                        Reference locationReference = (Reference)extension.getValue();

                        try {
                            Location location = (Location) findResource(locationReference, otherResources);
                            if (location != null
                                    && location.hasAddress()) {
                                Address address = location.getAddress();
                                if (address.hasPostalCode()) {
                                    model.setPostcode(address.getPostalCode());
                                }
                            }
                        } catch (ResourceNotFoundException ex) {
                            //The Emis data contains organisations that refer to organisations that don't exist
                            LOG.warn("" + fhir.getResourceType() + " " + fhir.getId() + " refers to " + locationReference.getReference() + " that doesn't exist");
                        }
                    }

                }
            }
        }

        data.getOrganization().add(model);
    }
}

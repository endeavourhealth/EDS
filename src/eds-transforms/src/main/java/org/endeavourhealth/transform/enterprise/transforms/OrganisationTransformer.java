package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.ResourceNotFoundException;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.transform.EnterpriseIdMapRepository;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.FhirValueSetUri;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class OrganisationTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationTransformer.class);

    private String extractOrgOdsCode = null;

    public OrganisationTransformer(String extractOrgOdsCode) {
        this.extractOrgOdsCode = extractOrgOdsCode;
    }

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.Organization model = data.getOrganisations();

        Integer enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.intValue());

        } else {

            org.hl7.fhir.instance.model.Organization fhir = (org.hl7.fhir.instance.model.Organization)deserialiseResouce(resource);

            int id;
            String odsCode = null;
            String name = null;
            String typeCode = null;
            String typeDesc = null;
            String postcode = null;
            Integer parentOrganisationId = null;

            id = enterpriseId.intValue();
            
            if (fhir.hasIdentifier()) {
                odsCode = IdentifierHelper.findIdentifierValue(fhir.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);
            }

            //if the organisation ODS code matches the one we're filing data for, replace the ID with the ID
            //we've pre-generated to use as our org ID
            if (odsCode != null
                    && odsCode.equalsIgnoreCase(extractOrgOdsCode)) {
                new EnterpriseIdMapRepository().saveEnterpriseOrganisationIdMapping(extractOrgOdsCode, enterpriseId);
            }

            //we have at least one Emis org without a name, which is against their spec, but we need to handle it
            if (fhir.hasName()) {
                name = fhir.getName();
            } else {
                name = "";
            }
            //name = fhir.getName();

            if (fhir.hasPartOf()) {

                //the EMIS test pack has organisations that refer to parent orgs that don't exist, so
                //this is wrapped in a try/catch to handle that failure to find the parent
                try {
                    Reference partOfReference = fhir.getPartOf();
                    org.hl7.fhir.instance.model.Organization partOfOrganisation = (org.hl7.fhir.instance.model.Organization)findResource(partOfReference, otherResources);

                    if (partOfOrganisation != null) {
                        parentOrganisationId = findEnterpriseId(data.getOrganisations(), partOfOrganisation);

                        //because we can't guarantee what order we'll process organisations in, we may process
                        //child orgs before we process parent ones, in which case we won't have allocated
                        //an Enterprise ID for them yet. If that happens, simply assign the Enterprise ID now.
                        if (parentOrganisationId == null) {
                            String partOfType = partOfOrganisation.getResourceType().toString();
                            UUID partOfId = UUID.fromString(partOfReference.getId());
                            parentOrganisationId = createEnterpriseId(data.getOrganisations(), partOfType, partOfId);
                        }
                    }

                } catch (Exception ex) {
                    //ignore any failure
                }
            }

            if (fhir.hasType()) {
                CodeableConcept cc = fhir.getType();
                for (Coding coding: cc.getCoding()) {
                    if (coding.getSystem().equals(FhirValueSetUri.VALUE_SET_ORGANISATION_TYPE)) {

                        typeCode = coding.getCode();
                        typeDesc = coding.getDisplay();

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
                                    postcode = address.getPostalCode();
                                }
                            }
                        } catch (ResourceNotFoundException ex) {
                            //The Emis data contains organisations that refer to organisations that don't exist
                            LOG.warn("" + fhir.getResourceType() + " " + fhir.getId() + " refers to " + locationReference.getReference() + " that doesn't exist");
                        }
                    }

                }
            }

            model.writeUpsert(id,
                odsCode,
                name,
                typeCode,
                typeDesc,
                postcode,
                parentOrganisationId);
        }

    }

    /*public void transform(ResourceByExchangeBatch resource,
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
                        Integer partOfEnterpriseId = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Organization(), partOfOrganisation);

                        //because we can't guarantee what order we'll process organisations in, we may process
                        //child orgs before we process parent ones, in which case we won't have allocated
                        //an Enterprise ID for them yet. If that happens, simply assume the Enterprise ID now.
                        if (partOfEnterpriseId == null) {
                            String partOfType = partOfOrganisation.getResourceType().toString();
                            UUID partOfId = UUID.fromString(partOfReference.getId());
                            partOfEnterpriseId = createEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Organization(), partOfType, partOfId);
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
    }*/
}

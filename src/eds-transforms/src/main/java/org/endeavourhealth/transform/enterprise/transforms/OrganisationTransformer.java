package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.FhirValueSetUri;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.endeavourhealth.core.data.ehr.ResourceNotFoundException;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class OrganisationTransformer extends AbstractTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationTransformer.class);

    /*private String extractOrgOdsCode = null;

    public OrganisationTransformer(String extractOrgOdsCode) {
        this.extractOrgOdsCode = extractOrgOdsCode;
    }*/

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          AbstractEnterpriseCsvWriter csvWriter,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long enterprisePatientId,
                          Long enterprisePersonId,
                          String configName) throws Exception {

        Long enterpriseId = mapId(resource, csvWriter, false); //don't assign IDs automatically for every org
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            csvWriter.writeDelete(enterpriseId.longValue());

        } else {

            Resource fhir = deserialiseResouce(resource);
            transform(enterpriseId, fhir, data, csvWriter, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        }
    }

    public void transform(Long enterpriseId,
                          Resource resource,
                          OutputContainer data,
                          AbstractEnterpriseCsvWriter csvWriter,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long enterprisePatientId,
                          Long enterprisePersonId,
                          String configName) throws Exception {

        org.hl7.fhir.instance.model.Organization fhir = (org.hl7.fhir.instance.model.Organization)resource;

        long id;
        String odsCode = null;
        String name = null;
        String typeCode = null;
        String typeDesc = null;
        String postcode = null;
        Long parentOrganisationId = null;

        id = enterpriseId.longValue();

        if (fhir.hasIdentifier()) {
            odsCode = IdentifierHelper.findIdentifierValue(fhir.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);
        }

        //if the organisation ODS code matches the one we're filing data for, replace the ID with the ID
        //we've pre-generated to use as our org ID
        /*if (odsCode != null
                && odsCode.equalsIgnoreCase(extractOrgOdsCode)) {
            EnterpriseIdHelper.saveEnterpriseOrganisationId(extractOrgOdsCode, enterpriseId);
        }*/

        //we have at least one Emis org without a name, which is against their spec, but we need to handle it
        if (fhir.hasName()) {
            name = fhir.getName();
        } else {
            name = "";
        }
        //name = fhir.getName();

        if (fhir.hasPartOf()) {
            Reference partOfReference = fhir.getPartOf();
            parentOrganisationId = findEnterpriseId(data.getOrganisations(), partOfReference);
            if (parentOrganisationId == null) {
                parentOrganisationId = transformOnDemand(partOfReference, data, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
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
                        Location location = (Location)findResource(locationReference, otherResources);
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

        org.endeavourhealth.transform.enterprise.outputModels.Organization model = (org.endeavourhealth.transform.enterprise.outputModels.Organization)csvWriter;
        model.writeUpsert(id,
            odsCode,
            name,
            typeCode,
            typeDesc,
            postcode,
            parentOrganisationId);
    }

}

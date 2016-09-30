package org.endeavourhealth.transform.enterprise.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.ResourceNotFoundException;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Map;
import java.util.UUID;

public class ReferralRequestTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.ReferralRequest model = new org.endeavourhealth.core.xml.enterprise.ReferralRequest();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            ReferralRequest fhir = (ReferralRequest)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getPatient();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasEncounter()) {
                Reference encounterReference = (Reference)fhir.getEncounter();
                UUID enterpriseEncounterUuid = findEnterpriseUuid(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid.toString());
            }

            if (fhir.hasRequester()) {
                Reference practitionerReference = fhir.getRequester();
                UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid.toString());
            }

            if (fhir.hasDateElement()) {
                DateTimeType dt = fhir.getDateElement();
                model.setDate(convertDate(dt.getValue()));
                model.setDatePrecision(convertDatePrecision(dt.getPrecision()));
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getType());
            model.setSnomedConceptId(snomedConceptId);

            if (fhir.hasRecipient()) {
                if (fhir.getRecipient().size() > 1) {
                    throw new TransformException("Cannot handle referral requests with more than one recipient " + fhir.getId());
                }
                Reference recipientReference = fhir.getRecipient().get(0);
                ResourceType resourceType = ReferenceHelper.getResourceType(recipientReference);
                if (resourceType == ResourceType.Organization) {

                    //the EMIS test pack contains referrals that point to recipient organisations that don't exist,
                    //so we need to handle the failure to find the organisation
                    try {
                        Organization organization = (Organization) findResource(recipientReference, otherResources);
                        String name = organization.getName();
                        String odsCode = null;
                        for (Identifier identifier : organization.getIdentifier()) {
                            if (identifier.getSystem().equals(FhirUri.IDENTIFIER_SYSTEM_ODS_CODE)) {
                                odsCode = identifier.getValue();
                            }
                        }

                        model.setRecipientOrganisationName(name);
                        model.setRecipientOrganisationOdsCode(odsCode);
                    } catch (ResourceNotFoundException ex) {
                        //ignore
                    }

                }
            }

            if (fhir.hasPriority()) {
                CodeableConcept codeableConcept = fhir.getPriority();
                if (!Strings.isNullOrEmpty(codeableConcept.getText())) {
                    model.setPriority(codeableConcept.getText());
                } else {
                    for (Coding coding: codeableConcept.getCoding()) {
                        model.setPriority(coding.getDisplay());
                    }
                }
            }

            if (fhir.hasServiceRequested()) {
                for (CodeableConcept codeableConcept: fhir.getServiceRequested()) {
                    if (!Strings.isNullOrEmpty(codeableConcept.getText())) {
                        model.setServiceRequested(codeableConcept.getText());
                    } else {
                        for (Coding coding: codeableConcept.getCoding()) {
                            model.setServiceRequested(coding.getDisplay());
                        }
                    }
                }
            }

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.REFERRAL_REQUEST_SEND_MODE)) {
                        CodeableConcept cc = (CodeableConcept)extension.getValue();
                        if (!Strings.isNullOrEmpty(cc.getText())) {
                            model.setMode(cc.getText());
                        } else {
                            Coding coding = cc.getCoding().get(0);
                            model.setMode(coding.getDisplay());
                        }
                    }
                }
            }
        }

        data.getReferralRequest().add(model);
    }


}


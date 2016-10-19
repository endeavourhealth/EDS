package org.endeavourhealth.transform.enterprise.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Map;

public class ReferralRequestTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.ReferralRequest model = new org.endeavourhealth.core.xml.enterprise.ReferralRequest();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            ReferralRequest fhir = (ReferralRequest)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(patientReference);
            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasEncounter()) {
                Reference encounterReference = (Reference)fhir.getEncounter();
                Integer enterpriseEncounterUuid = findEnterpriseId(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid);
            }

            if (fhir.hasRequester()) {
                Reference practitionerReference = fhir.getRequester();
                Integer enterprisePractitionerUuid = findEnterpriseId(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid);
            }

            if (fhir.hasDateElement()) {
                DateTimeType dt = fhir.getDateElement();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getType());
            model.setSnomedConceptId(snomedConceptId);

            if (fhir.hasRequester()) {
                Reference requesterReference = fhir.getRequester();
                ResourceType resourceType = ReferenceHelper.getResourceType(requesterReference);

                //the requester can be an organisation or practitioner
                if (resourceType == ResourceType.Organization) {

                    Integer enterpriseId = findEnterpriseId(requesterReference);
                    model.setRecipientOrganizationId(enterpriseId);

                } else if (resourceType == ResourceType.Practitioner) {

                    Practitioner fhirPractitioner = (Practitioner)findResource(requesterReference, otherResources);
                    Practitioner.PractitionerPractitionerRoleComponent role = fhirPractitioner.getPractitionerRole().get(0);
                    Reference organisationReference = role.getManagingOrganization();
                    Integer enterpriseId = findEnterpriseId(organisationReference);
                    if (enterpriseId != null) {
                        model.setRecipientOrganizationId(enterpriseId);
                    }
                }
            }

            if (fhir.hasRecipient()) {
                if (fhir.getRecipient().size() > 1) {
                    throw new TransformException("Cannot handle referral requests with more than one recipient " + fhir.getId());
                }
                Reference recipientReference = fhir.getRecipient().get(0);
                ResourceType resourceType = ReferenceHelper.getResourceType(recipientReference);

                //the recipient can be an organisation or practitioner
                if (resourceType == ResourceType.Organization) {

                    //the EMIS test pack contains referrals that point to recipient organisations that don't exist,
                    //so we need to handle the failure to find the organisation
                    Integer enterpriseId = findEnterpriseId(recipientReference);
                    if (enterpriseId != null) {
                        model.setRecipientOrganizationId(enterpriseId);
                    }
                } else if (resourceType == ResourceType.Practitioner) {

                    Practitioner fhirPractitioner = (Practitioner)findResource(recipientReference, otherResources);
                    Practitioner.PractitionerPractitionerRoleComponent role = fhirPractitioner.getPractitionerRole().get(0);
                    Reference organisationReference = role.getManagingOrganization();
                    Integer enterpriseId = findEnterpriseId(organisationReference);
                    if (enterpriseId != null) {
                        model.setRecipientOrganizationId(enterpriseId);
                    }
                }
            }

            //base the outgoing flag simply on whether the recipient ID matches the owning ID
            boolean outgoing = model.getRecipientOrganizationId() != model.getOrganizationId();
            model.setOutgoingReferral(outgoing);

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


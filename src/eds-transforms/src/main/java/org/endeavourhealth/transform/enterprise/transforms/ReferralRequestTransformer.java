package org.endeavourhealth.transform.enterprise.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.ResourceNotFoundException;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.ReferralPriority;
import org.endeavourhealth.common.fhir.schema.ReferralType;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class ReferralRequestTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(ReferralRequestTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.ReferralRequest model = data.getReferralRequests();

        Integer enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;
            
        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.intValue());
            
        } else {

            ReferralRequest fhir = (ReferralRequest)deserialiseResouce(resource);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(data.getPatients(), patientReference);

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientUuid == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            int id;
            int organizationId;
            int patientId;
            Integer encounterId = null;
            Integer practitionerId = null;
            Date clinicalEffectiveDate = null;
            Integer datePrecisionId = null;
            Long snomedConceptId = null;
            Integer requesterOrganizationId = null;
            Integer recipientOrganizationId = null;
            Integer priorityId = null;
            Integer typeId = null;
            String mode = null;
            Boolean outgoing = null;
            String originalCode = null;
            String originalTerm = null;

            id = enterpriseId.intValue();
            organizationId = enterpriseOrganisationUuid.intValue();
            patientId = enterprisePatientUuid.intValue();

            if (fhir.hasEncounter()) {
                Reference encounterReference = (Reference)fhir.getEncounter();
                encounterId = findEnterpriseId(data.getEncounters(), encounterReference);
            }

            if (fhir.hasRequester()) {
                Reference practitionerReference = fhir.getRequester();
                practitionerId = findEnterpriseId(data.getPractitioners(), practitionerReference);
            }

            if (fhir.hasDateElement()) {
                DateTimeType dt = fhir.getDateElement();
                clinicalEffectiveDate = dt.getValue();
                datePrecisionId = convertDatePrecision(dt.getPrecision());
            }

            //changed where the observation code is stored
            if (fhir.hasServiceRequested()) {
                if (fhir.getServiceRequested().size() > 1) {
                    throw new TransformException("Transform doesn't support referrals with multiple service codes " + fhir.getId());
                }
                CodeableConcept fhirServiceRequested = fhir.getServiceRequested().get(0);
                snomedConceptId = CodeableConceptHelper.findSnomedConceptId(fhirServiceRequested);

                //add the raw original code, to assist in data checking
                originalCode = CodeableConceptHelper.findOriginalCode(fhirServiceRequested);

                //add original term too, for easy display of results
                originalTerm = fhirServiceRequested.getText();
            }
            /*Long snomedConceptId = findSnomedConceptId(fhir.getType());
            model.setSnomedConceptId(snomedConceptId);*/

            if (fhir.hasRequester()) {
                Reference requesterReference = fhir.getRequester();
                ResourceType resourceType = ReferenceHelper.getResourceType(requesterReference);

                //the requester can be an organisation or practitioner
                if (resourceType == ResourceType.Organization) {
                    requesterOrganizationId = findEnterpriseId(data.getOrganisations(), requesterReference);

                } else if (resourceType == ResourceType.Practitioner) {
                    requesterOrganizationId = findOrganisationEnterpriseIdFromPractictioner(requesterReference, data, otherResources, fhir);
                }
            }

            if (fhir.hasRecipient()) {

                //there may be two recipients, one for the organisation and one for the practitioner
                for (Reference recipientReference: fhir.getRecipient()) {
                    ResourceType resourceType = ReferenceHelper.getResourceType(recipientReference);
                    if (resourceType == ResourceType.Organization) {
                        //the EMIS test pack contains referrals that point to recipient organisations that don't exist,
                        //so we need to handle the failure to find the organisation
                        recipientOrganizationId = findEnterpriseId(data.getOrganisations(), recipientReference);
                    }
                }

                //if we didn't find an organisation reference, look for a practitioner one
                if (recipientOrganizationId == null) {
                    for (Reference recipientReference : fhir.getRecipient()) {
                        ResourceType resourceType = ReferenceHelper.getResourceType(recipientReference);

                        if (resourceType == ResourceType.Practitioner) {
                            recipientOrganizationId = findOrganisationEnterpriseIdFromPractictioner(recipientReference, data, otherResources, fhir);
                        }
                    }
                }
            }

            //base the outgoing flag simply on whether the recipient ID matches the owning ID
            if (requesterOrganizationId != null) {
                outgoing = requesterOrganizationId.intValue() == organizationId;
            }

            if (fhir.hasPriority()) {
                CodeableConcept codeableConcept = fhir.getPriority();
                if (codeableConcept.hasCoding()) {
                    Coding coding = codeableConcept.getCoding().get(0);
                    ReferralPriority fhirReferralPriority = ReferralPriority.fromCode(coding.getCode());
                    priorityId = fhirReferralPriority.ordinal();
                }
            }

            if (fhir.hasType()) {
                CodeableConcept codeableConcept = fhir.getType();
                if (codeableConcept.hasCoding()) {
                    Coding coding = codeableConcept.getCoding().get(0);
                    ReferralType fhirReferralType = ReferralType.fromCode(coding.getCode());
                    typeId = fhirReferralType.ordinal();
                }
            }

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.REFERRAL_REQUEST_SEND_MODE)) {
                        CodeableConcept cc = (CodeableConcept)extension.getValue();
                        if (!Strings.isNullOrEmpty(cc.getText())) {
                            mode = cc.getText();
                        } else {
                            Coding coding = cc.getCoding().get(0);
                            mode = coding.getDisplay();
                        }
                    }
                }
            }

            model.writeUpsert(id, 
                organizationId,
                patientId,
                encounterId,
                practitionerId,
                clinicalEffectiveDate,
                datePrecisionId,
                snomedConceptId,
                requesterOrganizationId,
                recipientOrganizationId,
                priorityId,
                typeId,
                mode,
                outgoing,
                originalCode,
                originalTerm);
        }
    }

    private Integer findOrganisationEnterpriseIdFromPractictioner(Reference practitionerReference,
                                                                  OutputContainer data,
                                                                  Map<String, ResourceByExchangeBatch> otherResources,
                                                                  ReferralRequest fhir) throws Exception {

        try {
            Practitioner fhirPractitioner = (Practitioner)findResource(practitionerReference, otherResources);
            Practitioner.PractitionerPractitionerRoleComponent role = fhirPractitioner.getPractitionerRole().get(0);
            Reference organisationReference = role.getManagingOrganization();
            return findEnterpriseId(data.getOrganisations(), organisationReference);

        } catch (ResourceNotFoundException ex) {
            //we have a number of examples of Emis data where the practitioner doesn't exist, so handle this not being found
            LOG.warn("" + fhir.getResourceType() + " " + fhir.getId() + " refers to a Practitioner that doesn't exist");
            return null;
        }
    }


    /*public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.ReferralRequest model = new org.endeavourhealth.core.xml.enterprise.ReferralRequest();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            ReferralRequest fhir = (ReferralRequest)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Patient(), patientReference);

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientUuid == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasEncounter()) {
                Reference encounterReference = (Reference)fhir.getEncounter();
                Integer enterpriseEncounterUuid = findEnterpriseId(new Encounter(), encounterReference);
                model.setEncounterId(enterpriseEncounterUuid);
            }

            if (fhir.hasRequester()) {
                Reference practitionerReference = fhir.getRequester();
                Integer enterprisePractitionerUuid = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Practitioner(), practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid);
            }

            if (fhir.hasDateElement()) {
                DateTimeType dt = fhir.getDateElement();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
            }

            //changed where the observation code is stored
            if (fhir.hasServiceRequested()) {
                if (fhir.getServiceRequested().size() > 1) {
                    throw new TransformException("Transform doesn't support referrals with multiple service codes " + fhir.getId());
                }
                CodeableConcept fhirServiceRequested = fhir.getServiceRequested().get(0);
                Long snomedConceptId = findSnomedConceptId(fhirServiceRequested);
                model.setSnomedConceptId(snomedConceptId);


                //add the raw original code, to assist in data checking
                String originalCode = findOriginalCode(fhirServiceRequested);
                model.setOriginalCode(originalCode);

                //add original term too, for easy display of results
                String originalTerm = fhirServiceRequested.getText();
                model.setOriginalTerm(originalTerm);

            }
            *//*Long snomedConceptId = findSnomedConceptId(fhir.getType());
            model.setSnomedConceptId(snomedConceptId);*//*

            if (fhir.hasRequester()) {
                Reference requesterReference = fhir.getRequester();
                ResourceType resourceType = ReferenceHelper.getResourceType(requesterReference);

                //the requester can be an organisation or practitioner
                if (resourceType == ResourceType.Organization) {

                    Integer enterpriseId = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Organization(), requesterReference);
                    model.setRequesterOrganizationId(enterpriseId);

                } else if (resourceType == ResourceType.Practitioner) {

                    Practitioner fhirPractitioner = (Practitioner)findResource(requesterReference, otherResources);
                    Practitioner.PractitionerPractitionerRoleComponent role = fhirPractitioner.getPractitionerRole().get(0);
                    Reference organisationReference = role.getManagingOrganization();
                    Integer enterpriseId = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Organization(), organisationReference);
                    if (enterpriseId != null) {
                        model.setRequesterOrganizationId(enterpriseId);
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
                    Integer enterpriseId = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Organization(), recipientReference);
                    if (enterpriseId != null) {
                        model.setRecipientOrganizationId(enterpriseId);
                    }
                } else if (resourceType == ResourceType.Practitioner) {

                    Practitioner fhirPractitioner = (Practitioner)findResource(recipientReference, otherResources);
                    Practitioner.PractitionerPractitionerRoleComponent role = fhirPractitioner.getPractitionerRole().get(0);
                    Reference organisationReference = role.getManagingOrganization();
                    Integer enterpriseId = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Organization(), organisationReference);
                    if (enterpriseId != null) {
                        model.setRecipientOrganizationId(enterpriseId);
                    }
                }
            }

            //base the outgoing flag simply on whether the recipient ID matches the owning ID
            boolean outgoing = model.getRequesterOrganizationId() == model.getOrganizationId();
            model.setOutgoingReferral(outgoing);

            if (fhir.hasPriority()) {
                CodeableConcept codeableConcept = fhir.getPriority();
                if (codeableConcept.hasCoding()) {
                    Coding coding = (Coding)codeableConcept.getCoding().get(0);
                    ReferralPriority fhirReferralPriority = ReferralPriority.fromCode(coding.getCode());
                    model.setPriorityId(fhirReferralPriority.ordinal());
                }
            }

            if (fhir.hasType()) {
                CodeableConcept codeableConcept = fhir.getType();
                if (codeableConcept.hasCoding()) {
                    Coding coding = (Coding)codeableConcept.getCoding().get(0);
                    ReferralType fhirReferralType = ReferralType.fromCode(coding.getCode());
                    model.setTypeId(fhirReferralType.ordinal());
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
*/

}


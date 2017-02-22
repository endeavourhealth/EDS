package org.endeavourhealth.transform.enterprise.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.schema.MedicationAuthorisationType;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class MedicationStatementTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(MedicationStatementTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.MedicationStatement model = data.getMedicationStatements();

        Integer enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.intValue());

        } else {

            MedicationStatement fhir = (MedicationStatement)deserialiseResouce(resource);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(data.getPatients(), patientReference);

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientUuid == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            int id;
            int organisationId;
            int patientId;
            Integer encounterId = null;
            Integer practitionerId = null;
            Date clinicalEffectiveDate = null;
            Integer datePrecisionId = null;
            Long dmdId = null;
            Boolean isActive = null;
            Date cancellationDate = null;
            String dose = null;
            BigDecimal quantityValue = null;
            String quantityUnit = null;
            int authorisationTypeId;
            String originalTerm = null;

            id = enterpriseId.intValue();
            organisationId = enterpriseOrganisationUuid.intValue();
            patientId = enterprisePatientUuid.intValue();

            if (fhir.hasInformationSource()) {
                Reference practitionerReference = fhir.getInformationSource();
                practitionerId = findEnterpriseId(data.getPractitioners(), practitionerReference);
            }

            if (fhir.hasDateAssertedElement()) {
                DateTimeType dt = fhir.getDateAssertedElement();
                clinicalEffectiveDate = dt.getValue();
                datePrecisionId = convertDatePrecision(dt.getPrecision());
            }

            dmdId = CodeableConceptHelper.findSnomedConceptId(fhir.getMedicationCodeableConcept());

            //add term too, for easy display of results
            originalTerm = fhir.getMedicationCodeableConcept().getText();
            //if we failed to find one, it's because of a change in how the CodeableConcept was generated, so find the term differently
            if (Strings.isNullOrEmpty(originalTerm)) {
                originalTerm = CodeableConceptHelper.findSnomedConceptText(fhir.getMedicationCodeableConcept());
            }

            if (fhir.hasStatus()) {
                MedicationStatement.MedicationStatementStatus fhirStatus = fhir.getStatus();
                isActive = new Boolean(fhirStatus == MedicationStatement.MedicationStatementStatus.ACTIVE);
            }

            if (fhir.hasDosage()) {
                if (fhir.getDosage().size() > 1) {
                    throw new TransformException("Cannot support MedicationStatements with more than one dose " + fhir.getId());
                }

                MedicationStatement.MedicationStatementDosageComponent doseage = fhir.getDosage().get(0);
                dose = doseage.getText();

                //one of the Emis test packs includes the unicode \u0001 character in the dose. This should be handled
                //during the inbound transform, but the data is already in the DB now, so needs handling here
                char[] chars = dose.toCharArray();
                for (int i=0; i<chars.length; i++) {
                    char c = chars[i];
                    if (c == 1) {
                        chars[i] = '?'; //just replace with ?
                    }
                }
                dose = new String(chars);
            }

            MedicationAuthorisationType authorisationType = null;

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {

                    if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_AUTHORISATION_CANCELLATION)) {
                        //this extension is a compound one, with one or two inner extensions, giving us the date and performer
                        if (extension.hasExtension()) {
                            for (Extension innerExtension: extension.getExtension()) {
                                if (innerExtension.getValue() instanceof DateType) {
                                    DateType d = (DateType)innerExtension.getValue();
                                    cancellationDate = d.getValue();
                                }
                            }
                        }

                    } else if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY)) {
                        Quantity q = (Quantity)extension.getValue();
                        quantityValue = q.getValue();
                        quantityUnit = q.getUnit();

                    } else if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_AUTHORISATION_TYPE)) {
                        Coding c = (Coding)extension.getValue();
                        authorisationType = MedicationAuthorisationType.fromCode(c.getCode());
                    }
                }
            }

            authorisationTypeId = authorisationType.ordinal();

            model.writeUpsert(id,
                organisationId,
                patientId,
                encounterId,
                practitionerId,
                clinicalEffectiveDate,
                datePrecisionId,
                dmdId,
                isActive,
                cancellationDate,
                dose,
                quantityValue,
                quantityUnit,
                authorisationTypeId,
                originalTerm);
        }
    }

    /*public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.MedicationStatement model = new org.endeavourhealth.core.xml.enterprise.MedicationStatement();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            MedicationStatement fhir = (MedicationStatement)deserialiseResouce(resource);

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

            if (fhir.hasInformationSource()) {
                Reference practitionerReference = fhir.getInformationSource();
                Integer enterprisePractitionerUuid = findEnterpriseId(new Practitioner(), practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid);
            }

            if (fhir.hasDateAssertedElement()) {
                DateTimeType dt = fhir.getDateAssertedElement();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getMedicationCodeableConcept());
            model.setDmdId(snomedConceptId);

            //add term too, for easy display of results
            String originalTerm = fhir.getMedicationCodeableConcept().getText();
            //if we failed to find one, it's because of a change in how the CodeableConcept was generated, so find the term differently
            if (Strings.isNullOrEmpty(originalTerm)) {
                originalTerm = findSnomedConceptText(fhir.getMedicationCodeableConcept());
            }
            model.setOriginalTerm(originalTerm);

            if (fhir.hasStatus()) {
                MedicationStatement.MedicationStatementStatus fhirStatus = fhir.getStatus();
                model.setIsActive(fhirStatus == MedicationStatement.MedicationStatementStatus.ACTIVE);
            }

            if (fhir.hasDosage()) {
                if (fhir.getDosage().size() > 1) {
                    throw new TransformException("Cannot support MedicationStatements with more than one dose " + fhir.getId());
                }

                MedicationStatement.MedicationStatementDosageComponent doseage = fhir.getDosage().get(0);
                String dose = doseage.getText();

                //one of the Emis test packs includes the unicode \u0001 character in the dose. This should be handled
                //during the inbound transform, but the data is already in the DB now, so needs handling here
                char[] chars = dose.toCharArray();
                for (int i=0; i<chars.length; i++) {
                    char c = chars[i];
                    if (c == 1) {
                        chars[i] = '?'; //just replace with ?
                    }
                }
                dose = new String(chars);

                model.setDose(dose);
            }

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {

                    if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_AUTHORISATION_CANCELLATION)) {
                        //this extension is a compound one, with one or two inner extensions, giving us the date and performer
                        if (extension.hasExtension()) {
                            for (Extension innerExtension: extension.getExtension()) {
                                if (innerExtension.getValue() instanceof DateType) {
                                    DateType d = (DateType)innerExtension.getValue();
                                    model.setCancellationDate(convertDate(d.getValue()));
                                }
                            }
                        }

                    } else if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY)) {
                        Quantity q = (Quantity)extension.getValue();
                        model.setQuantityValue(q.getValue());
                        model.setQuantityUnit(q.getUnit());

                    } else if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_AUTHORISATION_TYPE)) {
                        Coding c = (Coding)extension.getValue();
                        MedicationAuthorisationType type = MedicationAuthorisationType.fromCode(c.getCode());
                        model.setMedicationStatementAuthorisationTypeId(type.ordinal());
                    }
                }
            }
        }

        data.getMedicationStatement().add(model);
    }*/



}


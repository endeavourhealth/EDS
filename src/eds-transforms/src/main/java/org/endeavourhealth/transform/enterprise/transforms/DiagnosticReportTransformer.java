package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Extension;
import org.hl7.fhir.instance.model.Reference;

import java.util.Map;

public class DiagnosticReportTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                          EnterpriseData data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.DiagnosticReport model = new org.endeavourhealth.core.xml.enterprise.DiagnosticReport();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            DiagnosticReport fhir = (DiagnosticReport)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getSubject();
            Integer enterprisePatientUuid = findEnterpriseId(patientReference);
            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasEncounter()) {
                Reference encounterReference = fhir.getEncounter();
                Integer enterpriseEncounterUuid = findEnterpriseId(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid);
            }

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {
                    if (extension.getUrl().equals(FhirExtensionUri.DIAGNOSTIC_REPORT_FILED_BY)) {
                        Reference practitionerReference = (Reference)extension.getValue();
                        Integer enterprisePractitionerUuid = findEnterpriseId(practitionerReference);
                        model.setPractitionerId(enterprisePractitionerUuid);
                    }
                }
            }

            if (fhir.hasEffectiveDateTimeType()) {
                DateTimeType dt = fhir.getEffectiveDateTimeType();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getCode());
            model.setSnomedConceptId(snomedConceptId);
        }

        data.getDiagnosticReport().add(model);
    }
}

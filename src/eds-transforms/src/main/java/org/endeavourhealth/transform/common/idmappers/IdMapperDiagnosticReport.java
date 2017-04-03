package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class IdMapperDiagnosticReport extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        DiagnosticReport report = (DiagnosticReport)resource;

        if (report.hasIdentifier()) {
            super.mapIdentifiers(report.getIdentifier(), resource, serviceId, systemId);
        }
        if (report.hasSubject()) {
            super.mapReference(report.getSubject(), resource, serviceId, systemId);
        }
        if (report.hasEncounter()) {
            super.mapReference(report.getEncounter(), resource, serviceId, systemId);
        }
        if (report.hasPerformer()) {
            super.mapReference(report.getPerformer(), resource, serviceId, systemId);
        }
        if (report.hasRequest()) {
            super.mapReferences(report.getRequest(), resource, serviceId, systemId);
        }
        if (report.hasSpecimen()) {
            super.mapReferences(report.getSpecimen(), resource, serviceId, systemId);
        }
        if (report.hasResult()) {
            super.mapReferences(report.getResult(), resource, serviceId, systemId);
        }
        if (report.hasImagingStudy()) {
            super.mapReferences(report.getImagingStudy(), resource, serviceId, systemId);
        }
        if (report.hasImage()) {
            for (DiagnosticReport.DiagnosticReportImageComponent image: report.getImage()) {
                if (image.hasLink()) {
                    super.mapReference(image.getLink(), resource, serviceId, systemId);
                }
            }
        }

        return super.mapCommonResourceFields(report, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        DiagnosticReport report = (DiagnosticReport)resource;
        if (report.hasSubject()) {
            return ReferenceHelper.getReferenceId(report.getSubject(), ResourceType.Patient);
        }
        return null;
    }
}

package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.DiagnosticOrder;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperDiagnosticReport extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        DiagnosticReport report = (DiagnosticReport)resource;

        super.mapResourceId(report, serviceId, systemInstanceId);
        super.mapExtensions(report, serviceId, systemInstanceId);

        if (report.hasIdentifier()) {
            super.mapIdentifiers(report.getIdentifier(), serviceId, systemInstanceId);
        }
        if (report.hasSubject()) {
            super.mapReference(report.getSubject(), serviceId, systemInstanceId);
        }
        if (report.hasEncounter()) {
            super.mapReference(report.getEncounter(), serviceId, systemInstanceId);
        }
        if (report.hasPerformer()) {
            super.mapReference(report.getPerformer(), serviceId, systemInstanceId);
        }
        if (report.hasRequest()) {
            super.mapReferences(report.getRequest(), serviceId, systemInstanceId);
        }
        if (report.hasSpecimen()) {
            super.mapReferences(report.getSpecimen(), serviceId, systemInstanceId);
        }
        if (report.hasResult()) {
            super.mapReferences(report.getResult(), serviceId, systemInstanceId);
        }
        if (report.hasImagingStudy()) {
            super.mapReferences(report.getImagingStudy(), serviceId, systemInstanceId);
        }
        if (report.hasImage()) {
            for (DiagnosticReport.DiagnosticReportImageComponent image: report.getImage()) {
                if (image.hasLink()) {
                    super.mapReference(image.getLink(), serviceId, systemInstanceId);
                }
            }
        }
   }
}

package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperDiagnosticReport extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        DiagnosticReport report = (DiagnosticReport)resource;

        super.mapResourceId(report, serviceId, systemId);
        super.mapExtensions(report, serviceId, systemId);

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
   }
}

package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.DiagnosticOrder;
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
            super.mapIdentifiers(report.getIdentifier(), serviceId, systemId);
        }
        if (report.hasSubject()) {
            super.mapReference(report.getSubject(), serviceId, systemId);
        }
        if (report.hasEncounter()) {
            super.mapReference(report.getEncounter(), serviceId, systemId);
        }
        if (report.hasPerformer()) {
            super.mapReference(report.getPerformer(), serviceId, systemId);
        }
        if (report.hasRequest()) {
            super.mapReferences(report.getRequest(), serviceId, systemId);
        }
        if (report.hasSpecimen()) {
            super.mapReferences(report.getSpecimen(), serviceId, systemId);
        }
        if (report.hasResult()) {
            super.mapReferences(report.getResult(), serviceId, systemId);
        }
        if (report.hasImagingStudy()) {
            super.mapReferences(report.getImagingStudy(), serviceId, systemId);
        }
        if (report.hasImage()) {
            for (DiagnosticReport.DiagnosticReportImageComponent image: report.getImage()) {
                if (image.hasLink()) {
                    super.mapReference(image.getLink(), serviceId, systemId);
                }
            }
        }
   }
}

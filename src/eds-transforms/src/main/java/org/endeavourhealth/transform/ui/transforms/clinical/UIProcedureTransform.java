package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.common.exceptions.TransformRuntimeException;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIProcedure;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.hl7.fhir.instance.model.Procedure;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.List;
import java.util.stream.Collectors;

public class UIProcedureTransform extends UIClinicalTransform<Procedure, UIProcedure> {

    @Override
    public List<UIProcedure> transform(List<Procedure> resources, ReferencedResources referencedResources) {
        return resources
                .stream()
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    private static UIProcedure transform(Procedure procedure, ReferencedResources referencedResources) {
        try {
            return new UIProcedure()
                    .setId(procedure.getId())
                    .setCode(CodeHelper.convert(procedure.getCode()))
                    .setEffectiveDate(getPerformedDate(procedure))
                    .setEffectivePractitioner(getPerformer(procedure, referencedResources))
                    .setRecordedDate(getRecordedDateExtensionValue(procedure))
                    .setRecordingPractitioner(getRecordedByExtensionValue(procedure, referencedResources))
                    .setNotes(getNotes(procedure.getNotes()));

        } catch (Exception e) {
            throw new TransformRuntimeException(e);
        }
    }

    private static UIDate getPerformedDate(Procedure procedure) {
        try {
            if (!procedure.hasPerformedDateTimeType())
                return null;

            return DateHelper.convert(procedure.getPerformedDateTimeType());
        } catch (Exception e) {
            throw new TransformRuntimeException(e);
        }
    }

    private static UIPractitioner getPerformer(Procedure procedure, ReferencedResources referencedResources) {
        return referencedResources.getUIPractitioner(procedure
                .getPerformer()
                .stream()
                .filter(t -> ReferenceHelper.isResourceType(t.getActor(), ResourceType.Practitioner))
                .map(t -> t.getActor())
                .collect(StreamExtension.firstOrNullCollector()));
    }

    @Override
    public List<Reference> getReferences(List<Procedure> resources) {
        return StreamExtension.concat(
                resources
                        .stream()
                        .map(t -> t.getSubject()),
                resources
                        .stream()
                        .filter(t -> t.hasPerformer())
                        .flatMap(t -> t.getPerformer().stream())
                        .map(t -> t.getActor()),
                resources
                        .stream()
                        .filter(t -> t.hasEncounter())
                        .map(t -> t.getEncounter()),
                resources
                        .stream()
                        .filter(t -> t.hasLocation())
                        .map(t -> t.getLocation()),
                resources
                        .stream()
                        .map(t -> getRecordedByExtensionValue(t))
                        .filter(t -> (t != null)))
                .collect(Collectors.toList());
    }
}

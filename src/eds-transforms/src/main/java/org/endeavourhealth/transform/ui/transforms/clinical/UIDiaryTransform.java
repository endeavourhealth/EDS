package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.transform.common.exceptions.TransformRuntimeException;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIDiary;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UIDiaryTransform extends UIClinicalTransform<ProcedureRequest, UIDiary> {
    @Override
    public List<UIDiary> transform(List<ProcedureRequest> resources, ReferencedResources referencedResources) {
        return resources
                .stream()
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    private UIDiary transform(ProcedureRequest procedureRequest, ReferencedResources referencedResources) {
        return new UIDiary()
                .setId(procedureRequest.getId())
                .setCode(CodeHelper.convert(procedureRequest.getCode()))
                .setEffectiveDate(DateHelper.convert(getScheduledDateTimeType(procedureRequest)));
    }

    private DateTimeType getScheduledDateTimeType(ProcedureRequest procedureRequest) {
        try {
            if (!procedureRequest.hasScheduledDateTimeType())
                return null;

            return procedureRequest.getScheduledDateTimeType();
        } catch (Exception e) {
            throw new TransformRuntimeException(e);
        }
    }

    @Override
    public List<Reference> getReferences(List<ProcedureRequest> resources) {
        return new ArrayList<>();
    }
}

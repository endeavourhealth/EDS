package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.ui.helpers.ExtensionHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.UIPractitioner;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;

public abstract class UIClinicalTransform<T extends Resource, U extends UIResource> {
    public abstract List<U> transform(List<T> resources, ReferencedResources referencedResources);
    public abstract List<Reference> getReferences(List<T> resources);

    protected static UIPractitioner getRecordedByExtensionValue(DomainResource resource, ReferencedResources referencedResources) {
        Reference reference = ExtensionHelper.getExtensionValue(resource, FhirExtensionUri.RECORDED_BY, Reference.class);
        return referencedResources.getUIPractitioner(reference);
    }

    protected static Date getRecordedDateExtensionValue(DomainResource resource) {
        DateTimeType recordedDate = ExtensionHelper.getExtensionValue(resource, FhirExtensionUri.RECORDED_DATE, DateTimeType.class);

        if (recordedDate == null)
            return null;

        return recordedDate.getValue();
    }
}

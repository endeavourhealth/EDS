package org.endeavourhealth.transform.common;

import org.endeavourhealth.common.fhir.FhirResourceException;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.ResourceHelper;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class FhirHelper {
    public static <T extends Resource> Reference findAndCreateReference(Class<T> type, List<Resource> resources) throws TransformException {
        try {
            return ReferenceHelper.findAndCreateReference(type, resources);
        } catch (FhirResourceException e) {
            throw new TransformException("Error creating reference, see cause", e);
        }
    }

    public static <T extends Resource> T findResourceOfType(Class<T> type, List<Resource> resources) throws TransformException {
        try {
            return ResourceHelper.findResourceOfType(type, resources);
        } catch (FhirResourceException e) {
            throw new TransformException("Error finding resource, see cause", e);
        }
    }

    public static <T extends Resource> String findResourceId(Class<T> type, List<Resource> resources) throws TransformException {
        try {
            return ResourceHelper.findResourceId(type, resources);
        } catch (FhirResourceException e) {
            throw new TransformException("Error finding resource, see cause", e);
        }
    }
}

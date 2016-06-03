package org.endeavourhealth.transform.fhir;

import com.google.common.base.Strings;
import org.hl7.fhir.instance.model.Annotation;

public class AnnotationHelper {

    public static Annotation createAnnotation(String notes) {
        if (Strings.isNullOrEmpty(notes)) {
            return null;
        }

        return new Annotation().setText(notes);
    }
}

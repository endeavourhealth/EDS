package org.endeavourhealth.transform.ui.models.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIObservation extends UIResource<UIObservation> {
    private UICodeableConcept code;
    private Date effectiveDateTime;
}

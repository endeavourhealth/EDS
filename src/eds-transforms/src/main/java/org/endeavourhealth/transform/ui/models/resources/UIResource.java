package org.endeavourhealth.transform.ui.models.resources;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class UIResource<T extends UIResource> {
    private String id;

    public String getId() {
        return id;
    }

    public T setId(String id) {
        this.id = id;
        return (T)this;
    }
}

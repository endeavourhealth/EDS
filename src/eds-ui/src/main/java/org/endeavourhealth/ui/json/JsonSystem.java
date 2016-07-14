package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonSystem {
    private String uuid = null;
    private String name = null;
    private List<TechnicalInterface> technicalInterface = null;

    public JsonSystem() {
    }

    public JsonSystem(System system) {
        this.uuid = system.getUuid();
        this.name = system.getName();
        this.technicalInterface = system.getTechnicalInterface();
    }

    /**
     * gets/sets
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TechnicalInterface> getTechnicalInterface() {
        return technicalInterface;
    }

    public void setTechnicalInterface(List<TechnicalInterface> technicalInterface) {
        this.technicalInterface = technicalInterface;
    }

}

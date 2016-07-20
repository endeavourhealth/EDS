package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonServiceInterfaceEndpoint {
    private UUID systemUuid = null;
    private UUID technicalInterfaceUuid = null;
    private String endpoint = null;

    public JsonServiceInterfaceEndpoint() {
    }

    /**
     * gets/sets
     */

    public UUID getSystemUuid() {
        return systemUuid;
    }

    public void setSystemUuid(UUID systemUuid) {
        this.systemUuid = systemUuid;
    }

    public UUID getTechnicalInterfaceUuid() {
        return technicalInterfaceUuid;
    }

    public void setTechnicalInterfaceUuid(UUID technicalInterfaceUuid) {
        this.technicalInterfaceUuid = technicalInterfaceUuid;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}

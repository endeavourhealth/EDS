package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.json.JsonServiceInterfaceEndpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonService {
    private UUID uuid = null;
    private String localIdentifier = null;
    private String name = null;
    private List<JsonServiceInterfaceEndpoint> endpoints = null;
    private Map<UUID, String> organisations = null;

    public JsonService() {
    }

    public JsonService(Service service) throws IOException {
        this.uuid = service.getId();
        this.localIdentifier = service.getLocalIdentifier();
        this.name = service.getName();
        this.organisations = service.getOrganisations();

        String endpointJson = service.getEndpoints();
        if (endpointJson != null && !endpointJson.isEmpty()) {
            this.endpoints = new ObjectMapper().readValue(endpointJson, new TypeReference<List<JsonServiceInterfaceEndpoint>>(){});
        } else {
            this.endpoints = new ArrayList<>();
        }
    }

    /**
     * gets/sets
     */
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    public void setLocalIdentifier(String localIdentifier) {
        this.localIdentifier = localIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JsonServiceInterfaceEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<JsonServiceInterfaceEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public Map<UUID, String> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(Map<UUID, String> organisations) {
        this.organisations = organisations;
    }
}

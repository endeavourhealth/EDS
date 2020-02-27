package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonService {
    private UUID uuid = null;
    private String localIdentifier = null;
    private String publisherConfigName = null;
    private String name = null;
    private List<ServiceInterfaceEndpoint> endpoints = null;
    private String postcode;
    private String ccgCode;
    private String organisationTypeDesc;
    private String organisationTypeCode;
    private List<JsonServiceSystemStatus> systemStatuses;
    private String alias;
    private Map<String, String> tags;

    public JsonService() {
    }

    public JsonService(Service service, List<JsonServiceSystemStatus> statuses) throws Exception {
        this.uuid = service.getId();
        this.localIdentifier = service.getLocalId();
        this.publisherConfigName = service.getPublisherConfigName();
        this.name = service.getName();
        //this.additionalInfo = additionalInfo;
        this.endpoints = service.getEndpointsList();
        this.postcode = service.getPostcode();
        this.ccgCode = service.getCcgCode();
        if (service.getOrganisationType() != null) {
            this.organisationTypeDesc = service.getOrganisationType().getDescription();
            this.organisationTypeCode = service.getOrganisationType().getCode();
        }
        this.alias = service.getAlias();
        if (service.getTags() != null) {
            this.tags = new HashMap<>(service.getTags());
        }
        this.systemStatuses = statuses;
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

    public String getPublisherConfigName() {
        return publisherConfigName;
    }

    public void setPublisherConfigName(String publisherConfigName) {
        this.publisherConfigName = publisherConfigName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ServiceInterfaceEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<ServiceInterfaceEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCcgCode() {
        return ccgCode;
    }

    public void setCcgCode(String ccgCode) {
        this.ccgCode = ccgCode;
    }

    public String getOrganisationTypeDesc() {
        return organisationTypeDesc;
    }

    public void setOrganisationTypeDesc(String organisationTypeDesc) {
        this.organisationTypeDesc = organisationTypeDesc;
    }

    public String getOrganisationTypeCode() {
        return organisationTypeCode;
    }

    public void setOrganisationTypeCode(String organisationTypeCode) {
        this.organisationTypeCode = organisationTypeCode;
    }

    public List<JsonServiceSystemStatus> getSystemStatuses() {
        return systemStatuses;
    }

    public void setSystemStatuses(List<JsonServiceSystemStatus> systemStatuses) {
        this.systemStatuses = systemStatuses;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}


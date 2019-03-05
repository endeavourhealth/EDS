package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.fhirStorage.JsonServiceInterfaceEndpoint;

import java.io.IOException;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonService {
    private UUID uuid = null;
    private String localIdentifier = null;
    private String publisherConfigName = null;
    private boolean hasInboundError;
    private String name = null;
    private List<JsonServiceInterfaceEndpoint> endpoints = null;
    private Map<UUID, String> organisations = null;
    private String additionalInfo = null; //transient info, such as progress in deleting data
    private String notes = null;
    private String postcode;
    private String ccgCode;
    private String organisationTypeDesc;
    private String organisationTypeCode;
    private Map<String, Date> lastDataReceived; //last data date for each system
    private Map<String, Boolean> lastDataProcessed; //whether we're fully up to date for each system
    //private String lastDataDesc; //free-text saying when the last data received was

    public JsonService() {
    }

    public JsonService(Service service) throws IOException {
        this(service, null, false);
    }

    public JsonService(Service service, String additionalInfo, boolean hasInboundError) throws IOException {
        this.uuid = service.getId();
        this.localIdentifier = service.getLocalId();
        this.hasInboundError = hasInboundError;
        this.name = service.getName();
        this.organisations = service.getOrganisations();
        this.publisherConfigName = service.getPublisherConfigName();
        this.additionalInfo = additionalInfo;

        String endpointJson = service.getEndpoints();
        if (endpointJson != null && !endpointJson.isEmpty()) {
            this.endpoints = ObjectMapperPool.getInstance().readValue(endpointJson, new TypeReference<List<JsonServiceInterfaceEndpoint>>(){});
        } else {
            this.endpoints = new ArrayList<>();
        }

        this.notes = service.getNotes();
        this.postcode = service.getPostcode();
        this.ccgCode = service.getCcgCode();
        if (service.getOrganisationType() != null) {
            this.organisationTypeDesc = service.getOrganisationType().getDescription();
            this.organisationTypeCode = service.getOrganisationType().getCode();
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

    public String getPublisherConfigName() {
        return publisherConfigName;
    }

    public void setPublisherConfigName(String publisherConfigName) {
        this.publisherConfigName = publisherConfigName;
    }

    public boolean isHasInboundError() {
        return hasInboundError;
    }

    public void setHasInboundError(boolean hasInboundError) {
        this.hasInboundError = hasInboundError;
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

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public Map<String, Date> getLastDataReceived() {
        return lastDataReceived;
    }

    public void setLastDataReceived(Map<String, Date> lastDataReceived) {
        this.lastDataReceived = lastDataReceived;
    }

    public Map<String, Boolean> getLastDataProcessed() {
        return lastDataProcessed;
    }

    public void setLastDataProcessed(Map<String, Boolean> lastDataProcessed) {
        this.lastDataProcessed = lastDataProcessed;
    }
}

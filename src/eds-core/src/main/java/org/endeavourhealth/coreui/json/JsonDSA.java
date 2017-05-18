package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonDSA {
    private String uuid = null;
    private String name = null;
    private String description = null;
    private String derivation = null;
    private String publisherInformation = null;
    private String publisherContractInformation = null;
    private String subscriberInformation = null;
    private String subscriberContractInformation = null;
    private Short dsaStatusId = null;
    private String consentModel = null;
    private Map<UUID, String> dataFlows = null;
    private Map<UUID, String> regions = null;
    private Map<UUID, String> publishers = null;
    private Map<UUID, String> subscribers = null;
    private List<JsonDsaPurpose> purposes = new ArrayList<>();
    private List<JsonDsaBenefit> benefits = new ArrayList<>();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDerivation() {
        return derivation;
    }

    public void setDerivation(String derivation) {
        this.derivation = derivation;
    }

    public String getPublisherInformation() {
        return publisherInformation;
    }

    public void setPublisherInformation(String publisherInformation) {
        this.publisherInformation = publisherInformation;
    }

    public String getPublisherContractInformation() {
        return publisherContractInformation;
    }

    public void setPublisherContractInformation(String publisherContractInformation) {
        this.publisherContractInformation = publisherContractInformation;
    }

    public String getSubscriberInformation() {
        return subscriberInformation;
    }

    public void setSubscriberInformation(String subscriberInformation) {
        this.subscriberInformation = subscriberInformation;
    }

    public String getSubscriberContractInformation() {
        return subscriberContractInformation;
    }

    public void setSubscriberContractInformation(String subscriberContractInformation) {
        this.subscriberContractInformation = subscriberContractInformation;
    }

    public Short getDsaStatusId() {
        return dsaStatusId;
    }

    public void setDsaStatusId(Short dsaStatusId) {
        this.dsaStatusId = dsaStatusId;
    }

    public String getConsentModel() {
        return consentModel;
    }

    public void setConsentModel(String consentModel) {
        this.consentModel = consentModel;
    }

    public Map<UUID, String> getDataFlows() {
        return dataFlows;
    }

    public void setDataFlows(Map<UUID, String> dataFlows) {
        this.dataFlows = dataFlows;
    }

    public Map<UUID, String> getRegions() {
        return regions;
    }

    public void setRegions(Map<UUID, String> regions) {
        this.regions = regions;
    }

    public Map<UUID, String> getPublishers() {
        return publishers;
    }

    public void setPublishers(Map<UUID, String> publishers) {
        this.publishers = publishers;
    }

    public Map<UUID, String> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Map<UUID, String> subscribers) {
        this.subscribers = subscribers;
    }

    public List<JsonDsaPurpose> getPurposes() {
        return purposes;
    }

    public void setPurposes(List<JsonDsaPurpose> purposes) {
        this.purposes = purposes;
    }

    public List<JsonDsaBenefit> getBenefits() {
        return benefits;
    }

    public void setBenefits(List<JsonDsaBenefit> benefits) {
        this.benefits = benefits;
    }
}

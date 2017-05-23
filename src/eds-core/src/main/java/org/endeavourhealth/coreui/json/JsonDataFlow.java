package org.endeavourhealth.coreui.json;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonDataFlow {
    private String uuid = null;
    private String name = null;
    private Short directionId = null;
    private Short flowScheduleId = null;
    private Integer approximateVolume = null;
    private Short dataExchangeMethodId = null;
    private Short storageProtocolId = null;
    private Short securityInfrastructureId = null;
    private Short securityArchitectureId = null;
    private Short flowStatusId = null;
    private String additionalDocumentation = null;
    private String signOff = null;
    private Map<UUID, String> dsas = null;
    private Map<UUID, String> dpas = null;

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

    public Short getDirectionId() {
        return directionId;
    }

    public void setDirectionId(Short directionId) {
        this.directionId = directionId;
    }

    public Short getFlowScheduleId() {
        return flowScheduleId;
    }

    public void setFlowScheduleId(Short flowScheduleId) {
        this.flowScheduleId = flowScheduleId;
    }

    public Short getDataExchangeMethodId() {
        return dataExchangeMethodId;
    }

    public void setDataExchangeMethodId(Short dataExchangeMethodId) {
        this.dataExchangeMethodId = dataExchangeMethodId;
    }

    public Short getFlowStatusId() {
        return flowStatusId;
    }

    public void setFlowStatusId(Short flowStatusId) {
        this.flowStatusId = flowStatusId;
    }

    public Integer getApproximateVolume() {
        return approximateVolume;
    }

    public void setApproximateVolume(Integer approximateVolume) {
        this.approximateVolume = approximateVolume;
    }

    public String getAdditionalDocumentation() {
        return additionalDocumentation;
    }

    public void setAdditionalDocumentation(String additionalDocumentation) {
        this.additionalDocumentation = additionalDocumentation;
    }

    public String getSignOff() {
        return signOff;
    }

    public void setSignOff(String signOff) {
        this.signOff = signOff;
    }

    public Map<UUID, String> getDsas() {
        return dsas;
    }

    public void setDsas(Map<UUID, String> dsas) {
        this.dsas = dsas;
    }

    public Map<UUID, String> getDpas() {
        return dpas;
    }

    public void setDpas(Map<UUID, String> dpas) {
        this.dpas = dpas;
    }

    public Short getStorageProtocolId() {
        return storageProtocolId;
    }

    public void setStorageProtocolId(Short storageProtocolId) {
        this.storageProtocolId = storageProtocolId;
    }

    public Short getSecurityInfrastructureId() {
        return securityInfrastructureId;
    }

    public void setSecurityInfrastructureId(Short securityInfrastructureId) {
        this.securityInfrastructureId = securityInfrastructureId;
    }

    public Short getSecurityArchitectureId() {
        return securityArchitectureId;
    }

    public void setSecurityArchitectureId(Short securityArchitectureId) {
        this.securityArchitectureId = securityArchitectureId;
    }
}

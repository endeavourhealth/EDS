package org.endeavourhealth.coreui.json;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonDataFlow {
    private String uuid = null;
    private String name = null;
    private String status = null;
    private Short directionId = null;
    private Short flowScheduleId = null;
    private Integer approximateVolume = null;
    private Short dataExchangeMethodId = null;
    private Short flowStatusId = null;
    private String additionalDocumentation = null;
    private String signOff = null;
    private String dataSet = null;
    private String cohort = null;
    private String subscriber = null;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getDataSet() {
        return dataSet;
    }

    public void setDataSet(String dataSet) {
        this.dataSet = dataSet;
    }

    public String getCohort() {
        return cohort;
    }

    public void setCohort(String cohort) {
        this.cohort = cohort;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
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
}

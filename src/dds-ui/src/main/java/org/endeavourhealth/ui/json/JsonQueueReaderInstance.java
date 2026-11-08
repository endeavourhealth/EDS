package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonQueueReaderInstance {

    private String configId;
    private String queueName;
    private String hostName;
    private String applicationInstanceName;
    private Integer applicationInstanceNumber;
    private String status;
    private Long lastHeartbeat;
    private Long dtStarted;
    private Long dtJar;
    private Integer maxHeapMb;
    private String maxHeapDesc;
    private Integer currentHeapMb;
    private String currentHeapDesc;
    private Integer physicalMemoryMb;
    private String physicalMemoryDesc;
    private Double cpuLoad;
    private Boolean isBusy;
    private String queueType;
    private JsonMemoryDefaults memoryDefaults;

    public JsonQueueReaderInstance() {
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getApplicationInstanceName() {
        return applicationInstanceName;
    }

    public void setApplicationInstanceName(String applicationInstanceName) {
        this.applicationInstanceName = applicationInstanceName;
    }

    public Integer getApplicationInstanceNumber() {
        return applicationInstanceNumber;
    }

    public void setApplicationInstanceNumber(Integer applicationInstanceNumber) {
        this.applicationInstanceNumber = applicationInstanceNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Long getDtStarted() {
        return dtStarted;
    }

    public void setDtStarted(Long dtStarted) {
        this.dtStarted = dtStarted;
    }

    public Long getDtJar() {
        return dtJar;
    }

    public void setDtJar(Long dtJar) {
        this.dtJar = dtJar;
    }

    public Integer getMaxHeapMb() {
        return maxHeapMb;
    }

    public void setMaxHeapMb(Integer maxHeapMb) {
        this.maxHeapMb = maxHeapMb;
    }

    public String getMaxHeapDesc() {
        return maxHeapDesc;
    }

    public void setMaxHeapDesc(String maxHeapDesc) {
        this.maxHeapDesc = maxHeapDesc;
    }

    public Integer getCurrentHeapMb() {
        return currentHeapMb;
    }

    public void setCurrentHeapMb(Integer currentHeapMb) {
        this.currentHeapMb = currentHeapMb;
    }

    public String getCurrentHeapDesc() {
        return currentHeapDesc;
    }

    public void setCurrentHeapDesc(String currentHeapDesc) {
        this.currentHeapDesc = currentHeapDesc;
    }

    public Integer getPhysicalMemoryMb() {
        return physicalMemoryMb;
    }

    public void setPhysicalMemoryMb(Integer physicalMemoryMb) {
        this.physicalMemoryMb = physicalMemoryMb;
    }

    public String getPhysicalMemoryDesc() {
        return physicalMemoryDesc;
    }

    public void setPhysicalMemoryDesc(String physicalMemoryDesc) {
        this.physicalMemoryDesc = physicalMemoryDesc;
    }

    public Double getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(Double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public Boolean getBusy() {
        return isBusy;
    }

    public void setBusy(Boolean busy) {
        isBusy = busy;
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    public JsonMemoryDefaults getMemoryDefaults() {
        return memoryDefaults;
    }

    public void setMemoryDefaults(JsonMemoryDefaults memoryDefaults) {
        this.memoryDefaults = memoryDefaults;
    }
}

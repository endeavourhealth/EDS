package org.endeavourhealth.core.data.transform.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "transform", name = "vitrucare_patient_id_map")
public class VitruCarePatientIdMap {

    @PartitionKey
    @Column(name = "eds_patient_id")
    private UUID edsPatientId;

    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "system_id")
    private UUID systemId;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "vitrucare_id")
    private String vitruCareId;


    public UUID getEdsPatientId() {
        return edsPatientId;
    }

    public void setEdsPatientId(UUID edsPatientId) {
        this.edsPatientId = edsPatientId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public void setSystemId(UUID systemId) {
        this.systemId = systemId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getVitruCareId() {
        return vitruCareId;
    }

    public void setVitruCareId(String vitruCareId) {
        this.vitruCareId = vitruCareId;
    }
}

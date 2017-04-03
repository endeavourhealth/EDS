package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Table(keyspace = "admin", name = "patient_cohort")
public class PatientCohort {

    @PartitionKey(0)
    @Column(name = "protocol_id")
    private UUID protocolId;

    @PartitionKey(1)
    @Column(name = "service_id")
    private UUID serviceId;

    @ClusteringColumn(0)
    @Column(name = "patient_id")
    private UUID patientId;

    @ClusteringColumn(1)
    @Column(name = "version")
    private UUID version;

    @Column(name = "inserted")
    private Date inserted;

    @Column(name = "in_cohort")
    private boolean inCohort;

    public PatientCohort() {}

    public UUID getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(UUID protocolId) {
        this.protocolId = protocolId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getVersion() {
        return version;
    }

    public void setVersion(UUID version) {
        this.version = version;
    }

    public Date getInserted() {
        return inserted;
    }

    public void setInserted(Date inserted) {
        this.inserted = inserted;
    }

    public boolean isInCohort() {
        return inCohort;
    }

    public void setInCohort(boolean inCohort) {
        this.inCohort = inCohort;
    }
}

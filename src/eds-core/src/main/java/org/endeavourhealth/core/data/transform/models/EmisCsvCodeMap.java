package org.endeavourhealth.core.data.transform.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "transform", name = "emis_csv_code_map")
public class EmisCsvCodeMap {

    @PartitionKey
    @Column(name = "service_id")
    private UUID serviceId = null;
    @ClusteringColumn(0)
    @Column(name = "system_id")
    private UUID systemId = null;
    @ClusteringColumn(1)
    @Column(name = "medication")
    private boolean medication = false;
    @ClusteringColumn(2)
    @Column(name = "code_id")
    private Long codeId = null;
    @ClusteringColumn(3)
    @Column(name = "time_uuid")
    private UUID timeUuid = null;
    @Column(name = "code_type")
    private String codeType = null;
    @Column(name = "codeable_concept")
    private String codeableConcept = null;


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

    public boolean isMedication() {
        return medication;
    }

    public void setMedication(boolean medication) {
        this.medication = medication;
    }

    public Long getCodeId() {
        return codeId;
    }

    public void setCodeId(Long codeId) {
        this.codeId = codeId;
    }

    public UUID getTimeUuid() {
        return timeUuid;
    }

    public void setTimeUuid(UUID timeUuid) {
        this.timeUuid = timeUuid;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getCodeableConcept() {
        return codeableConcept;
    }

    public void setCodeableConcept(String codeableConcept) {
        this.codeableConcept = codeableConcept;
    }
}

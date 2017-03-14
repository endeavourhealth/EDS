package org.endeavourhealth.core.rdbms.eds;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
//@IdClass(ConfigPK.class)
@Table(name = "patient_search_local_identifier", schema = "\"public\"", catalog = "eds")
public class PatientSearchLocalIdentifier implements Serializable {

    @Id
    private UUID serviceId = null;
    @Id
    private UUID systemId = null;
    private String localId = null;
    @Id
    private String localIdSystem = null;
    @Id
    private UUID patientId = null;
    private Date lastUpdated = null;

    @Column(name = "service_id")
    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    @Column(name = "system_id")
    public UUID getSystemId() {
        return systemId;
    }

    public void setSystemId(UUID systemId) {
        this.systemId = systemId;
    }

    @Column(name = "local_id")
    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    @Column(name = "local_id_system")
    public String getLocalIdSystem() {
        return localIdSystem;
    }

    public void setLocalIdSystem(String localIdSystem) {
        this.localIdSystem = localIdSystem;
    }

    @Column(name = "patient_id")
    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    @Column(name = "last_updated")
    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}

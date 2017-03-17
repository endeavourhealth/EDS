package org.endeavourhealth.core.rdbms.eds;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
//@IdClass(ConfigPK.class)
@Table(name = "patient_search_local_identifier", schema = "public", catalog = "eds")
public class PatientSearchLocalIdentifier implements Serializable {

    private String serviceId = null;
    private String systemId = null;
    private String localId = null;
    private String localIdSystem = null;
    private String patientId = null;
    private Date lastUpdated = null;

    @Id
    @Column(name = "service_id")
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Id
    @Column(name = "system_id")
    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Column(name = "local_id")
    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    @Id
    @Column(name = "local_id_system")
    public String getLocalIdSystem() {
        return localIdSystem;
    }

    public void setLocalIdSystem(String localIdSystem) {
        this.localIdSystem = localIdSystem;
    }

    @Id
    @Column(name = "patient_id")
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
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

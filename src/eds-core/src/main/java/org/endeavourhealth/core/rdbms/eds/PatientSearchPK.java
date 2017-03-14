package org.endeavourhealth.core.rdbms.eds;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

public class PatientSearchPK implements Serializable {
    private UUID serviceId = null;
    private UUID systemId = null;
    private UUID patientId = null;

    @Id
    //@Type(type="uuid-char")
    @Column(name = "service_id", columnDefinition="uuid", nullable = false)
    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    @Id
    //@Type(type="uuid-char")
    @Column(name = "system_id", columnDefinition="uuid", nullable = false)
    public UUID getSystemId() {
        return systemId;
    }

    public void setSystemId(UUID systemId) {
        this.systemId = systemId;
    }

    @Id
    //@Type(type="uuid-char")
    @Column(name = "patient_id", columnDefinition="uuid", nullable = false)
    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }
}

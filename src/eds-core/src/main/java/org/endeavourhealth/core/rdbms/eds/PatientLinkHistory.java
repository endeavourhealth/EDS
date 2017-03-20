package org.endeavourhealth.core.rdbms.eds;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "patient_link_history", schema = "public", catalog = "eds")
public class PatientLinkHistory implements Serializable {

    private String patientId = null;
    private Date updated = null;
    private String newPersonId = null;
    private String previousPersonId = null;

    @Id
    @Column(name = "patient_id", nullable = false)
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    @Id
    @Column(name = "updated", nullable = false)
    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Column(name = "new_person_id", nullable = false)
    public String getNewPersonId() {
        return newPersonId;
    }

    public void setNewPersonId(String newPersonId) {
        this.newPersonId = newPersonId;
    }

    @Column(name = "previous_person_id")
    public String getPreviousPersonId() {
        return previousPersonId;
    }

    public void setPreviousPersonId(String previousPersonId) {
        this.previousPersonId = previousPersonId;
    }
}

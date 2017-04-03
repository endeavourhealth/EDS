package org.endeavourhealth.core.rdbms.transform;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "pseudo_id_map", schema = "public", catalog = "transform")
public class PseudoIdMap implements Serializable {

    private String patientId = null;
    private String pseudoId = null;

    @Id
    @Column(name = "patient_id", nullable = false)
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    @Column(name = "pseudo_id", nullable = false)
    public String getPseudoId() {
        return pseudoId;
    }

    public void setPseudoId(String pseudoId) {
        this.pseudoId = pseudoId;
    }
}

package org.endeavourhealth.core.rdbms.transform;


import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "patient_person_map", schema = "public", catalog = "transform")
public class PatientPersonMap implements Serializable {
}

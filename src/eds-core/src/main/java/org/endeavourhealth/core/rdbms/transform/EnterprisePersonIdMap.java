package org.endeavourhealth.core.rdbms.transform;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "enterprise_person_id_map", schema = "public", catalog = "transform")
public class EnterprisePersonIdMap implements Serializable {

    private String personId = null;
    private Long enterprisePersonId;

    @Id
    @Column(name = "person_id", nullable = false)
    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    @Generated(GenerationTime.INSERT)
    @Column(name = "enterprise_person_id", insertable = false)
    public Long getEnterprisePersonId() {
        return enterprisePersonId;
    }

    public void setEnterprisePersonId(Long enterprisePersonId) {
        this.enterprisePersonId = enterprisePersonId;
    }
}

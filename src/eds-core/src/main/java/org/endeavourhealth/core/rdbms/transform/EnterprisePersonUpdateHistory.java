package org.endeavourhealth.core.rdbms.transform;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "enterprise_person_update_history", schema = "public")
public class EnterprisePersonUpdateHistory implements Serializable {

    private Date dateRun = null;

    @Id
    @Column(name = "date_run", nullable = false)
    public Date getDateRun() {
        return dateRun;
    }

    public void setDateRun(Date dateRun) {
        this.dateRun = dateRun;
    }


}

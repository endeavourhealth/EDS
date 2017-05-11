package org.endeavourhealth.core.rdbms.transform;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "household_id_map", schema = "public")
public class HouseholdIdMap implements Serializable {

    private String postcode = null;
    private String line1 = null;
    private String line2 = null;
    private Long householdId = null;

    @Id
    @Column(name = "postcode", nullable = false)
    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    @Id
    @Column(name = "line_1", nullable = false)
    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    @Id
    @Column(name = "line_2", nullable = false)
    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    @Generated(GenerationTime.INSERT)
    @Column(name = "household_id", insertable = false)
    public Long getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(Long householdId) {
        this.householdId = householdId;
    }
}

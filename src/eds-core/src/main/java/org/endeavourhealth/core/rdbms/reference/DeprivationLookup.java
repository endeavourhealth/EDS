package org.endeavourhealth.core.rdbms.reference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "deprivation_lookup", schema = "public", catalog = "reference")
public class DeprivationLookup {

    private String lsoaCode = null;
    private Integer imdRank = null;
    private Integer imdDecile = null;

    @Id
    @Column(name = "lsoa_code", nullable = false)
    public String getLsoaCode() {
        return lsoaCode;
    }

    public void setLsoaCode(String lsoaCode) {
        this.lsoaCode = lsoaCode;
    }

    @Column(name = "imd_rank", nullable = false)
    public Integer getImdRank() {
        return imdRank;
    }

    public void setImdRank(Integer imdRank) {
        this.imdRank = imdRank;
    }

    @Column(name = "imd_decile", nullable = false)
    public Integer getImdDecile() {
        return imdDecile;
    }

    public void setImdDecile(Integer imdDecile) {
        this.imdDecile = imdDecile;
    }
}

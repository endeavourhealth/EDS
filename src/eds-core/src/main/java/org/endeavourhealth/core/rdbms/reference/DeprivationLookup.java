package org.endeavourhealth.core.rdbms.reference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "deprivation_lookup", schema = "public", catalog = "reference")
public class DeprivationLookup {

    private String lsoaCode = null;
    private BigDecimal imdScore = null;
    private Integer imdDecile = null;

    @Id
    @Column(name = "lsoa_code", nullable = false)
    public String getLsoaCode() {
        return lsoaCode;
    }

    public void setLsoaCode(String lsoaCode) {
        this.lsoaCode = lsoaCode;
    }

    @Column(name = "imd_score", nullable = false)
    public BigDecimal getImdScore() {
        return imdScore;
    }

    public void setImdScore(BigDecimal imdScore) {
        this.imdScore = imdScore;
    }

    @Column(name = "imd_decile", nullable = false)
    public Integer getImdDecile() {
        return imdDecile;
    }

    public void setImdDecile(Integer imdDecile) {
        this.imdDecile = imdDecile;
    }
}

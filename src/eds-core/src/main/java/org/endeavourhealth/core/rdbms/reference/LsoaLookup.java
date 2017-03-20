package org.endeavourhealth.core.rdbms.reference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "lsoa_lookup", schema = "public", catalog = "reference")
public class LsoaLookup implements Serializable {

    private String lsoaCode = null;
    private String lsoaName = null;

    @Id
    @Column(name = "lsoa_code", nullable = false)
    public String getLsoaCode() {
        return lsoaCode;
    }

    public void setLsoaCode(String lsoaCode) {
        this.lsoaCode = lsoaCode;
    }

    @Column(name = "lsoa_name", nullable = false)
    public String getLsoaName() {
        return lsoaName;
    }

    public void setLsoaName(String lsoaName) {
        this.lsoaName = lsoaName;
    }
}

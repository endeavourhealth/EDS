package org.endeavourhealth.core.rdbms.reference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "msoa_lookup", schema = "public", catalog = "reference")
public class MsoaLookup implements Serializable {

    private String msoaCode = null;
    private String msoaName = null;

    @Id
    @Column(name = "msoa_code", nullable = false)
    public String getMsoaCode() {
        return msoaCode;
    }

    public void setMsoaCode(String msoaCode) {
        this.msoaCode = msoaCode;
    }

    @Column(name = "msoa_name", nullable = false)
    public String getMsoaName() {
        return msoaName;
    }

    public void setMsoaName(String msoaName) {
        this.msoaName = msoaName;
    }
}

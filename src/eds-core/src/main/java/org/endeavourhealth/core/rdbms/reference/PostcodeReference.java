package org.endeavourhealth.core.rdbms.reference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "postcode_reference", schema = "public", catalog = "reference")
public class PostcodeReference {

    private String postcodeNoSpace = null;
    private String postcode = null;
    private String lsoaCode = null;
    private String lsoaName = null;
    private String msoaCode = null;
    private String msoaName = null;
    private String ward = null;
    private String ward1998 = null;
    private String ccg = null;
    private BigDecimal townsendScore = null;

    @Id
    @Column(name = "postcode_no_space")
    public String getPostcodeNoSpace() {
        return postcodeNoSpace;
    }

    public void setPostcodeNoSpace(String postcodeNoSpace) {
        this.postcodeNoSpace = postcodeNoSpace;
    }

    @Column(name = "postcode")
    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    @Column(name = "lsoa_code")
    public String getLsoaCode() {
        return lsoaCode;
    }

    public void setLsoaCode(String lsoaCode) {
        this.lsoaCode = lsoaCode;
    }

    @Column(name = "lsoa_name")
    public String getLsoaName() {
        return lsoaName;
    }

    public void setLsoaName(String lsoaName) {
        this.lsoaName = lsoaName;
    }

    @Column(name = "msoa_code")
    public String getMsoaCode() {
        return msoaCode;
    }

    public void setMsoaCode(String msoaCode) {
        this.msoaCode = msoaCode;
    }

    @Column(name = "msoa_name")
    public String getMsoaName() {
        return msoaName;
    }

    public void setMsoaName(String msoaName) {
        this.msoaName = msoaName;
    }

    @Column(name = "ward")
    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    @Column(name = "ward_1998")
    public String getWard1998() {
        return ward1998;
    }

    public void setWard1998(String ward1998) {
        this.ward1998 = ward1998;
    }

    @Column(name = "ccg")
    public String getCcg() {
        return ccg;
    }

    public void setCcg(String ccg) {
        this.ccg = ccg;
    }

    @Column(name = "townsend_score")
    public BigDecimal getTownsendScore() {
        return townsendScore;
    }

    public void setTownsendScore(BigDecimal townsendScore) {
        this.townsendScore = townsendScore;
    }
}

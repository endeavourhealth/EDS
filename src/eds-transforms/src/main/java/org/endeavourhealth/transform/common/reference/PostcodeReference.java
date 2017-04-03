package org.endeavourhealth.transform.common.reference;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "postcode_reference", schema = "\"public\"", catalog = "reference")
public class PostcodeReference {

    private String postcodeNoSpace = null;
    private String postcode = null;
    private String lsoaCode = null;
    private String lsoaName = null;
    private BigDecimal northing = null;
    private BigDecimal easting = null;
    private String ward = null;
    private String ward1998 = null;
    private String commissioningRegion = null;
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

    @Basic
    @Column(name = "postcode")
    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    @Basic
    @Column(name = "lsoa_code")
    public String getLsoaCode() {
        return lsoaCode;
    }

    public void setLsoaCode(String lsoaCode) {
        this.lsoaCode = lsoaCode;
    }

    @Basic
    @Column(name = "lsoa_name")
    public String getLsoaName() {
        return lsoaName;
    }

    public void setLsoaName(String lsoaName) {
        this.lsoaName = lsoaName;
    }

    @Basic
    @Column(name = "northing")
    public BigDecimal getNorthing() {
        return northing;
    }

    public void setNorthing(BigDecimal northing) {
        this.northing = northing;
    }

    @Basic
    @Column(name = "easting")
    public BigDecimal getEasting() {
        return easting;
    }

    public void setEasting(BigDecimal easting) {
        this.easting = easting;
    }

    @Basic
    @Column(name = "ward")
    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    @Basic
    @Column(name = "ward_1998")
    public String getWard1998() {
        return ward1998;
    }

    public void setWard1998(String ward1998) {
        this.ward1998 = ward1998;
    }

    @Basic
    @Column(name = "commissioning_region")
    public String getCommissioningRegion() {
        return commissioningRegion;
    }

    public void setCommissioningRegion(String commissioningRegion) {
        this.commissioningRegion = commissioningRegion;
    }

    @Basic
    @Column(name = "ccg")
    public String getCcg() {
        return ccg;
    }

    public void setCcg(String ccg) {
        this.ccg = ccg;
    }

    @Basic
    @Column(name = "townsend_score")
    public BigDecimal getTownsendScore() {
        return townsendScore;
    }

    public void setTownsendScore(BigDecimal townsendScore) {
        this.townsendScore = townsendScore;
    }
}

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
    private Integer incomeRank = null;
    private Integer incomeDecile = null;
    private Integer employmentRank = null;
    private Integer employmentDecile = null;
    private Integer educationRank = null;
    private Integer educationDecile = null;
    private Integer healthRank = null;
    private Integer healthDecile = null;
    private Integer crimeRank = null;
    private Integer crimeDecile = null;
    private Integer housingAndServicesBarriersRank = null;
    private Integer housingAndServicesBarriersDecile = null;
    private Integer livingEnvironmentRank = null;
    private Integer livingEnvironmentDecile = null;

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

    @Column(name = "income_rank", nullable = false)
    public Integer getIncomeRank() {
        return incomeRank;
    }

    public void setIncomeRank(Integer incomeRank) {
        this.incomeRank = incomeRank;
    }

    @Column(name = "income_decile", nullable = false)
    public Integer getIncomeDecile() {
        return incomeDecile;
    }

    public void setIncomeDecile(Integer incomeDecile) {
        this.incomeDecile = incomeDecile;
    }

    @Column(name = "employment_rank", nullable = false)
    public Integer getEmploymentRank() {
        return employmentRank;
    }

    public void setEmploymentRank(Integer employmentRank) {
        this.employmentRank = employmentRank;
    }

    @Column(name = "employment_decile", nullable = false)
    public Integer getEmploymentDecile() {
        return employmentDecile;
    }

    public void setEmploymentDecile(Integer employmentDecile) {
        this.employmentDecile = employmentDecile;
    }

    @Column(name = "education_rank", nullable = false)
    public Integer getEducationRank() {
        return educationRank;
    }

    public void setEducationRank(Integer educationRank) {
        this.educationRank = educationRank;
    }

    @Column(name = "education_decile", nullable = false)
    public Integer getEducationDecile() {
        return educationDecile;
    }

    public void setEducationDecile(Integer educationDecile) {
        this.educationDecile = educationDecile;
    }

    @Column(name = "health_rank", nullable = false)
    public Integer getHealthRank() {
        return healthRank;
    }

    public void setHealthRank(Integer healthRank) {
        this.healthRank = healthRank;
    }

    @Column(name = "health_decile", nullable = false)
    public Integer getHealthDecile() {
        return healthDecile;
    }

    public void setHealthDecile(Integer healthDecile) {
        this.healthDecile = healthDecile;
    }

    @Column(name = "crime_rank", nullable = false)
    public Integer getCrimeRank() {
        return crimeRank;
    }

    public void setCrimeRank(Integer crimeRank) {
        this.crimeRank = crimeRank;
    }

    @Column(name = "crime_decile", nullable = false)
    public Integer getCrimeDecile() {
        return crimeDecile;
    }

    public void setCrimeDecile(Integer crimeDecile) {
        this.crimeDecile = crimeDecile;
    }

    @Column(name = "housing_and_services_barriers_rank", nullable = false)
    public Integer getHousingAndServicesBarriersRank() {
        return housingAndServicesBarriersRank;
    }

    public void setHousingAndServicesBarriersRank(Integer housingServicesBarriersRank) {
        this.housingAndServicesBarriersRank = housingServicesBarriersRank;
    }

    @Column(name = "housing_and_services_barriers_decile", nullable = false)
    public Integer getHousingAndServicesBarriersDecile() {
        return housingAndServicesBarriersDecile;
    }

    public void setHousingAndServicesBarriersDecile(Integer housingServicesBarriersDecile) {
        this.housingAndServicesBarriersDecile = housingServicesBarriersDecile;
    }

    @Column(name = "living_environment_rank", nullable = false)
    public Integer getLivingEnvironmentRank() {
        return livingEnvironmentRank;
    }

    public void setLivingEnvironmentRank(Integer livingEnvironmentRank) {
        this.livingEnvironmentRank = livingEnvironmentRank;
    }

    @Column(name = "living_environment_decile", nullable = false)
    public Integer getLivingEnvironmentDecile() {
        return livingEnvironmentDecile;
    }

    public void setLivingEnvironmentDecile(Integer livingEnvironmentDecile) {
        this.livingEnvironmentDecile = livingEnvironmentDecile;
    }
}

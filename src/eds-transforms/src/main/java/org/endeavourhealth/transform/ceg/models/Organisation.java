package org.endeavourhealth.transform.ceg.models;

import org.apache.commons.csv.CSVPrinter;

import java.math.BigInteger;

public class Organisation extends AbstractModel {

    private BigInteger serviceProviderId;
    private String serviceProviderName;
    private String serviceProviderCode;
    private String providerGroup;
    private Integer commissionerId;
    private String commissioner;
    private String commissionerCode;
    private String pod;
    private String commissioningRegionCode;
    private String commissioningRegion;
    private String localAreaTeamCode;
    private String localAreaTeam;
    private String commissioningCounty;
    private String commissioningCountry;

    @Override
    public void writeHeaderToCsv(CSVPrinter csvPrinter) throws Exception {
        printString("SK_ServiceProviderID", csvPrinter);
        printString("ServiceProviderName", csvPrinter);
        printString("ServiceProviderCode", csvPrinter);
        printString("ProviderGroup", csvPrinter);
        printString("CommissionerID", csvPrinter);
        printString("Commissioner", csvPrinter);
        printString("CommissionerCode", csvPrinter);
        printString("POD", csvPrinter);
        printString("CommissioningRegionCode", csvPrinter);
        printString("CommissioningRegion", csvPrinter);
        printString("LocalAreaTeamCode", csvPrinter);
        printString("LocalAreaTeam", csvPrinter);
        printString("CommissioningCounty", csvPrinter);
        printString("CommissioningCountry", csvPrinter);
    }

    @Override
    public void writeRecordToCsv(CSVPrinter csvPrinter) throws Exception {
        printBigInt(serviceProviderId, csvPrinter);
        printString(serviceProviderName, csvPrinter);
        printString(serviceProviderCode, csvPrinter);
        printString(providerGroup, csvPrinter);
        printInt(commissionerId, csvPrinter);
        printString(commissioner, csvPrinter);
        printString(commissionerCode, csvPrinter);
        printString(pod, csvPrinter);
        printString(commissioningRegionCode, csvPrinter);
        printString(commissioningRegion, csvPrinter);
        printString(localAreaTeamCode, csvPrinter);
        printString(localAreaTeam, csvPrinter);
        printString(commissioningCounty, csvPrinter);
        printString(commissioningCountry, csvPrinter);
    }

    @Override
    public BigInteger getServiceProviderId() {
        return serviceProviderId;
    }

    @Override
    public void setServiceProviderId(BigInteger serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public String getServiceProviderCode() {
        return serviceProviderCode;
    }

    public void setServiceProviderCode(String serviceProviderCode) {
        this.serviceProviderCode = serviceProviderCode;
    }

    public String getProviderGroup() {
        return providerGroup;
    }

    public void setProviderGroup(String providerGroup) {
        this.providerGroup = providerGroup;
    }

    public Integer getCommissionerId() {
        return commissionerId;
    }

    public void setCommissionerId(Integer commissionerId) {
        this.commissionerId = commissionerId;
    }

    public String getCommissioner() {
        return commissioner;
    }

    public void setCommissioner(String commissioner) {
        this.commissioner = commissioner;
    }

    public String getCommissionerCode() {
        return commissionerCode;
    }

    public void setCommissionerCode(String commissionerCode) {
        this.commissionerCode = commissionerCode;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getCommissioningRegionCode() {
        return commissioningRegionCode;
    }

    public void setCommissioningRegionCode(String commissioningRegionCode) {
        this.commissioningRegionCode = commissioningRegionCode;
    }

    public String getCommissioningRegion() {
        return commissioningRegion;
    }

    public void setCommissioningRegion(String commissioningRegion) {
        this.commissioningRegion = commissioningRegion;
    }

    public String getLocalAreaTeamCode() {
        return localAreaTeamCode;
    }

    public void setLocalAreaTeamCode(String localAreaTeamCode) {
        this.localAreaTeamCode = localAreaTeamCode;
    }

    public String getLocalAreaTeam() {
        return localAreaTeam;
    }

    public void setLocalAreaTeam(String localAreaTeam) {
        this.localAreaTeam = localAreaTeam;
    }

    public String getCommissioningCounty() {
        return commissioningCounty;
    }

    public void setCommissioningCounty(String commissioningCounty) {
        this.commissioningCounty = commissioningCounty;
    }

    public String getCommissioningCountry() {
        return commissioningCountry;
    }

    public void setCommissioningCountry(String commissioningCountry) {
        this.commissioningCountry = commissioningCountry;
    }
}

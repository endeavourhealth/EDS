package org.endeavourhealth.transform.ceg.models;

import org.apache.commons.csv.CSVPrinter;

import java.math.BigInteger;

public class Staff extends AbstractModel {

    private BigInteger staffId;
    private String authorisingUser;
    private String authorisingRole;

    @Override
    public void writeHeaderToCsv(CSVPrinter csvPrinter) throws Exception {
        printString("SK_StaffID", csvPrinter);
        printString("AuthorisingUser", csvPrinter);
        printString("AuthorisingUserRole", csvPrinter);
    }

    @Override
    public void writeRecordToCsv(CSVPrinter csvPrinter) throws Exception {
        printBigInt(staffId, csvPrinter);
        printString(authorisingUser, csvPrinter);
        printString(authorisingRole, csvPrinter);
    }

    @Override
    public BigInteger getServiceProviderId() {
        return null;
    }

    @Override
    public void setServiceProviderId(BigInteger serviceProviderId) {
        //do nothing, as this model doesn't use this
    }

    public BigInteger getStaffId() {
        return staffId;
    }

    public void setStaffId(BigInteger staffId) {
        this.staffId = staffId;
    }

    public String getAuthorisingUser() {
        return authorisingUser;
    }

    public void setAuthorisingUser(String authorisingUser) {
        this.authorisingUser = authorisingUser;
    }

    public String getAuthorisingRole() {
        return authorisingRole;
    }

    public void setAuthorisingRole(String authorisingRole) {
        this.authorisingRole = authorisingRole;
    }


}

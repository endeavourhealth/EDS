package org.endeavourhealth.ui.database.administration;

import org.endeavourhealth.ui.database.*;

import java.time.Instant;
import java.util.UUID;

public class DbEndUserPwd extends DbAbstractTable {

    private static final TableAdapter adapter = new TableAdapter(DbEndUserPwd.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID endUserPwdUuid = null;
    @DatabaseColumn
    private UUID endUserUuid = null;
    @DatabaseColumn
    private String pwdHash = null;
    @DatabaseColumn
    private Instant dtExpired = null;
    @DatabaseColumn
    private Integer failedAttempts = null;
    @DatabaseColumn
    private boolean isOneTimeUse = false;

    public DbEndUserPwd() {

    }

    public static DbEndUserPwd retrieveForEndUserNotExpired(UUID endUserUuid) throws Exception {
        return DatabaseManager.db().retrieveEndUserPwdForUserNotExpired(endUserUuid);
    }

    public static DbEndUserPwd retrieveForUuid(UUID uuid) throws Exception {
        return DatabaseManager.db().retrieveForPrimaryKeys(DbEndUserPwd.class, uuid);
    }

    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    /**
     * gets/sets
     */
    public UUID getEndUserPwdUuid() {
        return endUserPwdUuid;
    }

    public void setEndUserPwdUuid(UUID endUserPwdUuid) {
        this.endUserPwdUuid = endUserPwdUuid;
    }

    public UUID getEndUserUuid() {
        return endUserUuid;
    }

    public void setEndUserUuid(UUID endUserUuid) {
        this.endUserUuid = endUserUuid;
    }

    public String getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }

    public Instant getDtExpired() {
        return dtExpired;
    }

    public void setDtExpired(Instant dtExpired) {
        this.dtExpired = dtExpired;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public boolean isOneTimeUse() {
        return isOneTimeUse;
    }

    public void setOneTimeUse(boolean oneTimeUse) {
        isOneTimeUse = oneTimeUse;
    }
}

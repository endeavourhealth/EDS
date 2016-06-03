package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "admin", name = "end_user_pwd")
public class EndUserPwd {
    @PartitionKey
    @Column(name = "id")
    private UUID id;
    @Column(name = "end_user_id")
    private UUID endUserId;
    @Column(name = "pwd_hash")
    private String pwdHash;
    @Column(name = "dt_expired")
    private Date dtExpired;
    @Column(name = "failed_attempts")
    private Integer failedAttempts;
    @Column(name = "is_one_time_use")
    private Boolean isOneTimeUse;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEndUserId() {
        return endUserId;
    }

    public void setEndUserId(UUID endUserId) {
        this.endUserId = endUserId;
    }

    public String getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }

    public Date getDtExpired() {
        return dtExpired;
    }

    public void setDtExpired(Date dtExpired) {
        this.dtExpired = dtExpired;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Boolean getIsOneTimeUse() {
        return isOneTimeUse;
    }

    public void setIsOneTimeUse(Boolean isOneTimeUse) {
        this.isOneTimeUse = isOneTimeUse;
    }

}
package org.endeavourhealth.ui.database.administration;

import org.endeavourhealth.ui.database.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class DbEndUserEmailInvite extends DbAbstractTable {

    private static final TableAdapter adapter = new TableAdapter(DbEndUserEmailInvite.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID endUserEmailInviteUuid = null;
    @DatabaseColumn
    private UUID endUserUuid = null;
    @DatabaseColumn
    private String uniqueToken = null;
    @DatabaseColumn
    private Instant dtCompleted = null;


    public DbEndUserEmailInvite() {
    }

    public static List<DbEndUserEmailInvite> retrieveForEndUserNotCompleted(UUID userUuid) throws Exception {
        return DatabaseManager.db().retrieveEndUserEmailInviteForUserNotCompleted(userUuid);
    }

    public static DbEndUserEmailInvite retrieveForToken(String token) throws Exception {
        return DatabaseManager.db().retrieveEndUserEmailInviteForToken(token);
    }

    public static DbEndUserEmailInvite retrieveForUuid(UUID uuid) throws Exception {
        return DatabaseManager.db().retrieveForPrimaryKeys(DbEndUserEmailInvite.class, uuid);
    }

    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    /**
     * gets/sets
     */
    public UUID getEndUserEmailInviteUuid() {
        return endUserEmailInviteUuid;
    }

    public void setEndUserEmailInviteUuid(UUID endUserEmailInviteUuid) {
        this.endUserEmailInviteUuid = endUserEmailInviteUuid;
    }

    public UUID getEndUserUuid() {
        return endUserUuid;
    }

    public void setEndUserUuid(UUID endUserUuid) {
        this.endUserUuid = endUserUuid;
    }

    public String getUniqueToken() {
        return uniqueToken;
    }

    public void setUniqueToken(String uniqueToken) {
        this.uniqueToken = uniqueToken;
    }

    public Instant getDtCompleted() {
        return dtCompleted;
    }

    public void setDtCompleted(Instant dtCompleted) {
        this.dtCompleted = dtCompleted;
    }
}

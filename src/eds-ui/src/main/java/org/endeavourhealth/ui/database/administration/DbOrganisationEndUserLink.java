package org.endeavourhealth.ui.database.administration;

import org.endeavourhealth.ui.database.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DB entity linking endUsers to organisations
 */
public final class DbOrganisationEndUserLink extends DbAbstractTable {

    private static final TableAdapter adapter = new TableAdapter(DbOrganisationEndUserLink.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID organisationEndUserLinkUuid = null;
    @DatabaseColumn
    private UUID organisationUuid = null;
    @DatabaseColumn
    private UUID endUserUuid = null;
    @DatabaseColumn
    private boolean isAdmin = false;
    @DatabaseColumn
    private Instant dtExpired = null;


    public DbOrganisationEndUserLink() {
    }

    public static List<DbOrganisationEndUserLink> retrieveForEndUserNotExpired(UUID endUserUuid) throws Exception {
        return DatabaseManager.db().retrieveOrganisationEndUserLinksForUserNotExpired(endUserUuid);
    }

    public static DbOrganisationEndUserLink retrieveForOrganisationEndUserNotExpired(UUID organisationUuid, UUID endUserUuid) throws Exception {
        return DatabaseManager.db().retrieveOrganisationEndUserLinksForOrganisationEndUserNotExpired(organisationUuid, endUserUuid);
    }

    public static List<DbOrganisationEndUserLink> retrieveForOrganisationNotExpired(UUID organisationUuid) throws Exception {
        return DatabaseManager.db().retrieveOrganisationEndUserLinksForOrganisationNotExpired(organisationUuid);
    }

    public static DbOrganisationEndUserLink retrieveForUuid(UUID uuid) throws Exception {
        return DatabaseManager.db().retrieveForPrimaryKeys(DbOrganisationEndUserLink.class, uuid);
    }

    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    /**
     * gets/sets
     */
    public UUID getOrganisationEndUserLinkUuid() {
        return organisationEndUserLinkUuid;
    }

    public void setOrganisationEndUserLinkUuid(UUID organisationEndUserLinkUuid) {
        this.organisationEndUserLinkUuid = organisationEndUserLinkUuid;
    }

    public UUID getOrganisationUuid() {
        return organisationUuid;
    }

    public void setOrganisationUuid(UUID organisationUuid) {
        this.organisationUuid = organisationUuid;
    }

    public UUID getEndUserUuid() {
        return endUserUuid;
    }

    public void setEndUserUuid(UUID endUserUuid) {
        this.endUserUuid = endUserUuid;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Instant getDtExpired() {
        return dtExpired;
    }

    public void setDtExpired(Instant dtExpired) {
        this.dtExpired = dtExpired;
    }
}

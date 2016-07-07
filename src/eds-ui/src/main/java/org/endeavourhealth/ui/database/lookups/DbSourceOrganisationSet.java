package org.endeavourhealth.ui.database.lookups;

import org.endeavourhealth.ui.database.*;

import java.util.List;
import java.util.UUID;

public final class DbSourceOrganisationSet extends DbAbstractTable {
    private static final TableAdapter adapter = new TableAdapter(DbSourceOrganisationSet.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID sourceOrganisationSetUuid = null;
    @DatabaseColumn
    private UUID organisationUuid = null;
    @DatabaseColumn
    private String name = null;
    @DatabaseColumn
    private String odsCodes = null;

    public static List<DbSourceOrganisationSet> retrieveAllSets(UUID organisationUuid) throws Exception {
        return DatabaseManager.db().retrieveAllOrganisationSets(organisationUuid);
    }
    public static List<DbSourceOrganisationSet> retrieveSets(UUID organisationUuid, String searchTerm) throws Exception {
        return DatabaseManager.db().retrieveOrganisationSetsForSearchTerm(organisationUuid, searchTerm);
    }
    public static DbSourceOrganisationSet retrieveSetForUuid(UUID uuid) throws Exception {
        return DatabaseManager.db().retrieveForPrimaryKeys(DbSourceOrganisationSet.class, uuid);
    }


    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    /**
     * gets/sets
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOdsCodes() {
        return odsCodes;
    }

    public void setOdsCodes(String odsCodes) {
        this.odsCodes = odsCodes;
    }

    public UUID getSourceOrganisationSetUuid() {
        return sourceOrganisationSetUuid;
    }

    public void setSourceOrganisationSetUuid(UUID sourceOrganisationSetUuid) {
        this.sourceOrganisationSetUuid = sourceOrganisationSetUuid;
    }

    public UUID getOrganisationUuid() {
        return organisationUuid;
    }

    public void setOrganisationUuid(UUID organisationUuid) {
        this.organisationUuid = organisationUuid;
    }
}

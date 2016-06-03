package org.endeavourhealth.ui.database.administration;

import org.endeavourhealth.ui.database.*;

import java.util.List;
import java.util.UUID;

public final class DbOrganisation extends DbAbstractTable {

    private static final TableAdapter adapter = new TableAdapter(DbOrganisation.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID organisationUuid = null;
    @DatabaseColumn
    private String name = null;
    @DatabaseColumn
    private String nationalId = null;


    public DbOrganisation() {
    }

    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    public static List<DbOrganisation> retrieveForAll() throws Exception {
        return DatabaseManager.db().retrieveAllOrganisations();
    }

    public static DbOrganisation retrieveForUuid(UUID uuid) throws Exception {
        return DatabaseManager.db().retrieveForPrimaryKeys(DbOrganisation.class, uuid);
    }

    public static DbOrganisation retrieveOrganisationForNameNationalId(String name, String nationalId) throws Exception {
        return DatabaseManager.db().retrieveOrganisationForNameNationalId(name, nationalId);
    }


    /**
     * gets/sets
     */
    public UUID getOrganisationUuid() {
        return organisationUuid;
    }

    public void setOrganisationUuid(UUID organisationUuid) {
        this.organisationUuid = organisationUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }
}

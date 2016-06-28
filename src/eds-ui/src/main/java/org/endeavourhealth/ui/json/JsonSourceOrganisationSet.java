package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisationSet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonSourceOrganisationSet implements Comparable {
    private UUID uuid = null;
    private String name = null;
    private List<JsonSourceOrganisation> organisations = null;

    public JsonSourceOrganisationSet() {}

    public JsonSourceOrganisationSet(DbSourceOrganisationSet set) {
        this.uuid = set.getSourceOrganisationSetUuid();
        this.name = set.getName();
    }

    public void addOrganisation(JsonSourceOrganisation organisation) {
        if (organisations == null) {
            organisations = new ArrayList<>();
        }
        organisations.add(organisation);
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

    public List<JsonSourceOrganisation> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(List<JsonSourceOrganisation> organisations) {
        this.organisations = organisations;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public int compareTo(Object o) {
        JsonSourceOrganisationSet other = (JsonSourceOrganisationSet)o;
        return name.compareToIgnoreCase(other.getName());
    }
}

package org.endeavourhealth.core.mySQLDatabase.models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by studu on 13/03/2017.
 */
public class RegionorganisationmapEntityPK implements Serializable {
    private String regionUuid;
    private String organisationUUid;

    @Column(name = "regionUuid", nullable = false, length = 36)
    @Basic
    @Id
    public String getRegionUuid() {
        return regionUuid;
    }

    public void setRegionUuid(String regionUuid) {
        this.regionUuid = regionUuid;
    }

    @Column(name = "organisationUUid", nullable = false, length = 36)
    @Basic
    @Id
    public String getOrganisationUUid() {
        return organisationUUid;
    }

    public void setOrganisationUUid(String organisationUUid) {
        this.organisationUUid = organisationUUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegionorganisationmapEntityPK that = (RegionorganisationmapEntityPK) o;

        if (regionUuid != null ? !regionUuid.equals(that.regionUuid) : that.regionUuid != null) return false;
        if (organisationUUid != null ? !organisationUUid.equals(that.organisationUUid) : that.organisationUUid != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = regionUuid != null ? regionUuid.hashCode() : 0;
        result = 31 * result + (organisationUUid != null ? organisationUUid.hashCode() : 0);
        return result;
    }
}

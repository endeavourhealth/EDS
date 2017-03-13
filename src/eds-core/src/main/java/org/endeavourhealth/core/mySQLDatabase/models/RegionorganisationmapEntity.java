package org.endeavourhealth.core.mySQLDatabase.models;

import javax.persistence.*;

/**
 * Created by studu on 10/03/2017.
 */
@Entity
@Table(name = "regionorganisationmap", schema = "organisationmanager", catalog = "")
public class RegionorganisationmapEntity {
    private String regionUuid;
    private String organisationUUid;
    private int id;

    @Basic
    @Column(name = "regionUuid", nullable = false, length = 36)
    public String getRegionUuid() {
        return regionUuid;
    }

    public void setRegionUuid(String regionUuid) {
        this.regionUuid = regionUuid;
    }

    @Basic
    @Column(name = "organisationUUid", nullable = false, length = 36)
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

        RegionorganisationmapEntity that = (RegionorganisationmapEntity) o;

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

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

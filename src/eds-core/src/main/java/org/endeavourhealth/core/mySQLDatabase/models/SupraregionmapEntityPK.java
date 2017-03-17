package org.endeavourhealth.core.mySQLDatabase.models;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class SupraregionmapEntityPK implements Serializable {
    private String parentRegionUuid;
    private String childRegionUUid;

    @Column(name = "ParentRegionUuid", nullable = false, length = 36)
    @Id
    public String getParentRegionUuid() {
        return parentRegionUuid;
    }

    public void setParentRegionUuid(String parentRegionUuid) {
        this.parentRegionUuid = parentRegionUuid;
    }

    @Column(name = "ChildRegionUUid", nullable = false, length = 36)
    @Id
    public String getChildRegionUUid() {
        return childRegionUUid;
    }

    public void setChildRegionUUid(String childRegionUUid) {
        this.childRegionUUid = childRegionUUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SupraregionmapEntityPK that = (SupraregionmapEntityPK) o;

        if (parentRegionUuid != null ? !parentRegionUuid.equals(that.parentRegionUuid) : that.parentRegionUuid != null)
            return false;
        if (childRegionUUid != null ? !childRegionUUid.equals(that.childRegionUUid) : that.childRegionUUid != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parentRegionUuid != null ? parentRegionUuid.hashCode() : 0;
        result = 31 * result + (childRegionUUid != null ? childRegionUUid.hashCode() : 0);
        return result;
    }
}

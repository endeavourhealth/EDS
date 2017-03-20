package org.endeavourhealth.core.mySQLDatabase.models;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by studu on 20/03/2017.
 */
public class MastermappingEntityPK implements Serializable {
    private String childUuid;
    private String parentUUid;
    private short mapTypeId;

    @Column(name = "ChildUuid", nullable = false, length = 36)
    @Id
    public String getChildUuid() {
        return childUuid;
    }

    public void setChildUuid(String childUuid) {
        this.childUuid = childUuid;
    }

    @Column(name = "ParentUUid", nullable = false, length = 36)
    @Id
    public String getParentUUid() {
        return parentUUid;
    }

    public void setParentUUid(String parentUUid) {
        this.parentUUid = parentUUid;
    }

    @Column(name = "MapTypeId", nullable = false)
    @Id
    public short getMapTypeId() {
        return mapTypeId;
    }

    public void setMapTypeId(short mapTypeId) {
        this.mapTypeId = mapTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MastermappingEntityPK that = (MastermappingEntityPK) o;

        if (mapTypeId != that.mapTypeId) return false;
        if (childUuid != null ? !childUuid.equals(that.childUuid) : that.childUuid != null) return false;
        if (parentUUid != null ? !parentUUid.equals(that.parentUUid) : that.parentUUid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = childUuid != null ? childUuid.hashCode() : 0;
        result = 31 * result + (parentUUid != null ? parentUUid.hashCode() : 0);
        result = 31 * result + (int) mapTypeId;
        return result;
    }
}

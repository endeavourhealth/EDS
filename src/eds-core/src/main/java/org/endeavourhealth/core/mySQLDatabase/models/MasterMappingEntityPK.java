package org.endeavourhealth.core.mySQLDatabase.models;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class MasterMappingEntityPK implements Serializable {
    private String childUuid;
    private String parentUUid;
    private short childMapTypeId;
    private short parentMapTypeId;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterMappingEntityPK that = (MasterMappingEntityPK) o;

        if (childMapTypeId != that.childMapTypeId) return false;
        if (parentMapTypeId != that.parentMapTypeId) return false;
        if (childUuid != null ? !childUuid.equals(that.childUuid) : that.childUuid != null) return false;
        if (parentUUid != null ? !parentUUid.equals(that.parentUUid) : that.parentUUid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = childUuid != null ? childUuid.hashCode() : 0;
        result = 31 * result + (parentUUid != null ? parentUUid.hashCode() : 0);
        result = 31 * result + (int) childMapTypeId;
        result = 31 * result + (int) parentMapTypeId;
        return result;
    }

    @Column(name = "ChildMapTypeId", nullable = false)
    @Id
    public short getChildMapTypeId() {
        return childMapTypeId;
    }

    public void setChildMapTypeId(short childMapTypeId) {
        this.childMapTypeId = childMapTypeId;
    }

    @Column(name = "ParentMapTypeId", nullable = false)
    @Id
    public short getParentMapTypeId() {
        return parentMapTypeId;
    }

    public void setParentMapTypeId(short parentMapTypeId) {
        this.parentMapTypeId = parentMapTypeId;
    }
}

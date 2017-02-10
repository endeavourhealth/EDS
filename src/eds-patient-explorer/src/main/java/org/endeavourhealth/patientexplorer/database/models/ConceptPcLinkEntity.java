package org.endeavourhealth.patientexplorer.database.models;

import javax.persistence.*;

@Entity
@Table(name = "trm_concept_pc_link", schema = "\"public\"", catalog = "coding")
public class ConceptPcLinkEntity {
    private long pid;
    private int rel_type;
    private long child_pid;
    private long codesystem_pid;
    private long parent_pid;

    @Id
    @Column(name = "pid")
    public long getPid() {
        return pid;
    }
    public void setPid(long pid) {
        this.pid = pid;
    }

    @Basic
    @Column(name = "rel_type")
    public int getRel_type() {
        return rel_type;
    }
    public void setRel_type(int rel_type) {
        this.rel_type = rel_type;
    }

    @Basic
    @Column(name = "child_pid")
    public long getChild_pid() {
        return child_pid;
    }
    public void setChild_pid(long child_pid) {
        this.child_pid = child_pid;
    }

    @Basic
    @Column(name = "codesystem_pid")
    public long getCodesystem_pid() {
        return codesystem_pid;
    }
    public void setCodesystem_pid(long codesystem_pid) {
        this.codesystem_pid = codesystem_pid;
    }

    @Basic
    @Column(name = "parent_pid")
    public long getParent_pid() {
        return parent_pid;
    }
    public void setParent_pid(long parent_id) {
        this.parent_pid = parent_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConceptPcLinkEntity that = (ConceptPcLinkEntity) o;

        if (pid != that.pid) return false;
        if (rel_type != that.rel_type) return false;
        if (child_pid != that.child_pid) return false;
        if (codesystem_pid != that.codesystem_pid) return false;
        if (parent_pid != that.parent_pid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (pid ^ (pid >>> 32));
        result = 31 * result + (rel_type ^ (rel_type >>> 32));
        result = (int) (31 * result + (child_pid ^ (child_pid >>> 32)));
        result = (int) (31 * result + (codesystem_pid ^ (codesystem_pid >>> 32)));
        result = (int) (31 * result + (parent_pid ^ (parent_pid >>> 32)));
        return result;
    }

}

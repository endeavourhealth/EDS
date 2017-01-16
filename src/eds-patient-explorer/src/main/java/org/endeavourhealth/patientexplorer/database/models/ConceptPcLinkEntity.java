package org.endeavourhealth.patientexplorer.database.models;

import javax.persistence.*;

@Entity
@Table(name = "trm_concept_pc_link", schema = "\"public\"", catalog = "coding")
public class ConceptPcLinkEntity {
    private int pid;
    private short rel_type;
    private int child_pid;
    private int codesystem_pid;
    private int parent_pid;

    @Id
    @Column(name = "pid")
    public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
        this.pid = pid;
    }

    @Basic
    @Column(name = "rel_type")
    public short getRel_type() {
        return rel_type;
    }
    public void setRel_type(short rel_type) {
        this.rel_type = rel_type;
    }

    @Basic
    @Column(name = "child_pid")
    public int getChild_pid() {
        return child_pid;
    }
    public void setChild_pid(int child_pid) {
        this.child_pid = child_pid;
    }

    @Basic
    @Column(name = "codesystem_pid")
    public int getCodesystem_pid() {
        return codesystem_pid;
    }
    public void setCodesystem_pid(int codesystem_pid) {
        this.codesystem_pid = codesystem_pid;
    }

    @Basic
    @Column(name = "parent_pid")
    public int getParent_pid() {
        return parent_pid;
    }
    public void setParent_pid(int parent_id) {
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
        int result = (pid ^ (pid >>> 32));
        result = 31 * result + (rel_type ^ (rel_type >>> 32));
        result = 31 * result + (child_pid ^ (child_pid >>> 32));
        result = 31 * result + (codesystem_pid ^ (codesystem_pid >>> 32));
        result = 31 * result + (parent_pid ^ (parent_pid >>> 32));
        return result;
    }

}
